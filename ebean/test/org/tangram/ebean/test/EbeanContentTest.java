/**
 *
 * Copyright 2013-2019 Martin Goellnitz
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

import dinistiq.Dinistiq;
import io.ebean.EbeanServer;
import io.ebean.Query;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.ebean.Code;
import org.tangram.ebean.test.content.BaseClass;
import org.tangram.ebean.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class EbeanContentTest extends BaseContentTest<EbeanServer, Query<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(EbeanContentTest.class);

    private Dinistiq dinistiq;


    @BeforeClass
    protected void beforeClass() throws Exception {
        LOG.info("beforeClass()");
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        dinistiq = new Dinistiq(packages, getBeansForScope());
        Assert.assertNotNull(dinistiq, "Need dinistiq instance to execute tests.");
    } // getInstance()


    @Override
    protected <T extends Object> T getInstance(Class<T> type) throws Exception {
        return dinistiq.findBean(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<EbeanServer, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface<Query<?>> createSubBean(MutableBeanFactory<EbeanServer, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "io.ebean";
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
    public void test01IsEnhanced() {
        LOG.info("test01IsEnhanced() start.");
        Method[] methods = Code.class.getMethods();
        Assert.assertTrue(BaseContentTest.checkMethodPrefixOccurs(methods, "_ebean"), "Classes were not enhanced.");
        LOG.info("test01IsEnhanced() completed.");
    } // test01IsEnhanced()

} // EbeanContentTest
