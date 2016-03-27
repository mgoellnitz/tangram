/*
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
package org.tangram.guicy.test;

import javax.servlet.ServletContextEvent;
import org.springframework.mock.web.MockServletContext;
import org.tangram.guicy.DefaultTangramContextListener;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the context listener.
 */
public class DefaultTangramContextListenerTest {

    public static boolean instanciated = false;


    @Test
    public void testDefaultTangramContextListener() {
        DefaultTangramContextListener contextListener = new DefaultTangramContextListener();
        MockServletContext servletContext = new MockServletContext();
        ServletContextEvent event = new ServletContextEvent(servletContext);
        servletContext.setInitParameter(DefaultTangramContextListener.SERVLET_MODULE_CLASS, MockServletModule.class.getName());
        contextListener.contextInitialized(event);
        Assert.assertTrue(instanciated, "Mock servlet module should have been instanciated.");
    } // testDefaultTangramContextListener()

} // DefaultTangramContextListenerTest
