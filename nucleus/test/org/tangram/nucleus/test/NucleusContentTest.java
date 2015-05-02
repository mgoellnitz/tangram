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
package org.tangram.nucleus.test;

import dinistiq.Dinistiq;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.tangram.nucleus.NucleusContent;
import org.tangram.nucleus.test.content.BaseClass;
import org.tangram.nucleus.test.content.SubClass;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test various content related methods.
 *
 * We need a test order to have enhanced classes first, then create some content, and the test this content in
 * a separate test "session".
 */
public class NucleusContentTest extends BaseContentTest {

    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages, create ? getBeansForContentCreate() : getBeansForContentCheck());
        Assert.assertNotNull(dinistiq, "need test dinistiq instance");
        return dinistiq.findBean(type);
    } // getInstance()


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
        return 16;
    }


    @Override
    protected int getNumberOfClasses() {
        return 4;
    }


    @Test(priority = 0)
    public void test0IsEnhanced() {
        Assert.assertTrue(checkMethodPrefixOccurs(NucleusContent.class.getMethods(), "jdo"), "Classes not enhanced");
    } // test1IsEnhanced()


    @Test
    public void testStringConversion() throws Exception {
        SubClass subBean = new SubClass();
        String testString = "justateststringtouseforconversiontesting";
        Assert.assertEquals(subBean.checkConversion(testString), testString, "Nucleus specific conversion methods failed");
        Assert.assertNull(subBean.checkConversion(null), "Nucleus specific conversion methods failed");
    } // testStringConversion()

} // NucleusContentTest
