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
package org.tangram.coma.tags;

import java.io.Serializable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class IncludeTag implements Tag, Serializable {

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


    @Override
    public int doEndTag() throws JspException {
        org.tangram.view.jsp.IncludeTag.render(pc.getServletContext(), pc.getRequest(), pc.getResponse(), pc.getOut(), theSelf, view);
        return EVAL_PAGE;
    } // doEndTag()


    @Override
    public void release() {
        pc = null;
        parent = null;
        theSelf = null;
        view = null;
    } // release()

} // IncludeTag
