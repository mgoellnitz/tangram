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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.tangram.Constants;
import org.tangram.guicy.TangramServletModule;
import org.tangram.guicy.postconstruct.PostConstructModule;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the Guicy servlet module.
 */
public class TangramServletModuleTest {

    /**
     * Since classes in test are protected we need this wrapper.
     */
    public class TestServletModule extends TangramServletModule {

        public void checkModule() {
            Object aggregator = getServletContext().getAttribute(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR);
            Assert.assertNotNull(aggregator, "We need a link factory aggregator.");
            Object viewSettins = getServletContext().getAttribute(Constants.ATTRIBUTE_VIEW_SETTINGS);
            Assert.assertNotNull(viewSettins, "We need view settings.");
            Object statistics = getServletContext().getAttribute(Constants.ATTRIBUTE_STATISTICS);
            Assert.assertNotNull(statistics, "We need a statistics instance.");
            Object viewUtilities = getServletContext().getAttribute(Constants.ATTRIBUTE_VIEW_UTILITIES);
            Assert.assertNotNull(viewUtilities, "We need a view utilities.");
        } // checkModule()

    } // TestServletModule


    @Test
    public void testTangramServletModule() {
        TestServletModule servletModule = new TestServletModule();
        Injector injector = Guice.createInjector(new PostConstructModule(), servletModule);
        Assert.assertNotNull(injector, "The injector must not be null.");
        servletModule.checkModule();
    } // testTangramServletModule()

} // TangramServletModuleTest
