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
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.velocity.LinkDirective;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the velocity directive to generate and output links.
 */
public class LinkDirectiveTest {

    @Test
    public void testLinkDirective() {
        String bean = "linkit";
        String view = "default";
        String action = "none";
        String url = "/testapp/id_RootTopic:1";

        LinkDirective linkDirective = new LinkDirective();

        Assert.assertEquals(linkDirective.getName(), "link", "Unexpected name for link directive.");
        Assert.assertEquals(linkDirective.getType(), DirectiveConstants.LINE, "Unexpected type for link directive.");
        StringBuilder output = new StringBuilder();
        StringBuilderWriter writer = new StringBuilderWriter(output);

        Context c = Mockito.mock(Context.class);
        InternalContextAdapter context = new InternalContextAdapterImpl(c);

        ServletContext app = new MockServletContext();
        HttpServletRequest request = new MockHttpServletRequest(app, "GET", url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        LinkFactoryAggregator aggregator = Mockito.mock(LinkFactoryAggregator.class);
        Mockito.when(aggregator.createLink(request, response, bean, action, view)).thenReturn(new Link(url));

        Mockito.when(context.get(Constants.ATTRIBUTE_REQUEST)).thenReturn(request);
        Mockito.when(context.get(Constants.ATTRIBUTE_RESPONSE)).thenReturn(response);
        Mockito.when(context.get(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR)).thenReturn(aggregator);

        Node node = Mockito.mock(Node.class);
        Node beanNode = Mockito.mock(Node.class);
        Node viewNode = Mockito.mock(Node.class);
        Node actionNode = Mockito.mock(Node.class);
        Node hrefNode = Mockito.mock(Node.class);
        Node targetNode = Mockito.mock(Node.class);
        Node handlersNode = Mockito.mock(Node.class);
        Mockito.when(beanNode.value(context)).thenReturn(bean);
        Mockito.when(viewNode.value(context)).thenReturn(view);
        Mockito.when(actionNode.value(context)).thenReturn(action);
        Mockito.when(hrefNode.value(context)).thenReturn(true);
        Mockito.when(targetNode.value(context)).thenReturn(false);
        Mockito.when(handlersNode.value(context)).thenReturn(false);
        Mockito.when(node.jjtGetChild(0)).thenReturn(beanNode);
        Mockito.when(node.jjtGetChild(1)).thenReturn(viewNode);
        Mockito.when(node.jjtGetChild(2)).thenReturn(actionNode);
        Mockito.when(node.jjtGetChild(3)).thenReturn(hrefNode);
        Mockito.when(node.jjtGetChild(4)).thenReturn(targetNode);
        Mockito.when(node.jjtGetChild(5)).thenReturn(handlersNode);
        Mockito.when(node.jjtGetNumChildren()).thenReturn(6);

        RuntimeInstance runtime = new RuntimeInstance();
        runtime.addDirective(linkDirective);
        linkDirective.init(runtime, context, node);
        boolean result = true;
        try {
            result = linkDirective.render(context, writer, node);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertFalse(result, "Link directive should return false.");
        Assert.assertEquals(writer.toString(), "href=\""+url+"\" ", "Unexpected result on writer.");
    } // testLinkDirective()

} // LinkDirectiveTest
