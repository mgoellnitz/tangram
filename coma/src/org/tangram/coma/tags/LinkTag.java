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
package org.tangram.coma.tags;

import java.io.IOException;
import java.io.Serializable;

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

    private static final long serialVersionUID = -3685617977023857332L;

    private static final Log log = LogFactory.getLog(Link.class);

    private PageContext pageContext = null;

    private Tag parent = null;

    private Object target = null;

    private String view = null;


    @Override
    public void setPageContext(PageContext p) {
        pageContext = p;
    }


    @Override
    public Tag getParent() {
        return parent;
    }


    @Override
    public void setParent(Tag t) {
        parent = t;
    }


    public Object getTarget() {
        return target;
    }


    public void setTarget(Object bean) {
        this.target = bean;
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


    @Override
    public int doEndTag() throws JspException {
        try {
            HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
            HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
            LinkFactory builder = Utils.getLinkFactory(request);
            if (log.isDebugEnabled()) {
                log.debug("doEndTag() "+target+" "+view);
            } // if
            Link link = builder.createLink(request, response, target, null, view);
            pageContext.getOut().write(link.getUrl());
        } catch (IOException ioe) {
            log.error("doEndTag() could not paste link into output", ioe);
        } // try/catch
        return EVAL_PAGE;
    } // doEndTag()

    
    @Override
    public void release() {
        pageContext = null;
        parent = null;
        target = null;
        view = null;
    } // release()

} // LinkTag
