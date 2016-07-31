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
package org.tangram.morphia.test;

import dinistiq.Dinistiq;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mongodb.morphia.Datastore;
import org.tangram.morphia.Code;
import org.tangram.morphia.test.content.BaseClass;
import org.tangram.morphia.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.Assert;
import org.testng.annotations.Test;


public class MorphiaContentTest extends BaseContentTest<Datastore> {

    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, create ? getBeansForContentCreate() : getBeansForContentCheck());
        Assert.assertNotNull(dinistiq, "Need dinistiq instance for execute tests.");
        return dinistiq.findBean(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<Datastore> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface createSubBean(MutableBeanFactory<Datastore> beanFactory) throws Exception {
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
    protected String getCondition() {
        return "where subtitle='great'";
    }


    @Override
    protected void setPeers(BaseInterface base, SubInterface peer) {
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


    @Test(priority = 10)
    public void test10WipeContent() throws Exception {
        MutableBeanFactory beanFactory = getInstance(MutableBeanFactory.class, true);
        Assert.assertNotNull(beanFactory, "Need factory for beans.");
        Object manager = beanFactory.getManager();
        Assert.assertNotNull(manager, "The factory should have an underlying manager instance.");
        Datastore datastore = (Datastore)manager;
        datastore.getCollection(Code.class).drop();
        datastore.getCollection(BaseClass.class).drop();
        datastore.getCollection(SubClass.class).drop();
    } // test10WipeContent()

} // MorphiaContentTest
