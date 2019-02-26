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
package org.tangram.mutable.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.tangram.components.mutable.ToolHandler;
import org.tangram.content.ChangeListener;
import org.tangram.content.Content;
import org.tangram.content.TransientCode;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableCode;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.tangram.protection.SimplePasswordProtection;
import org.tangram.util.SystemUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


public abstract class BaseContentTest<M extends Object, Q extends Object> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseContentTest.class);

    private static final String CLASS_CODE = "package org.tangram.example; import javax.inject.Named; @Named public class Test { public String s; }";

    protected static final String TESTPASSWORD = "testpassword";

    protected static final String TESTUSER = "testuser";

    private ChangeListener changeListener;


    /**
     * Check if any of the method's names starts with a given prefix.
     *
     * All byte-code transformers create such schematic methods.
     *
     * @param methods array of methods to check names
     * @param prefix name prefix at least one of the method's names should have
     * @return true if any of the method's names starts with the given prefix.
     */
    public static boolean checkMethodPrefixOccurs(Method[] methods, String prefix) {
        boolean flag = false;
        for (Method method : methods) {
            if (method.getName().startsWith(prefix)) {
                flag = true;
            } // if
        } // for
        return flag;
    } // checkMethodPrefixOccurs()


    /**
     * Test implementations of the simple password protection interfaces.
     *
     * @param passwordProtection instance under test
     */
    public static void checkSimplePasswordProtection(SimplePasswordProtection passwordProtection) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = new MockHttpSession();
        request.setSession(session);
        boolean visible = true;
        try {
            visible = passwordProtection.isContentVisible(request);
        } catch (Exception e) {
            Assert.fail("Password protection should not issue exceptions.", e);
        } // try/catch
        Assert.assertFalse(visible, "Initially content should not be visible.");
        Assert.assertTrue(passwordProtection.needsAuthorization(request), "Initially we should need a login.");

        HttpServletResponse response = new MockHttpServletResponse();
        String handleLoginResult = "X";
        try {
            handleLoginResult = passwordProtection.handleLogin(request, response);
        } catch (Exception e) {
            Assert.fail("Password protection should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(handleLoginResult, SimplePasswordProtection.ERROR_CODE, "No password should result in error.");

        request = new MockHttpServletRequest();
        request.setSession(session);
        response = new MockHttpServletResponse();
        request.setParameter(SimplePasswordProtection.PARAM_LOGIN, TESTUSER);
        request.setParameter(SimplePasswordProtection.PARAM_PASSWORD, "x");
        try {
            handleLoginResult = passwordProtection.handleLogin(request, response);
        } catch (Exception e) {
            Assert.fail("Password protection should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(handleLoginResult, SimplePasswordProtection.ERROR_CODE, "Wrong password should result in error.");

        request = new MockHttpServletRequest();
        request.setSession(session);
        response = new MockHttpServletResponse();
        request.setParameter(SimplePasswordProtection.PARAM_LOGIN, TESTUSER);
        request.setParameter(SimplePasswordProtection.PARAM_PASSWORD, TESTPASSWORD);
        try {
            handleLoginResult = passwordProtection.handleLogin(request, response);
        } catch (Exception e) {
            Assert.fail("Password protection should not issue exceptions.", e);
        } // try/catch
        Assert.assertNull(handleLoginResult, "Login should have succeded.");

        request = new MockHttpServletRequest();
        request.setSession(session);

        visible = true;
        try {
            visible = passwordProtection.isContentVisible(request);
        } catch (Exception e) {
            Assert.fail("Password protection should not issue exceptions.", e);
        } // try/catch
        Assert.assertTrue(visible, "After login content should be visible.");
        Assert.assertFalse(passwordProtection.needsAuthorization(request), "After login we should not need a second.");

        Assert.assertEquals(passwordProtection.getProtectedContents().size(), 0, "Unexpected list of protected contents.");
        Assert.assertEquals(passwordProtection.getProtectionPath().size(), 1, "Unexpected protection path.");
        Assert.assertEquals(passwordProtection.getProtectionPath().get(0), passwordProtection, "Unexpected protection path.");
    } // checkSimplePasswordProtection()


    public BaseContentTest() {
        changeListener = new ChangeListener() {
            @Override
            public void delete(Content content) {
                LOG.info("delete()");
            }


            @Override
            public void update(Content content) {
                LOG.info("update()");
            }
        };
    }


    protected Map<String, Object> getBeansForScope() {
        Map<String, Object> result = new HashMap<>();
        org.springframework.mock.web.MockServletContext context = new org.springframework.mock.web.MockServletContext() {

            @Override
            public String getRealPath(String path) {
                return "/x"+path;
            }

        };
        context.setContextPath("/");
        result.put("servletContext", context);
        return result;
    }


    /**
     * Init DI container and return instance of given type from that container.
     *
     * @param <T> type variable for the result type
     * @param type type constraint for the result instance
     * @param create is this test meant to create content or just check results
     * @throws Exception Anything might happen during such a process
     * @return result if available in the DI container or null
     */
    protected abstract <T extends Object> T getInstance(Class<T> type) throws Exception;


    protected MutableBeanFactory<M, Q> getMutableBeanFactory() throws Exception {
        MutableBeanFactory<M, Q> beanFactory = SystemUtils.convert(getInstance(MutableBeanFactory.class));
        beanFactory.addListener(changeListener);
        return beanFactory;
    } // getMutableBeanFactory


    protected abstract BaseInterface createBaseBean(MutableBeanFactory<M, Q> beanFactory) throws Exception;


    protected abstract SubInterface<Q> createSubBean(MutableBeanFactory<M, Q> beanFactory) throws Exception;


    protected abstract Class<? extends BaseInterface> getBaseClass();


    /**
     * Get package prefix of the package the manager instance resides in.
     *
     * @return java package name
     */
    protected abstract String getManagerPrefix();


    /**
     * Return the query element to retrieve the subinterface instance.
     *
     * @param beanFactory bean factory to be used to generated the query instance
     * @return implementation specific condition string
     */
    protected abstract String getCondition(MutableBeanFactory<M, Q> beanFactory);


    protected abstract void setPeers(BaseInterface base, BaseInterface sub);


    protected abstract int getNumberOfAllClasses();


    /**
     * Return expected number of available classes in our test configuration.
     *
     * @return Number of classes excluding interfaces and abstract classes
     */
    protected abstract int getNumberOfClasses();


    @Test(priority = 2)
    public void test02Factory() throws Exception {
        LOG.info("test02Factory() start.");
        MutableBeanFactory<M, Q> beanFactory = getMutableBeanFactory();
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        Object manager = beanFactory.getManager();
        Assert.assertNotNull(manager, "The factory should have an underlying manager instance.");
        String managerClassName = manager.getClass().getName();
        Assert.assertTrue(managerClassName.startsWith(getManagerPrefix()), "The factory should have a correctly typed manager instance.");
        List<Class<MutableCode>> codeClasses = beanFactory.getImplementingClasses(MutableCode.class);
        Assert.assertEquals(codeClasses.size(), 1, "We have one code class.");
        LOG.info("test02Factory() completed.");
    } // test2Factory()


    @Test(priority = 3)
    public void test03CreateTestContent() throws Exception {
        LOG.info("test03CreateTestContent() start.");
        MutableBeanFactory<M, Q> beanFactory = getMutableBeanFactory();
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        int numberOfAllClasses = getNumberOfAllClasses();
        // Assert.assertEquals(beanFactory.getAllClasses().toString(), "[interface org.tangram.feature.protection.Protection, interface org.tangram.feature.protection.ProtectedContent, interface org.tangram.mutable.test.content.SubInterface, interface org.tangram.mutable.MutableCode, interface org.tangram.content.CodeResource, class org.tangram.ebean.EContent, interface org.tangram.mutable.test.content.BaseInterface, class org.tangram.content.TransientCode, class org.tangram.ebean.Code, interface org.tangram.content.Content, class org.tangram.ebean.test.content.BaseClass, class org.tangram.ebean.test.content.SubClass]", "Discovered strange list of classes as strings.");
        Assert.assertEquals(beanFactory.getAllClasses().size(), numberOfAllClasses, "Have an unexpected total number of classes and interfaces available.");
        int numberOfClasses = getNumberOfClasses();
        Collection<Class<? extends Content>> ormClasses = beanFactory.getClasses();
        LOG.info("test03CreateTestContent() non abstract model classes {}", ormClasses);
        Assert.assertEquals(ormClasses.size(), numberOfClasses, "Discovered unexpected number of non abstract model classes.");

        BaseInterface beanB = createBaseBean(beanFactory);
        Assert.assertNotNull(beanB, "Could not create beanB.");
        beanB.setTitle("filter");
        beanFactory.persist(beanB);
        beanFactory.commitTransaction();

        SubInterface<Q> beanA = createSubBean(beanFactory);
        beanA.setSubtitle("great");
        Assert.assertNotNull(beanA, "Could not create bean.");
        setPeers(beanA, beanB);
        beanFactory.persist(beanA);
        beanFactory.commitTransaction();
        LOG.info("test03CreateTestContent() completed.");
    } // test03CreateTestContent()


    @Test(priority = 4)
    public void test04Components() throws Exception {
        LOG.info("test04Components() start.");
        MutableBeanFactory<M, Q> factory = getMutableBeanFactory();
        Assert.assertNotNull(factory, "Need factory for beans.");
        List<SubInterface> subBeans = factory.listBeans(SubInterface.class, getCondition(factory), "subtitle", true);
        Assert.assertEquals(subBeans.size(), 1, "We have prepared a fixed number of sub beans.");
        LOG.info("test04Components() obtaining instances of base interface.");
        List<? extends BaseInterface> allBeans = factory.listBeans(BaseInterface.class);
        LOG.info("test04Components() obtained instances of base interface.");
        Assert.assertEquals(allBeans.size(), 2, "We have prepared a fixed number of beans.");
        allBeans = factory.listBeans(BaseInterface.class);
        Assert.assertEquals(allBeans.size(), 2, "We have prepared a fixed number of beans cached.");
        // this contains is necessary due to possible subclassing in some of the APIs
        Assert.assertTrue(subBeans.get(0).getClass().getSimpleName().contains("SubClass"), "Peer of base beans is a sub bean.");
        List<? extends Content> baseBeans = factory.listBeansOfExactClass(getBaseClass());
        Assert.assertEquals(baseBeans.size(), 1, "We have prepared a fixed number of base beans.");
        LOG.info("test04Components() base beans {}", baseBeans);
        BaseInterface b = factory.getBean(getBaseClass(), baseBeans.get(0).getId());
        Assert.assertNotNull(b, "Bean should also be retrievable via ID.");
        baseBeans = factory.listBeansOfExactClass(getBaseClass());
        Assert.assertEquals(baseBeans.size(), 1, "We have prepared a fixed number of base beans cached.");
        LOG.info("test04Components() {} {} {}", factory, getBaseClass(), baseBeans);
        BaseInterface bean = factory.getBean(getBaseClass(), baseBeans.get(0).getId());
        Assert.assertNotNull(bean, "Bean should also be retrievable via ID.");
        Content content = factory.getBean(bean.getId());
        Assert.assertEquals(bean, content, "Untyped content should result in the same bean.");
        String stringValue = bean.toString();
        int idx = stringValue.lastIndexOf('/');
        if (idx>0) {
            idx++;
            stringValue = stringValue.substring(idx);
        } // if
        Assert.assertEquals(stringValue.substring(0, 9), "BaseClass", "Unexpected toString() implementation.");
        Assert.assertEquals(subBeans.get(0).compareTo(bean), 17, "Unexpected compareTo() implementation.");
        Content subBean = factory.getBean(subBeans.get(0).getId());
        Assert.assertEquals(subBeans.get(0), subBean, "Directly fetched bean should be the one from the list.");
        LOG.info("test04Components() completed.");
    } // test04Components()


    @Test(priority = 5)
    public void test05Query() throws Exception {
        LOG.info("test05Query() start.");
        MutableBeanFactory<M, Q> factory = getMutableBeanFactory();
        Assert.assertNotNull(factory, "Need factory for beans.");
        LOG.info("test05Query() obtaining instances of base class.");
        List<? extends BaseInterface> allBeans = factory.listBeans(factory.createQuery(getBaseClass(), null));
        LOG.info("test05Query() obtained instances of base class.");
        Assert.assertTrue(allBeans.size()>0, "We have prepared a fixed number of beans.");
        LOG.info("test05Query() completed.");
    } // test05Query()


    @Test(priority = 6)
    public void test06Filter() throws Exception {
        LOG.info("test06Filter() start.");
        MutableBeanFactory<M, Q> factory = getMutableBeanFactory();
        Assert.assertNotNull(factory, "Need factory for beans.");
        String filter = factory.getFilterQuery(getBaseClass(), "title", "filter");
        LOG.info("test06Filter() end");
        Assert.assertNotNull(filter, "There should be some filter query.");
        List<? extends BaseInterface> filteredBeans = factory.listBeansOfExactClass(getBaseClass(), filter, null, true);
        Assert.assertEquals(filteredBeans.size(), 1, "Unexpected contents for filter query.");
        LOG.info("test06Filter() completed.");
    } // test06Filter()


    @Test(priority = 7)
    public void test07Code() throws Exception {
        LOG.info("test07Code() start.");
        MutableBeanFactory<M, Q> beanFactory = getMutableBeanFactory();
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        Map<Class<? extends Content>, List<Class<? extends Content>>> classesMap = beanFactory.getImplementingClassesMap();
        Assert.assertNotNull(classesMap, "We have a classes map.");
        Assert.assertNotNull(classesMap.get(MutableCode.class), "We have a code class.");
        List<Class<MutableCode>> codeClasses = beanFactory.getImplementingClasses(MutableCode.class);
        Assert.assertEquals(codeClasses.size(), 1, "We have one code class.");
        Class<MutableCode> codeClass = codeClasses.get(0);
        List<MutableCode> codes = beanFactory.listBeans(codeClass, null);
        Assert.assertTrue(codes.isEmpty(), "We have no code instances.");
        MutableCode codeResource = beanFactory.createBean(codeClass);
        codeResource.setAnnotation("screen");
        codeResource.setMimeType("text/css");
        String emptyCss = "// Empty css";
        codeResource.setCode(emptyCss.toCharArray());
        codeResource.setModificationTime(System.currentTimeMillis());
        byte[] buffer = new byte[12];
        codeResource.getStream().read(buffer);
        String s = new String(buffer, "UTF-8");
        Assert.assertEquals(s, emptyCss, "Strange value from code stream.");
        beanFactory.persist(codeResource);
        codes = beanFactory.listBeans(codeClass, null);
        Assert.assertEquals(codes.size(), 1, "We have one code instance.");
        Assert.assertEquals(codes.get(0).compareTo(codes.get(0)), 0, "Code must be equal to itself.");
        Assert.assertEquals(codes.get(0).getCodeText(), emptyCss, "Code text must match value set up above.");
        Assert.assertEquals(codes.get(0).getSize(), emptyCss.length(), "Code size must match.");
        // Trigger groovy compiler
        codeResource = beanFactory.createBean(codeClass);
        codeResource.setAnnotation("org.tangram.example.Test");
        codeResource.setMimeType("application/x-groovy");
        codeResource.setCode(CLASS_CODE.toCharArray());
        beanFactory.persist(codeResource);
        codes = beanFactory.listBeans(codeClass, null);
        Assert.assertEquals(codes.size(), 2, "We have one code instance.");
        TransientCode bean = new TransientCode("a", "m", "TransientCode:42", "", System.currentTimeMillis());
        try {
            bean = beanFactory.getBean(TransientCode.class, codeResource.getId());
        } catch (Exception e) {
            LOG.info("test07Code() exception occured - only relevant for ebean and objectify AFAIK", e);
        } // try/catch
        Assert.assertNull(bean, "Despite the correct ID there should have been no code result.");
        LOG.info("test07Code() completed.");
    } // test07Code()


    @Test(priority = 8)
    public void test08ObtainCode() throws Exception {
        LOG.info("test08ObtainCode() start.");
        ClassRepository repository = getInstance(ClassRepository.class);
        Assert.assertNotNull(repository, "Could not find class repository.");
        Map<String, Class<Object>> annotatedClasses = repository.getAnnotated(Named.class);
        Assert.assertNotNull(annotatedClasses, "Could not find annotated classes.");
        Assert.assertEquals(annotatedClasses.size(), 1, "Expected one annotated class.");
        byte[] classBytes = repository.getBytes("org.tangram.example.Test");
        Assert.assertNotNull(classBytes, "Could not find class.");
        Assert.assertEquals(classBytes.length, 2601, "Unexpected number of bytes for class found.");
        repository.overrideClass("org.tangram.example.Test", classBytes);
        byte[] emptyClassBytes = repository.getBytes("org.tangram.example.Test");
        Assert.assertNotNull(emptyClassBytes, "Could not find class.");
        Assert.assertEquals(emptyClassBytes.length, 2601, "Unexpected number of bytes for class found.");
        Map<String, String> errors = repository.getCompilationErrors();
        Assert.assertEquals(errors.size(), 0, "Expected no compilation errors.");
    } // test08ObtainCode()


    @Test(priority = 9)
    public void test09DeleteComponents() throws Exception {
        LOG.info("test09DeleteComponents() start.");
        MutableBeanFactory<M, Q> beanFactory = getMutableBeanFactory();
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        List<? extends BaseInterface> allBeans = beanFactory.listBeans(getBaseClass());
        Assert.assertEquals(allBeans.size(), 2, "There should be the excepted number of beans before deletion.");
        List<? extends Content> subBeans = beanFactory.listBeans(SubInterface.class);
        int size = subBeans.size();
        Assert.assertEquals(size, 1, "We expected to have exactly one bean of type SubInterface.");
        if (size>0) {
            beanFactory.beginTransaction();
            boolean result = beanFactory.delete(subBeans.get(0));
            Assert.assertTrue(result, "Item should have been deleted.");
        } // if
        allBeans = beanFactory.listBeans(getBaseClass());
        Assert.assertEquals(allBeans.size(), 1, "There should be less beans after deleting one item.");
    } // test09DeleteComponents()


    /**
     * Perform an import via the tool handler.
     * Must be done after the wiping of the Database for some storage backends.
     */
    @Test(priority = 10)
    public void test10ContentImport() {
        LOG.info("test10ContentImport() start.");
        try {
            ToolHandler toolHandler = getInstance(ToolHandler.class);
            InputStream content = this.getClass().getResourceAsStream("/content-for-test-import.xml");
            Assert.assertNotNull(content, "Expected to find content for import test.");
            Reader reader = new InputStreamReader(content);
            toolHandler.contentImport(reader);
            MutableBeanFactory<M, Q> factory = getMutableBeanFactory();
            LOG.info("test10ContentImport() listing all beans");
            List<Content> beans = factory.listBeans(Content.class);
            Assert.assertNotNull(beans, "Expected to find at least some beans.");
            Assert.assertEquals(beans.size(), 5, "Expected to find a defined number of beans.");
            String filter = factory.getFilterQuery(getBaseClass(), "title", "handleForTest");
            Assert.assertNotNull(filter, "There should be some filter query");
            List<? extends BaseInterface> baseBeans = factory.listBeans(BaseInterface.class, filter, null, true);
            Assert.assertEquals(baseBeans.size(), 1, "Unexpected base interface instances.");
            filter = factory.getFilterQuery(getBaseClass(), "title", "subHandleForTest");
            Assert.assertNotNull(filter, "There should be some filter query");
            List<? extends SubInterface> subBeans = factory.listBeans(SubInterface.class, filter, null, true);
            Assert.assertEquals(subBeans.size(), 1, "Unexpected sub interface instances.");
            SubInterface subBean = subBeans.get(0);
            Assert.assertEquals(subBean.getTitle(), "subHandleForTest", "Title doesn't match the filtered one.");
            // TODO: EBean doesn't like this for some reasong
            // List<? extends BaseInterface> peers = subBean.getPeers();
            // Assert.assertNotNull(peers, "Missing content references.");
            // Assert.assertEquals(peers.size(), 1, "Unexpected number of references contents.");
            // Assert.assertEquals(peers.get(0), baseBeans.get(0), "Unexpected referenced content.");
        } catch (Exception|Error e) {
            LOG.error("test10ContentImport()", e);
            Assert.fail(e.getClass().getSimpleName()+":"+e.getMessage());
        }
        LOG.info("test10ContentImport() completed.");
    } // test10ContentImport()

} // BaseContentTest
