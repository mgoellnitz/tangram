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
package org.tangram.logic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.tangram.content.Content;

/**
 * 
 * Abstract base class for request shim implementations
 * 
 */
public class AbstractViewShim<T extends Content> extends AbstractShim<T> implements ViewShim {

    protected HttpSession session;

    protected HttpServletRequest request;


    public AbstractViewShim(HttpServletRequest request, T delegate) {
        super(delegate);
        this.request = request;
        this.session = request.getSession(false);
    } // AbstractViewShim()


    @Override
	public HttpSession getSession() {
        return session;
    }


    public void setSession(HttpSession session) {
        this.session = session;
    }


    @Override
	public HttpServletRequest getRequest() {
        return request;
    }


    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

} // AbstractViewShim
