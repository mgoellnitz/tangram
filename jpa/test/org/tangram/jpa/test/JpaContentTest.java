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
package org.tangram.jpa.test;

import dinistiq.Dinistiq;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.tangram.jpa.test.content.BaseClass;
import org.tangram.jpa.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JpaContentTest extends BaseContentTest {

    static {
        org.apache.openjpa.enhance.InstrumentationFactory.setDynamicallyInstallAgent(false);
    }


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface createSubBean(MutableBeanFactory beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected void setPeers(BaseInterface base, SubInterface peer) {
        List<BaseClass> peers = new ArrayList<>();
        peers.add((BaseClass) peer);
        ((BaseClass) base).setPeers(peers);
    } // setPeers()


    @Override
    protected int getNumberOfAllClasses() {
        return 12;
    }


    @Override
    protected int getNumberOfClasses() {
        return 3;
    }


    @Test
    public void test4CreateTestContent() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, getBeansForContentCreate());
        Assert.assertNotNull("need test dinistiq instance", dinistiq);
        MutableBeanFactory beanFactory = dinistiq.findBean(MutableBeanFactory.class);
        Assert.assertNotNull("need factory for beans", beanFactory);
        // Assert.assertEquals("have twelve classes and interfaces available", 12, beanFactory.getAllClasses().size());
        // Assert.assertEquals("implementing classes", Collections.emptySet(), beanFactory.getAllClasses());
        Assert.assertEquals("have three non abstract model classes", 3, beanFactory.getClasses().size());
    } // test4CreateTestContent()

} // JpaContentTest
