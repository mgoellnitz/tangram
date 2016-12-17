/**
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.objectify.test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import com.googlecode.objectify.cmd.Query;
import dinistiq.Dinistiq;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.tangram.objectify.ObjectifyBeanFactory;
import org.tangram.objectify.test.content.BaseClass;
import org.tangram.objectify.test.content.SubClass;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ObjectifyContentTest extends BaseContentTest<Objectify, Query<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectifyContentTest.class);

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private Closeable session;

    private Dinistiq dinistiq;


    @BeforeClass
    public void beforeClass() throws Exception {
        LOG.info("beforeClass()");
        ObjectifyService.setFactory(new ObjectifyFactory());
        this.session = ObjectifyService.begin();
        this.helper.setUp();
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        dinistiq = new Dinistiq(packages, getBeansForContentCheck());
        ObjectifyBeanFactory factory = dinistiq.findBean(ObjectifyBeanFactory.class);
        factory.setAdditionalClasses(Collections.emptyList());
    } // beforeClass()


    @AfterClass
    public void tearDown() throws Exception {
        LOG.info("tearDown()");
        AsyncCacheFilter.complete();
        this.session.close();
        this.helper.tearDown();
    }


    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
        Assert.assertNotNull(dinistiq, "Need dinistiq instance for execute tests.");
        return dinistiq.findBean(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<Objectify, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface<Query<?>> createSubBean(MutableBeanFactory<Objectify, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "com.googlecode";
    }


    @Override
    protected String getCondition(MutableBeanFactory<Objectify, Query<?>> beanFactory) {
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


    @Test
    public void testDummy() {
        Assert.assertEquals(getNumberOfClasses(), 3, "Unexpected number of classes in dummy test.");
    }

} // ObjectifyContentTest
