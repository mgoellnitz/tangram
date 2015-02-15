/**
 *
 * Copyright 2014 Martin Goellnitz
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

import dinistiq.Dinistiq;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.tangram.content.Content;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableCode;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseContentTest {

    /**
     * Check if any of the method's names starts with a given prefix.
     *
     * All byte-code transformers create such schematic methods.
     *
     * @param methods
     * @param prefix
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


    protected abstract BaseInterface createBaseBean(MutableBeanFactory beanFactory) throws Exception;


    protected abstract SubInterface createSubBean(MutableBeanFactory beanFactory) throws Exception;


    protected abstract Class<? extends BaseInterface> getBaseClass();


    protected abstract void setPeers(BaseInterface base, SubInterface sub);


    protected abstract int getNumberOfAllClasses();


    /**
     * excluding interfafes and abstract classes.
     */
    protected abstract int getNumberOfClasses();


    @Test
    public void test1CreateTestContent() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCreate());
        Assert.assertNotNull("need test dinistiq instance", dinistiq);
        MutableBeanFactory beanFactory = dinistiq.findBean(MutableBeanFactory.class);
        Assert.assertNotNull("need factory for beans", beanFactory);
        int numberOfAllClasses = getNumberOfAllClasses();
        // Assert.assertEquals("List of classes as strings", "[interface org.tangram.feature.protection.Protection, interface org.tangram.feature.protection.ProtectedContent, interface org.tangram.mutable.test.content.SubInterface, interface org.tangram.mutable.MutableCode, interface org.tangram.content.CodeResource, class org.tangram.ebean.EContent, interface org.tangram.mutable.test.content.BaseInterface, class org.tangram.content.TransientCode, class org.tangram.ebean.Code, interface org.tangram.content.Content, class org.tangram.ebean.test.content.BaseClass, class org.tangram.ebean.test.content.SubClass]", beanFactory.getAllClasses().toString());
        Assert.assertEquals("have "+numberOfAllClasses+" classes and interfaces available", numberOfAllClasses, beanFactory.getAllClasses().size());
        int numberOfClasses = getNumberOfClasses();
        Assert.assertEquals("have "+numberOfClasses+" non abstract model classes", numberOfClasses, beanFactory.getClasses().size());
        SubInterface beanA = createSubBean(beanFactory);
        Assert.assertNotNull("could not create bean", beanA);
        beanFactory.persist(beanA);
        beanFactory.commitTransaction();
        BaseInterface beanB = createBaseBean(beanFactory);
        Assert.assertNotNull("could not create beanB", beanB);
        setPeers(beanB, beanA);
        beanFactory.persist(beanB);
        beanFactory.commitTransaction();
    } // test1CreateTestContent()


    @Test
    public void test2Components() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCheck());
        Assert.assertNotNull("need test dinistiq instance", dinistiq);
        MutableBeanFactory beanFactory = dinistiq.findBean(MutableBeanFactory.class);
        Assert.assertNotNull("need factory for beans", beanFactory);
        List<? extends BaseInterface> allBeans = beanFactory.listBeans(getBaseClass());
        Assert.assertEquals("we have prepared a fixed number of beans", 2, allBeans.size());
        List<SubInterface> subBeans = beanFactory.listBeans(SubInterface.class);
        Assert.assertEquals("we have prepared a fixed number of sub beans", 1, subBeans.size());
        // this contains is necessary due to possible subclassing in some of the APIs
        Assert.assertTrue("peer of base beans is a sub bean", subBeans.get(0).getClass().getSimpleName().contains("SubClass"));
        List<? extends Content> baseBeans = beanFactory.listBeansOfExactClass(getBaseClass());
        Assert.assertEquals("we have prepared a fixed number of base beans", 1, baseBeans.size());
    } // test2Components()


    @Test
    public void test3Code() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCheck());
        Assert.assertNotNull("need test dinistiq instance", dinistiq);
        MutableBeanFactory beanFactory = dinistiq.findBean(MutableBeanFactory.class);
        Assert.assertNotNull("need factory for beans", beanFactory);
        Map<Class<? extends Content>, List<Class<? extends Content>>> classesMap = beanFactory.getImplementingClassesMap();
        Assert.assertNotNull("we have a classes map", classesMap);
        // Assert.assertEquals("implementing classes", Collections.emptySet(), classesMap.keySet());
        Assert.assertNotNull("we have a code class", classesMap.get(MutableCode.class));
        List<Class<MutableCode>> codeClasses = beanFactory.getImplementingClasses(MutableCode.class);
        Assert.assertEquals("We have one code class", 1, codeClasses.size());
        Class<MutableCode> codeClass = codeClasses.get(0);
        List<MutableCode> codes = beanFactory.listBeans(codeClass, null);
        Assert.assertTrue("We have no code instances", codes.isEmpty());
        MutableCode codeResource = beanFactory.createBean(codeClass);
        codeResource.setAnnotation("screen");
        codeResource.setMimeType("text/css");
        codeResource.setCode("// Empty css".toCharArray());
        beanFactory.persist(codeResource);
        codes = beanFactory.listBeans(codeClass, null);
        Assert.assertEquals("We have one code instance", 1, codes.size());
        Assert.assertEquals("Code must be equal to itself", 0, codes.get(0).compareTo(codes.get(0)));
        Assert.assertEquals("Code text must match value set up above", "// Empty css", codes.get(0).getCodeText());
        Assert.assertEquals("Code size must match", "// Empty css".length(), codes.get(0).getSize());
        // Trigger groovy compiler
        codeResource = beanFactory.createBean(codeClass);
        codeResource.setAnnotation("org.tangram.example.Test");
        codeResource.setMimeType("application/x-groovy");
        codeResource.setCode("package org.tangram.example; import javax.inject.Named; @Named public class Test {}".toCharArray());
        beanFactory.persist(codeResource);
        codes = beanFactory.listBeans(codeClass, null);
        Assert.assertEquals("We have one code instance", 2, codes.size());
    } // test3Code()


    @Test
    public void test4ObtainCode() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCheck());
        ClassRepository repository = dinistiq.findBean(ClassRepository.class);
        Assert.assertNotNull("Could not find class repository", repository);
        Map<String, Class<Object>> annotatedClasses = repository.getAnnotated(Named.class);
        Assert.assertNotNull("Could not find annotated classes", annotatedClasses);
        Assert.assertEquals("Expected one annotated class", 1, annotatedClasses.size());
        byte[] classBytes = repository.getBytes("org.tangram.example.Test");
        Assert.assertNotNull("Could not find class", classBytes);
        Assert.assertEquals("Unexpected number of bytes for class found", 4869, classBytes.length);
        repository.overrideClass("org.tangram.example.Test", classBytes);
        byte[] emptyClassBytes = repository.getBytes("org.tangram.example.Test");
        Assert.assertNotNull("Could not find class", emptyClassBytes);
        Assert.assertEquals("Unexpected number of bytes for class found", 4869, emptyClassBytes.length);
        Map<String, String> errors = repository.getCompilationErrors();
        Assert.assertEquals("Expected no compilation errors", 0, errors.size());
    } // test4ObtainCode()

} // BaseContentTest
