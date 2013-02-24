/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.view.jsp;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.tangram.Constants;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.Utils;
import org.tangram.view.ViewHandler;

public class IncludeTag implements Tag, Serializable {

    private static final Log log = LogFactory.getLog(IncludeTag.class);

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


    public static void render(Writer out, Map<String, Object> model, String view) throws IOException {
        ServletRequest request = (ServletRequest)model.get("request");
        ServletResponse response = (ServletResponse)model.get("response");

        ViewHandler viewHandler = Utils.getViewHandler(request);
        if (viewHandler==null) {
            throw new RuntimeException("No view handler found");
        } // if

        ModelAndViewFactory mavf = Utils.getModelAndViewFactory(request);
        ModelAndView mav = mavf.createModelAndView(model, view);
        View effectiveView = mav.getView();
        if (log.isDebugEnabled()) {
            log.debug("render() effectiveView="+effectiveView);
        } // if
        try {
            if (effectiveView==null) {
                String viewName = mav.getViewName();
                if (viewName==null) {
                    viewName = Constants.DEFAULT_VIEW;
                } // if

                effectiveView = viewHandler.resolveView(viewName, mav.getModel(), Locale.getDefault(), request);
            } // if

            if (out!=null) {
                out.flush();
            } // if
            if (log.isDebugEnabled()) {
                log.debug("render() model="+mav.getModel());
            } // if
            effectiveView.render(mav.getModel(), (HttpServletRequest)request, (HttpServletResponse)response);
        } catch (Exception e) {
            log.error("render() #"+view, e);
            if (out != null) {
                out.write(e.getLocalizedMessage());
            } // if
        } // try/catch
    } // render()


    public static void render(ServletRequest request, ServletResponse resp, Writer out, Object bean, String view) {
        if (bean==null) {
            return;
        } // if

        Object oldSelf = request.getAttribute(org.tangram.Constants.THIS);
        try {
            request.setAttribute(org.tangram.Constants.THIS, bean);

            if (log.isDebugEnabled()) {
                log.debug("render() bean="+bean.getClass().getName()+" #"+view);
            } // if

            ModelAndViewFactory mavf = Utils.getModelAndViewFactory(request);
            Map<String, Object> model = mavf.createModel(bean, request, resp);

            render(out, model, view);
        } catch (Exception e) {
            log.error("render() bean="+bean.getClass().getName()+" #"+view, e);
        } // try/catch
        request.setAttribute(org.tangram.Constants.THIS, oldSelf);
    } // render()


    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    } // doStartTag()


    @Override
    public int doEndTag() throws JspException {
        if (log.isDebugEnabled()) {
            log.debug("doEndTag("+Thread.currentThread().getId()+") view ******************* "+view);
        } // if
        /*
        @SuppressWarnings("unchecked")
        Enumeration<String> names = pc.getAttributeNamesInScope(PageContext.REQUEST_SCOPE);
        while (names.hasMoreElements()) {
            String name = names.nextElement();
             if (log.isDebugEnabled()) {
             log.debug("doEndTag("+Thread.currentThread().getId()+") request ******************* "+name);
             } // if
        } // while
        */

        render(pc.getRequest(), pc.getResponse(), pc.getOut(), bean, view);
        return EVAL_PAGE;
    } // doEndTag()


    @Override
    public void release() {
        pc = null;
        parent = null;
        bean = null;
        view = null;
    } // release()

} // Include
