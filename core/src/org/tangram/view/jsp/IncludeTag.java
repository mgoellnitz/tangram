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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view.jsp;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.view.ViewUtilities;


public class IncludeTag implements Tag, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(IncludeTag.class);

    private static final long serialVersionUID = -5289460956117400994L;

    private PageContext pc = null;

    private Tag parent = null;

    private Object bean = null;

    private String view = null;


    @Override
    public void setPageContext(PageContext p) {
        pc = p;
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


    public String getView() {
        return view;
    }


    public void setView(String s) {
        view = s;
    }


    public static void render(ServletContext context, ServletRequest req, ServletResponse resp, Writer writer, Object bean, String view) {
        if (bean==null) {
            return;
        } // if
        Object oldSelf = req.getAttribute(Constants.THIS);
        try {
            req.setAttribute(Constants.THIS, bean);
            LOG.debug("render() bean={} :{} #{}", bean, bean.getClass().getSimpleName(), view);
            ((ViewUtilities) (context.getAttribute(Constants.ATTRIBUTE_VIEW_UTILITIES))).render(writer, bean, view, req, resp);
        } catch (IOException e) {
            LOG.error("render() bean="+bean+" :"+bean.getClass().getSimpleName()+" #"+view, e);
        } // try/catch
        req.setAttribute(Constants.THIS, oldSelf);
    } // render()


    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    } // doStartTag()


    @Override
    public int doEndTag() throws JspException {
        LOG.debug("doEndTag({}) view {}", Thread.currentThread().getId(), view);
        render(pc.getServletContext(), pc.getRequest(), pc.getResponse(), pc.getOut(), bean, view);
        return EVAL_PAGE;
    } // doEndTag()


    @Override
    public void release() {
        pc = null;
        parent = null;
        bean = null;
        view = null;
    } // release()

} // IncludeTag
