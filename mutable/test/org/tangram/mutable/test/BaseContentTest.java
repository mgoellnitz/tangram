/**
 *
 * Copyright 2014-2015 Martin Goellnitz
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import org.tangram.content.Content;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableCode;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.Assert;
import org.testng.annotations.Test;


public abstract class BaseContentTest {

    private static final String CLASS_CODE = "package org.tangram.example; import javax.inject.Named; @Named public class Test { public String s; }";


    /**
     * Check if any of the method's names starts with a given prefix.
     *
     * All byte-code transformers create such schematic methods.
     *
     * @param methods array of methods to check names
     * @param prefix name prefix at least one of the method's names should have
     * @return true if any of the method's names starts with the given prefix.
     */
    protected boolean checkMethodPrefixOccurs(Method[] methods, String prefix) {
        boolean flag = false;
        for (Method method : methods) {
            if (method.getName().startsWith(prefix)) {
                flag = true;
            } // if
        } // for
        return flag;
    } // checkMethodPrefixOccurs()


    protected Map<String, Object> getBeansForContentCreate() {
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


    protected Map<String, Object> getBeansForContentCheck() {
        return getBeansForContentCreate();
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
    protected abstract <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception;


    protected abstract BaseInterface createBaseBean(MutableBeanFactory beanFactory) throws Exception;


    protected abstract SubInterface createSubBean(MutableBeanFactory beanFactory) throws Exception;


    protected abstract Class<? extends BaseInterface> getBaseClass();


    protected abstract void setPeers(BaseInterface base, SubInterface sub);


    protected abstract int getNumberOfAllClasses();


    /**
     * Return expetect number of available classes in our test configuration.
     *
     * @return Number of classes excluding interfaces and abstract classes
     */
    protected abstract int getNumberOfClasses();


    @Test(priority = 1)
    public void test1CreateTestContent() throws Exception {
        MutableBeanFactory beanFactory = getInstance(MutableBeanFactory.class, true);
        Assert.assertNotNull(beanFactory, "need factory for beans");
        int numberOfAllClasses = getNumberOfAllClasses();
        // Assert.assertEquals(beanFactory.getAllClasses().toString(), "[interface org.tangram.feature.protection.Protection, interface org.tangram.feature.protection.ProtectedContent, interface org.tangram.mutable.test.content.SubInterface, interface org.tangram.mutable.MutableCode, interface org.tangram.content.CodeResource, class org.tangram.ebean.EContent, interface org.tangram.mutable.test.content.BaseInterface, class org.tangram.content.TransientCode, class org.tangram.ebean.Code, interface org.tangram.content.Content, class org.tangram.ebean.test.content.BaseClass, class org.tangram.ebean.test.content.SubClass]", "List of classes as strings");
        Assert.assertEquals(beanFactory.getAllClasses().size(), numberOfAllClasses, "have "+numberOfAllClasses+" classes and interfaces available");
        int numberOfClasses = getNumberOfClasses();
        Assert.assertEquals(beanFactory.getClasses().size(), numberOfClasses, "have "+numberOfClasses+" non abstract model classes");
        SubInterface beanA = createSubBean(beanFactory);
        Assert.assertNotNull(beanA, "could not create bean");
        beanFactory.persist(beanA);
        beanFactory.commitTransaction();
        BaseInterface beanB = createBaseBean(beanFactory);
        Assert.assertNotNull(beanB, "could not create beanB");
        setPeers(beanB, beanA);
        beanFactory.persist(beanB);
        beanFactory.commitTransaction();
    } // test1CreateTestContent()


    @Test(priority = 2)
    public void test2Components() throws Exception {
        MutableBeanFactory beanFactory = getInstance(MutableBeanFactory.class, false);
        Assert.assertNotNull(beanFactory, "need factory for beans");
        List<? extends BaseInterface> allBeans = beanFactory.listBeans(getBaseClass());
        Assert.assertEquals(allBeans.size(), 2, "we have prepared a fixed number of beans");
        List<SubInterface> subBeans = beanFactory.listBeans(SubInterface.class);
        Assert.assertEquals(subBeans.size(), 1, "we have prepared a fixed number of sub beans");
        // this contains is necessary due to possible subclassing in some of the APIs
        Assert.assertTrue(subBeans.get(0).getClass().getSimpleName().contains("SubClass"), "peer of base beans is a sub bean");
        List<? extends Content> baseBeans = beanFactory.listBeansOfExactClass(getBaseClass());
        Assert.assertEquals(baseBeans.size(), 1, "we have prepared a fixed number of base beans");
    } // test2Components()


    @Test(priority = 3)
    public void test3Code() throws Exception {
        MutableBeanFactory beanFactory = getInstance(MutableBeanFactory.class, false);
        Assert.assertNotNull(beanFactory, "need factory for beans");
        Map<Class<? extends Content>, List<Class<? extends Content>>> classesMap = beanFactory.getImplementingClassesMap();
        Assert.assertNotNull(classesMap, "we have a classes map");
        // Assert.assertEquals("implementing classes", Collections.emptySet(), classesMap.keySet());
        Assert.assertNotNull(classesMap.get(MutableCode.class), "we have a code class");
        List<Class<MutableCode>> codeClasses = beanFactory.getImplementingClasses(MutableCode.class);
        Assert.assertEquals(codeClasses.size(), 1, "We have one code class");
        Class<MutableCode> codeClass = codeClasses.get(0);
        List<MutableCode> codes = beanFactory.listBeans(codeClass, null);
        Assert.assertTrue(codes.isEmpty(), "We have no code instances");
        MutableCode codeResource = beanFactory.createBean(codeClass);
        codeResource.setAnnotation("screen");
        codeResource.setMimeType("text/css");
        codeResource.setCode("// Empty css".toCharArray());
        beanFactory.persist(codeResource);
        codes = beanFactory.listBeans(codeClass, null);
        Assert.assertEquals(codes.size(), 1, "We have one code instance");
        Assert.assertEquals(codes.get(0).compareTo(codes.get(0)), 0, "Code must be equal to itself");
        Assert.assertEquals(codes.get(0).getCodeText(), "// Empty css", "Code text must match value set up above");
        Assert.assertEquals(codes.get(0).getSize(), "// Empty css".length(), "Code size must match");
        // Trigger groovy compiler
        codeResource = beanFactory.createBean(codeClass);
        codeResource.setAnnotation("org.tangram.example.Test");
        codeResource.setMimeType("application/x-groovy");
        codeResource.setCode(CLASS_CODE.toCharArray());
        beanFactory.persist(codeResource);
        codes = beanFactory.listBeans(codeClass, null);
        Assert.assertEquals(codes.size(), 2, "We have one code instance");
    } // test3Code()


    @Test(priority = 4)
    public void test4ObtainCode() throws Exception {
        ClassRepository repository = getInstance(ClassRepository.class, false);
        Assert.assertNotNull(repository, "Could not find class repository");
        Map<String, Class<Object>> annotatedClasses = repository.getAnnotated(Named.class);
        Assert.assertNotNull(annotatedClasses, "Could not find annotated classes");
        Assert.assertEquals(annotatedClasses.size(), 1, "Expected one annotated class");
        byte[] classBytes = repository.getBytes("org.tangram.example.Test");
        Assert.assertNotNull(classBytes, "Could not find class");
        Assert.assertEquals(classBytes.length, 2372, "Unexpected number of bytes for class found");
        repository.overrideClass("org.tangram.example.Test", classBytes);
        byte[] emptyClassBytes = repository.getBytes("org.tangram.example.Test");
        Assert.assertNotNull(emptyClassBytes, "Could not find class");
        Assert.assertEquals(emptyClassBytes.length, 2372, "Unexpected number of bytes for class found");
        Map<String, String> errors = repository.getCompilationErrors();
        Assert.assertEquals(errors.size(), 0, "Expected no compilation errors");
    } // test4ObtainCode()

} // BaseContentTest
