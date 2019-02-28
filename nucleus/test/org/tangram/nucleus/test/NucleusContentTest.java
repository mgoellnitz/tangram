/**
 *
 * Copyright 2014-2019 Martin Goellnitz
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.tangram.nucleus.NucleusContent;
import org.tangram.nucleus.protection.PasswordProtection;
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
public class NucleusContentTest extends BaseContentTest<PersistenceManager, Query<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(NucleusContentTest.class);


    @Override
    protected <T extends Object> T getInstance(Class<T> type) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:tangram/*.xml", "tangram/*.xml"});
        return context.getBean(type);
    } // getInstance()


    @Override
    protected SubInterface<Query<?>> createSubBean(MutableBeanFactory<PersistenceManager, Query<?>> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "org.datanucleus";
    }


    @Override
    protected String getCondition(MutableBeanFactory<PersistenceManager, Query<?>> beanFactory) {
        return "subtitle == 'great'";
    } // getCondition()


    @Override
    protected void setPeers(BaseInterface base, BaseInterface peer) {
        List<BaseClass> peers = new ArrayList<>();
        peers.add((BaseClass) peer);
        ((BaseClass) base).setPeers(peers);
    } // setPeers()


    @Override
    protected int getNumberOfAllClasses() {
        return 8;
    }


    @Override
    protected int getNumberOfClasses() {
        return 4;
    }


    @Test(priority = 1)
    public void test01IsEnhanced() {
        LOG.info("test01IsEnhanced() start.");
        Assert.assertTrue(BaseContentTest.checkMethodPrefixOccurs(NucleusContent.class.getMethods(), "dn"), "Classes were not enhanced.");
        LOG.info("test01IsEnhanced() completed.");
    } // test01IsEnhanced()


    @Test
    public void testStringConversion() throws Exception {
        SubClass subBean = new SubClass();
        String testString = "justateststringtouseforconversiontesting";
        Assert.assertEquals(subBean.checkConversion(testString), testString, "Nucleus specific conversion methods failed.");
        Assert.assertNull(subBean.checkConversion(null), "Nucleus specific conversion methods failed.");
    } // testStringConversion()


    @Test
    public void testPasswordProtection() {
        PasswordProtection passwordProtection = new PasswordProtection();
        passwordProtection.setLogin(TESTUSER);
        passwordProtection.setPassword(TESTPASSWORD);
        passwordProtection.setProtectionKey("mock password protection");
        passwordProtection.setProtectedContents(Collections.emptyList());
        checkSimplePasswordProtection(passwordProtection);
    } // testPasswordProtection()

} // NucleusContentTest
