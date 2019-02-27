/**
 *
 * Copyright 2016-2019 Martin Goellnitz
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
package org.tangram.test.morphia;

import dinistiq.Dinistiq;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.morphia.Code;
import org.tangram.morphia.MorphiaBeanFactory;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.tangram.test.morphia.content.BaseClass;
import org.tangram.test.morphia.content.SubClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class MorphiaContentTest extends BaseContentTest<Datastore, Query<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(MorphiaContentTest.class);

    private Dinistiq dinistiq;


    @BeforeClass
    protected void beforeClass() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        dinistiq = new Dinistiq(packages, getBeansForScope());
        MorphiaBeanFactory factory = dinistiq.findBean(MorphiaBeanFactory.class);
        Set<Class<? extends Content>> additionalClasses = new HashSet<>();
        additionalClasses.add(AdditionalClass.class);
        factory.setAdditionalClasses(additionalClasses);
        Assert.assertNotNull(dinistiq, "Need dinistiq instance to execute tests.");
        super.beforeClass();
    } // getInstance()


    @Override
    protected <T extends Object> T getInstance(Class<T> type) throws Exception {
        return dinistiq.findBean(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<Datastore, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface<Query<?>> createSubBean(MutableBeanFactory<Datastore, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "org.mongodb.morphia";
    }


    @Override
    protected String getCondition(MutableBeanFactory<Datastore, Query<?>> beanFactory) {
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
        return 7;
    }


    @Override
    protected int getNumberOfClasses() {
        return 4;
    }


    @Test(priority = 2)
    public void test02CheckClasses() throws Exception {
        LOG.info("test02CheckClasses() start.");
        MutableBeanFactory<Datastore, Query<?>> beanFactory = getMutableBeanFactory();
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        List<? extends Class<? extends BaseInterface>> baseClasses = beanFactory.getImplementingClasses(getBaseClass());
        Assert.assertEquals(baseClasses.size(), 2, "Missing implementations for base class.");
        List<Class<SubInterface>> subClasses = beanFactory.getImplementingClasses(SubInterface.class);
        Assert.assertEquals(subClasses.size(), 1, "Missing implementation for sub class.");
        LOG.info("test02CheckClasses() completed.");
    } // test02CheckClasses()


    @Test(priority = 90)
    public void test90WipeContent() throws Exception {
        LOG.info("test90WipeContent() start.");
        MutableBeanFactory<Datastore, Query<?>> beanFactory = getMutableBeanFactory();
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        Object manager = beanFactory.getManager();
        Assert.assertNotNull(manager, "The factory should have an underlying manager instance.");
        Datastore datastore = (Datastore) manager;
        datastore.getCollection(Code.class).drop();
        datastore.getCollection(BaseClass.class).drop();
        datastore.getCollection(SubClass.class).drop();
        LOG.info("test90WipeContent() completed.");
    } // test90WipeContent()

} // MorphiaContentTest
