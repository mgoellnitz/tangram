/**
 *
 * Copyright 2011 Martin Goellnitz
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.tangram.view.Utils;
import org.tangram.view.jsp.IncludeTag;

public class IncludeDirective extends Directive {

    // private static final Log log = LogFactory.getLog(IncludeDirective.class);

    @Override
    public String getName() {
        return "include";
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
        HttpServletRequest request = (HttpServletRequest)context.get("request");
        HttpServletResponse response = (HttpServletResponse)context.get("response");

        /* getting direct parameters */
        Object bean = node.jjtGetChild(0).value(context);
        String view = null;
        if (node.jjtGetNumChildren()>1) {
            view = (String)node.jjtGetChild(1).value(context);
        } // if

        // copy model from original context
        Map<String, Object> model = Utils.getModelAndViewFactory().createModel(bean, request, response);
        Object[] keys = context.getKeys();
        for (Object key : keys) {
            String k = ""+key;
            if ( !model.containsKey(k)) {
                model.put(k, context.get(k));
            } // if
        } // for
        model.remove("springMacroRequestContext");

        IncludeTag.render(writer, model, view);
        return false;
    } // render()

} // IncludeDirective
