/**
 *
 * Copyright 2014-2019 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.jpa.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.guicy.TangramServletModule;
import org.tangram.guicy.postconstruct.PostConstructModule;
import org.tangram.jpa.Code;
import org.tangram.jpa.JpaContent;
import org.tangram.jpa.protection.PasswordProtection;
import org.tangram.jpa.test.content.BaseClass;
import org.tangram.jpa.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test the basic content based features based on an OpenJPA integration test with HsqlDB and Guicy / Google Guice
 * for setup of the test environment.
 *
 * Also tests the contents of the enhanced JARs according to some basic rules.
 */
public class JpaContentTest extends BaseContentTest<EntityManager, Query> {

    private static final Logger LOG = LoggerFactory.getLogger(JpaContentTest.class);


    static {
        org.apache.openjpa.enhance.InstrumentationFactory.setDynamicallyInstallAgent(false);
    }

    private static final Collection<Class<?>> ENHANCED_CLASSES = Arrays.asList(JpaContent.class, Code.class);

    private Collection<String> baseJarEntries = new ArrayList<>(16);

    private String projectName;

    private String projectVersion;

    private String buildDir;


    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
        Injector injector = Guice.createInjector(new PostConstructModule(), new TangramServletModule());
        return injector.getInstance(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<EntityManager, Query> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface<Query> createSubBean(MutableBeanFactory<EntityManager, Query> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "org.apache.openjpa";
    }


    @Override
    protected String getCondition(MutableBeanFactory<EntityManager, Query> beanFactory) {
        return "select x from SubClass x where x.subtitle = 'great'";
    } // getCondition()


    @Override
    protected void setPeers(BaseInterface base, BaseInterface peer) {
        List<BaseClass> peers = new ArrayList<>();
        peers.add((BaseClass) peer);
        ((BaseClass) base).setPeers(peers);
    } // setPeers()


    @Override
    protected int getNumberOfAllClasses() {
        return 8;
    }


    @Override
    protected int getNumberOfClasses() {
        return 4;
    }


    private String getJarName(String jpaLibraryName) {
        return buildDir+"/libs/"+projectName+"-"+projectVersion+(StringUtils.isEmpty(jpaLibraryName) ? "" : "-"+jpaLibraryName)+".jar";
    }


    @BeforeClass
    protected void beforeClass() throws Exception {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("/org/tangram/jpa/test/content/test-environment.properties"));
            Assert.assertEquals(properties.size(), 3, "Unexpected number of test environment properties.");
            projectName = properties.getProperty("project.name");
            Assert.assertFalse(StringUtils.isEmpty(projectName), "Project name should be available.");
            projectVersion = properties.getProperty("project.version");
            Assert.assertFalse(StringUtils.isEmpty(projectVersion), "Project version should be available.");
            buildDir = properties.getProperty("project.build.dir");
            Assert.assertFalse(StringUtils.isEmpty(buildDir), "Project build dir should be available.");
        } catch (IOException ioe) {
            Assert.fail("Cannot determine test environment values."+ioe.getMessage());
        }
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(getJarName(null)))) {
            ZipEntry zipEntry = zip.getNextEntry();
            while (zipEntry!=null) {
                String name = zipEntry.getName();
                baseJarEntries.add(name);
                zipEntry = zip.getNextEntry();
            }
        } catch (IOException ioe) {
            Assert.fail("base JPA JAR as a reference not found: "+ioe.getMessage());
        }
    } // getInstance()


    @Test
    public void testPasswordProtection() {
        PasswordProtection passwordProtection = new PasswordProtection();
        passwordProtection.setLogin(TESTUSER);
        passwordProtection.setPassword(TESTPASSWORD);
        passwordProtection.setProtectionKey("mock password protection");
        passwordProtection.setProtectedContents(Collections.emptyList());
        checkSimplePasswordProtection(passwordProtection);
    } // testPasswordProtection()


    private ByteArrayClassLoader.ChildFirst readJarFile(String jpaName) {
        String context = jpaName.toLowerCase();
        int idx = context.indexOf('/');
        if (idx>0) {
            context = context.substring(0, idx);
        }
        Map<String, byte[]> classes = new HashMap<>();
        String jarName = getJarName(context);
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarName))) {
            int count = 0;
            ZipEntry zipEntry = zip.getNextEntry();
            while (zipEntry!=null) {
                String name = zipEntry.getName();
                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.').replaceFirst(".class", "");
                    LOG.info("readJarFile() {}: {}", name, className);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int len = zip.read(buffer); len>=0; len = zip.read(buffer)) {
                        baos.write(buffer, 0, len);
                    } // for
                    byte[] data = baos.toByteArray();
                    classes.put(className, data);
                }
                count++;
                Assert.assertNotNull(name, "JAR-Entry should have a name.");
                zipEntry = zip.getNextEntry();
            }
            Assert.assertEquals(count, baseJarEntries.size(), "Unexpected number of entries in enhanced JAR for "+jpaName+".");
        } catch (IOException ioe) {
            Assert.fail("JAR for "+jpaName+" not found: "+jarName);
        }
        return new ByteArrayClassLoader.ChildFirst(Thread.currentThread().getContextClassLoader(), classes);
    }


    private void checkJar(String jpaName, String methodPrefix) throws SecurityException {
        ClassLoader loader = readJarFile(jpaName);
        try {
            for (Class<?> c : ENHANCED_CLASSES) {
                Class<?> cls = loader.loadClass(c.getName());
                Method[] methods = cls.getMethods();
                boolean flag = false;
                for (Method method : methods) {
                    LOG.info("checkJar({}) method name in {}.{}", jpaName, c.getName(), method.getName());
                    if (method.getName().startsWith(methodPrefix)) {
                        flag = true;
                    } // if
                } // for
                Assert.assertTrue(flag, "Classes not enhanced for "+jpaName+". We miss the method prefix "+methodPrefix+".");
            }
        } catch (ClassNotFoundException cnfe) {
            Assert.fail("JAR for "+jpaName+" does not contain class "+cnfe.getMessage());
        }
    }


    public void optionaTestJavaEEContainerJar() {
        readJarFile("JEE");
    }


    @Test
    public void testDatanucleusJar() {
        checkJar("Datanucleus/JPA", "dn");
    }


    @Test
    public void testEclipseLinkJar() {
        checkJar("EclipseLink", "_persistence");
    }


    @Test
    public void testHibernateJar() {
        checkJar("Hibernate", "$$_hibernate");
    }


    @Test
    public void testOpenJPAJar() {
        checkJar("OpenJPA", "pc");
    }

} // JpaContentTest
