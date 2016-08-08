/**
 *
 * Copyright 2014-2016 Martin Goellnitz
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

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.tangram.guicy.TangramServletModule;
import org.tangram.guicy.postconstruct.PostConstructModule;
import org.tangram.jpa.protection.PasswordProtection;
import org.tangram.jpa.test.content.BaseClass;
import org.tangram.jpa.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.annotations.Test;


public class JpaContentTest extends BaseContentTest<EntityManager> {

    static {
        org.apache.openjpa.enhance.InstrumentationFactory.setDynamicallyInstallAgent(false);
    }


    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
        Injector injector = Guice.createInjector(new PostConstructModule(), new TangramServletModule());
        return injector.getInstance(type);
    } // getInstance()


    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory<EntityManager> beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface createSubBean(MutableBeanFactory<EntityManager> beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected String getManagerPrefix() {
        return "org.apache.openjpa";
    }


    @Override
    protected String getCondition() {
        return "select x from SubClass x where x.subtitle = 'great'";
    }


    @Override
    protected void setPeers(BaseInterface base, SubInterface peer) {
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


    @Test
    public void testPasswordProtection() {
        PasswordProtection passwordProtection = new PasswordProtection();
        passwordProtection.setLogin(TESTUSER);
        passwordProtection.setPassword(TESTPASSWORD);
        passwordProtection.setProtectionKey("mock password protection");
        passwordProtection.setProtectedContents(Collections.emptyList());
        checkSimplePasswordProtection(passwordProtection);
    } // testPasswordProtection()

} // JpaContentTest
