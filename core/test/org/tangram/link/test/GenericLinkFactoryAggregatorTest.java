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
package org.tangram.link.test;

import java.lang.reflect.Method;
import org.tangram.link.GenericLinkFactoryAggregator;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the generic link factory aggregator.
 */
public class GenericLinkFactoryAggregatorTest {

    @Test
    public void testGenericLinkFactoryAggregator() {
        GenericLinkFactoryAggregator linkFactoryAggregator = new GenericLinkFactoryAggregator();
        Method method = linkFactoryAggregator.findMethod(this, "testGenericLinkFactoryAggregator");
        Assert.assertNotNull(method, "Didn't find expected method.");
        Assert.assertEquals(method.getName(), "testGenericLinkFactoryAggregator", "Found unexpected method name.");
        Assert.assertEquals(method.getParameterCount(), 0, "Discovered unexpected parameter count.");
    } // testGenericLinkFactoryAggregator()

} // GenericLinkFactoryAggregatorTest()
