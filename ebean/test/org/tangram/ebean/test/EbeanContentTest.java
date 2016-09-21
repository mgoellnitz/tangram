/**
 *
 * Copyright 2013-2016 Martin Goellnitz
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
package org.tangram.ebean.test;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import dinistiq.Dinistiq;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tangram.ebean.Code;
import org.tangram.ebean.test.content.BaseClass;
import org.tangram.ebean.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.Assert;
import org.testng.annotations.Test;


public class EbeanContentTest extends BaseContentTest<EbeanServer, Query<?>> {

    @Override
    protected Map<String, Object> getBeansForContentCreate() {
        Map<String, Object> result = super.getBeansForContentCreate();
        result.put("ebeanDdlGenerate", Boolean.TRUE);
        result.put("ebeanDdlRun", Boolean.TRUE);
        return result;
    }


    @Override
    protected Map<String, Object> getBeansForContentCheck() {
        Map<String, Object> result = super.getBeansForContentCheck();
        result.put("ebeanDdlGenerate", Boolean.FALSE);
        result.put("ebeanDdlRun", Boolean.FALSE);
        return result;
    }


    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, create ? getBeansForContentCreate() : getBeansForContentCheck());
        Assert.assertNotNull(dinistiq, "Need dinistiq instance for execute tests.");
        return dinistiq.findBean(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<EbeanServer, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface createSubBean(MutableBeanFactory<EbeanServer, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "com.avaje.ebean";
    }


    @Override
    protected String getCondition(MutableBeanFactory<EbeanServer, Query<?>> beanFactory) {
        return "subtitle='great'";
    } // getCondition()


    @Override
    protected void setPeers(BaseInterface base, BaseInterface peer) {
        List<BaseClass> peers = new ArrayList<>();
        peers.add((BaseClass) peer);
        ((BaseClass) base).setPeers(peers);
    } // setPeers()


    @Override
    protected int getNumberOfAllClasses() {
        return 6;
    }


    @Override
    protected int getNumberOfClasses() {
        return 3;
    }


    /**
     * From time to time we ran into the problem that classes didn't get enhanced correctly
     */
    @Test(priority = 1)
    public void test0IsEnhanced() {
        Method[] methods = Code.class.getMethods();
        Assert.assertTrue(BaseContentTest.checkMethodPrefixOccurs(methods, "_ebean"), "Classes were not enhanced.");
    } // test0IsEnhanced()

} // EbeanContentTest
