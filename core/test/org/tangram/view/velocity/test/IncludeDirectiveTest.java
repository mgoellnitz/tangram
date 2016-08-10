/*
 *
 * Copyright 2016 martin
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
package org.tangram.view.velocity.test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.tangram.Constants;
import org.tangram.view.DefaultViewContextFactory;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;
import org.tangram.view.velocity.IncludeDirective;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the velocity directive to include views for beans.
 */
public class IncludeDirectiveTest {

    private final ViewContextFactory contextFactory = new DefaultViewContextFactory();


    @Test
    public void testIncludeDirective() {
        String bean = "linkit";
        String view = "default";
        String url = "/testapp/id_RootTopic:1";

        IncludeDirective includeDirective = new IncludeDirective();
        Assert.assertEquals(includeDirective.getName(), "include", "Unexpected name for link directive.");
        Assert.assertEquals(includeDirective.getType(), DirectiveConstants.LINE, "Unexpected type for include directive.");
        StringBuilder output = new StringBuilder();
        StringBuilderWriter writer = new StringBuilderWriter(output);

        Context c = Mockito.mock(Context.class);
        InternalContextAdapter context = new InternalContextAdapterImpl(c);

        ServletContext app = new MockServletContext();
        HttpServletRequest request = new MockHttpServletRequest(app, "GET", url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ViewUtilities viewUtilities = Mockito.mock(ViewUtilities.class);
        Mockito.when(viewUtilities.getViewContextFactory()).thenReturn(contextFactory);

        String[] keys = {Constants.ATTRIBUTE_REQUEST, Constants.ATTRIBUTE_RESPONSE, Constants.ATTRIBUTE_VIEW_UTILITIES};
        Mockito.when(context.get(Constants.ATTRIBUTE_REQUEST)).thenReturn(request);
        Mockito.when(context.get(Constants.ATTRIBUTE_RESPONSE)).thenReturn(response);
        Mockito.when(context.get(Constants.ATTRIBUTE_VIEW_UTILITIES)).thenReturn(viewUtilities);
        Mockito.when(context.getKeys()).thenReturn(keys);

        Node node = Mockito.mock(Node.class);
        Node beanNode = Mockito.mock(Node.class);
        Node viewNode = Mockito.mock(Node.class);
        Mockito.when(beanNode.value(context)).thenReturn(bean);
        Mockito.when(viewNode.value(context)).thenReturn(view);
        Mockito.when(node.jjtGetChild(0)).thenReturn(beanNode);
        Mockito.when(node.jjtGetChild(1)).thenReturn(viewNode);
        Mockito.when(node.jjtGetNumChildren()).thenReturn(2);

        RuntimeInstance runtime = new RuntimeInstance();
        runtime.addDirective(includeDirective);
        includeDirective.init(runtime, context, node);

        boolean result = true;
        try {
            result = includeDirective.render(context, writer, node);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertFalse(result, "Include directive should return false.");
        Assert.assertEquals(writer.toString(), "", "Unexpected result on writer.");
    } // testIncludeDirective()

} // IncludeDirectiveTest
