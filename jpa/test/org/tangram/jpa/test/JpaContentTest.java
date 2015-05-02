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
package org.tangram.jpa.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;
import java.util.ArrayList;
import java.util.List;
import org.tangram.guicy.TangramServletModule;
import org.tangram.jpa.test.content.BaseClass;
import org.tangram.jpa.test.content.SubClass;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.testng.Assert;
import org.testng.annotations.Test;


public class JpaContentTest extends BaseContentTest {

    static {
        org.apache.openjpa.enhance.InstrumentationFactory.setDynamicallyInstallAgent(false);
    }


    @Override
    protected <T extends Object> T getInstance(Class<T> type, boolean create) throws Exception {
//        final org.springframework.mock.web.MockServletContext context = new org.springframework.mock.web.MockServletContext() {
//
//        };
//        TangramServletModule servletModule = new TangramServletModule() {
//
//            @Override
//            public ServletContext getServletContext() {
//                return context;
//            }
//
//        };

        GuiceFilter c;
        Injector injector = Guice.createInjector(new CloseableModule(), new Jsr250Module(), new TangramServletModule());
        return injector.getInstance(type);
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
        return 15;
    }


    @Override
    protected int getNumberOfClasses() {
        return 4;
    }


    /**
     * Dummy test so that this test class contains at least one test.
     */
    @Test
    public void testNothing() {
        Assert.assertTrue(true);
    } // testNothing()

} // JpaContentTest
