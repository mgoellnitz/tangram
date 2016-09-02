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
package org.tangram.servlet.test;

import java.util.HashMap;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.components.MetaLinkHandler;
import org.tangram.servlet.MetaServlet;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;
import org.testng.annotations.Test;


/**
 * Test aspects of the meta servlet.
 */
public class MetaServletTest {

    @Mock
    private ViewUtilities viewUtilities; // NOPMD - not really unused

    @Mock
    private MetaLinkHandler metaLinkHandler; // NOPMD - not really unused

    @InjectMocks
    private final MetaServlet metaServlet = new MetaServlet();


    public MetaServletTest() {
        MockitoAnnotations.initMocks(this);
    } // ()


    @Test
    public void testMetaServlet() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ViewContext context = Mockito.mock(ViewContext.class);
        Mockito.when(context.getModel()).thenReturn(new HashMap<String, Object>());
        Mockito.when(context.getViewName()).thenReturn("viewName");
        Mockito.when(metaLinkHandler.handleRequest(request, response)).thenReturn(context);

        metaServlet.service(request, response);
    } // testMetaServlet()


    @Test
    public void testMetaServletExceptions() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ViewContext context = Mockito.mock(ViewContext.class);
        Mockito.when(context.getModel()).thenReturn(new HashMap<String, Object>());
        RuntimeException runtimeException = new RuntimeException("Hi!");
        Mockito.when(context.getViewName()).thenThrow(runtimeException);
        Mockito.when(metaLinkHandler.handleRequest(request, response)).thenReturn(context);
        ViewContextFactory factory = Mockito.mock(ViewContextFactory.class);
        Mockito.when(viewUtilities.getViewContextFactory()).thenReturn(factory);
        ViewContext vc = Mockito.mock(ViewContext.class);
        Mockito.when(vc.getModel()).thenReturn(new HashMap<String, Object>());
        Mockito.when(vc.getViewName()).thenReturn("error");
        Mockito.when(factory.createViewContext(runtimeException, request, response)).thenReturn(vc);

        metaServlet.service(request, response);
    } // testMetaServletExceptions()

} // MetaServletTest
