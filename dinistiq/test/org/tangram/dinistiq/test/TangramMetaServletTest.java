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
package org.tangram.dinistiq.test;

import java.util.Set;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tangram.components.dinistiq.TangramMetaServlet;
import org.tangram.link.LinkFactoryAggregator;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the meta servlet in dinistiq flavour.
 */
public class TangramMetaServletTest {

    @Mock
    private LinkFactoryAggregator linkFactoryAggregator; // NOPMD - not really unused

    @InjectMocks
    private final TangramMetaServlet tangramMetaServlet = new TangramMetaServlet();

    public TangramMetaServletTest() {
        MockitoAnnotations.initMocks(this);
    } // ()

    @Test
    public void testTangramMetaServlet() {
        Assert.assertEquals(tangramMetaServlet.getOrder(), 1000, "Check that the meta servlet is 'the last' in the list.");
        Set<String> urlPatterns = tangramMetaServlet.getUrlPatterns();
        Assert.assertEquals(urlPatterns.size(), 2, "The meta servlet should have two url patterns.");
    } // testTangramMetaServlet()

} // TangramMetaServletTest
