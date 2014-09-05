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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.tangram.content.Content;
import org.tangram.mutable.MutableBeanFactory;
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
        return Collections.emptyMap();
    }


    protected Map<String, Object> getBeansForContentCheck() {
        return Collections.emptyMap();
    }


    protected abstract BaseInterface createBaseBean(MutableBeanFactory beanFactory) throws Exception;


    protected abstract SubInterface createSubBean(MutableBeanFactory beanFactory) throws Exception;


    protected abstract Class<? extends BaseInterface> getBaseClass();


    protected abstract void setPeers(BaseInterface base, SubInterface sub);


    @Test
    public void test1CreateTestContent() throws Exception {
        Set<String> packages = new HashSet<String>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCreate());
        Assert.assertNotNull("need test dinistiq instance", dinistiq);
        MutableBeanFactory beanFactory = dinistiq.findTypedBean(MutableBeanFactory.class);
        Assert.assertNotNull("need factory for beans", beanFactory);
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
        Set<String> packages = new HashSet<String>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCheck());
        Assert.assertNotNull("need test dinistiq instance", dinistiq);
        MutableBeanFactory beanFactory = dinistiq.findTypedBean(MutableBeanFactory.class);
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

} // BaseContentTest
