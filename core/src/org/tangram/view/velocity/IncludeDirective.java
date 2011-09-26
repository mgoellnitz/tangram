package org.tangram.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
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
        return Directive.LINE;
    } // getType()


    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);
        context.put("null", null);
    } // init()


    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        HttpServletRequest request = (HttpServletRequest)context.get("request");
        HttpServletResponse response = (HttpServletResponse)context.get("response");

        /* getting direct parameters */
        Object bean = node.jjtGetChild(0).value(context);
        String view = null;
        if (node.jjtGetNumChildren()>1) {
            view = (String)node.jjtGetChild(1).value(context);
        } // if

        // copy model from original context
        Map<String, Object> model = Utils.getModelAndViewFactory(request).createModel(bean, request, response);
        // Map<String, Object> model = new HashMap<String, Object>();
        Object[] keys = context.getKeys();
        for (Object key : keys) {
            String k = ""+key;
            if ( !model.containsKey(k)) {
                model.put(k, context.get(k));
            } // if
        } // for
        model.remove("springMacroRequestContext");
        // model.remove(Constants.THIS);
        // model.put(Constants.THIS, bean);

        IncludeTag.render(writer, model, view);
        return false;
    } // render()

} // IncludeDirective
