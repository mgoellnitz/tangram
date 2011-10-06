package org.tangram.view.jsp;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.view.Utils;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

public class LinkTag implements Tag, Serializable {

    private static final long serialVersionUID = 7615554589005573155L;

    private static final Log log = LogFactory.getLog(LinkTag.class);

    private PageContext context = null;

    private Tag parent = null;

    private Object bean = null;

    private String action = null;

    private String view = null;

    private boolean href = false;

    private boolean target = false;

    private boolean handlers = false;


    @Override
	public void setPageContext(PageContext p) {
        context = p;
    }


    @Override
	public Tag getParent() {
        return parent;
    }


    @Override
	public void setParent(Tag t) {
        parent = t;
    }


    public Object getBean() {
        return bean;
    }


    public void setBean(Object bean) {
        this.bean = bean;
    }


    public String getAction() {
        return action;
    }


    public void setAction(String action) {
        this.action = action;
    }


    public String getView() {
        return view;
    }


    public void setView(String s) {
        view = s;
    }


    public boolean isHref() {
        return href;
    }


    public void setHref(boolean href) {
        this.href = href;
    }


    public boolean isTarget() {
        return target;
    }


    public void setTarget(boolean target) {
        this.target = target;
    }


    public boolean isHandlers() {
        return handlers;
    }


    public void setHandlers(boolean handlers) {
        this.handlers = handlers;
    }


    @Override
	public int doStartTag() throws JspException {
        return SKIP_BODY;
    } // doStartTag()


    public static void render(HttpServletRequest request, HttpServletResponse response, Writer out, Object bean, String action, String view, boolean isHref,
            boolean isTarget, boolean isHandlers) {
        LinkFactory linkBuilder = Utils.getLinkFactory(request);

        try {
            Link link = linkBuilder.createLink(request, response, bean, action, view);
            if (isHref) {
                out.write("href=\"");
            } // if
            out.write(link.getUrl());
            if (isHref) {
                out.write("\" ");
            } // if
            if (isTarget) {
                if (link.getTarget() != null) {
                    out.write("target=\""+link.getTarget()+"\" ");
                } // if
            } // if
            if (isHandlers) {
                for (Map.Entry<String, String> entry : link.getHandlers().entrySet()) {
                    out.write(entry.getKey()+"=\""+entry.getValue()+"\" ");
                } // for
            } // if
        } catch (IOException ioe) {
            log.error("doEndTag() could not paste link into output");
        } // try/catch
    } // render()


    public static void render(HttpServletRequest request, HttpServletResponse response, Writer out, Object bean, String action, String view) {
        render(request, response, out, bean, action, view, false, false, false);
    } // render()


    @Override
	public int doEndTag() throws JspException {
        Writer out = context.getOut();
        HttpServletRequest request = (HttpServletRequest)(context.getRequest());
        HttpServletResponse response = (HttpServletResponse)(context.getResponse());
        render(request, response, out, getBean(), getAction(), getView(), isHref(), isTarget(), isHandlers());
        return EVAL_PAGE;
    } // doEndTag()


    @Override
	public void release() {
        context = null;
        parent = null;
        bean = null;
        action = null;
        view = null;
        href = false;
        target = false;
        handlers = false;
    } // release()

} // Include
