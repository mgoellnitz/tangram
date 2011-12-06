/**
 * 
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.coma.tags;

import java.io.Serializable;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.tangram.Constants;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.ModelAwareViewResolver;
import org.tangram.view.Utils;

public class IncludeTag implements Tag, Serializable {

    private static final Log log = LogFactory.getLog(IncludeTag.class);

    private static final long serialVersionUID = -5289460956117400994L;

    private PageContext pc = null;

    private Tag parent = null;

    private Object theSelf = null;

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


    public Object getSelf() {
        return theSelf;
    }


    public void setSelf(Object bean) {
        this.theSelf = bean;
    }


    public String getView() {
        return view;
    }


    public void setView(String s) {
        view = s;
    }


    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    } // doStartTag()


    // TODO: duplicate code copied from core module
    @Override
    public int doEndTag() throws JspException {
        ServletRequest request = pc.getRequest();
        ServletResponse response = pc.getResponse();

        Object oldSelf = request.getAttribute(org.tangram.Constants.THIS);
        request.setAttribute(org.tangram.Constants.THIS, theSelf);

        ApplicationContext appContext = (ApplicationContext)request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        String[] resolverNames = null;
        if (appContext!=null) {
            resolverNames = appContext.getBeanNamesForType(ModelAwareViewResolver.class);
        } // if
        if ((resolverNames==null)||(resolverNames.length<1)) {
            throw new RuntimeException("No view resolver found");
        } // if

        try {
            if (log.isInfoEnabled()) {
                log.info("include() bean="+theSelf+" #"+view);
            } // if
            ModelAndViewFactory mavf = Utils.getModelAndViewFactory(request);
            ModelAndView mav = mavf.createModelAndView(theSelf, view, request, response);
            View effectiveView = mav.getView();
            if (log.isInfoEnabled()) {
                log.info("include() effectiveView="+effectiveView);
            } // if
            if (effectiveView==null) {
                String viewName = mav.getViewName();
                if (viewName==null) {
                    viewName = Constants.DEFAULT_VIEW;
                } // if
                  // TODO: It's rather stupid to ask each and every view resolver
                for (String resolverName : resolverNames) {
                    if (log.isInfoEnabled()) {
                        log.info("include() resolverName="+resolverName);
                    } // if
                    ModelAwareViewResolver resolver = (ModelAwareViewResolver)appContext.getBean(resolverName);
                    effectiveView = resolver.resolveViewName(viewName, mav.getModel(), Locale.getDefault());
                    if (effectiveView!=null) {
                        break;
                    } // if
                } // for
            } // if

            Writer out = pc.getOut();
            if (out!=null) {
                out.flush();
            } // if
            effectiveView.render(mav.getModel(), (HttpServletRequest)request, (HttpServletResponse)response);
        } catch (Exception e) {
            log.error("include() ", e);
        } // try/catch
        request.setAttribute(org.tangram.Constants.THIS, oldSelf);
        return EVAL_PAGE;
    } // doEndTag()


    @Override
    public void release() {
        pc = null;
        parent = null;
        theSelf = null;
        view = null;
    } // release()

} // Include
