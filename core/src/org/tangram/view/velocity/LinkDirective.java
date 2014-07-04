/**
 *
 * Copyright 2011-2014 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view.velocity;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.jsp.LinkTag;

public class LinkDirective extends Directive {

    private static final Logger LOG = LoggerFactory.getLogger(LinkDirective.class);


    @Override
    public String getName() {
        return "link";
    } // getName();


    @Override
    public int getType() {
        return DirectiveConstants.LINE;
    } // getType()


    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) {
        super.init(rs, context, node);
        context.put("null", null);
    } // init()


    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException {
        HttpServletRequest request = (HttpServletRequest)context.get(Constants.ATTRIBUTE_REQUEST);
        HttpServletResponse response = (HttpServletResponse)context.get(Constants.ATTRIBUTE_RESPONSE);
        LinkFactoryAggregator builder = (LinkFactoryAggregator)context.get(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR);

        /* getting direct parameters */
        Object bean = node.jjtGetChild(0).value(context);
        String view = null;
        String action = null;
        boolean href = false;
        boolean target = false;
        boolean handlers = false;

        int flagIdx = 1;
        if (node.jjtGetNumChildren()>1) {
            Object value = node.jjtGetChild(1).value(context);
            if ((value==null)||(value instanceof String)) {
                view = (String)value;
                flagIdx++;
            } // if
        } // if

        if (node.jjtGetNumChildren()>2) {
            Object value = node.jjtGetChild(2).value(context);
            if (value instanceof String) {
                action = (String)value;
                flagIdx++;
            } // if
        } // if

        if (node.jjtGetNumChildren()>flagIdx) {
            href = (Boolean)node.jjtGetChild(flagIdx).value(context);
            flagIdx++;
        } // if

        if (node.jjtGetNumChildren()>flagIdx) {
            target = (Boolean)node.jjtGetChild(flagIdx).value(context);
            flagIdx++;
        } // if

        if (node.jjtGetNumChildren()>flagIdx) {
            handlers = (Boolean)node.jjtGetChild(flagIdx).value(context);
            flagIdx++;
        } // if

        try {
            LinkTag.render(builder, request, response, writer, bean, action, view, href, target, handlers);
        } catch (RuntimeException rte) {
            LOG.error("render()", rte);
        } // try/catch
        return false;
    } // render()

} // IncludeDirective
