/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * Jetty - at least up to the version now packagd with gradle - comes with the bug that every compiled JSP
 * get a 'response.setContentType("text/html")' so that you cannot change this type with a
 * response.setContentType().
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private String contentType;

    private final Map<String, String> headers = new HashMap<>();


    public ResponseWrapper(HttpServletResponse delegate) {
        super(delegate);
        this.contentType = delegate.getContentType();
        delegate.setCharacterEncoding("utf-8");
    } // ResponseWrapper()


    public Map<String, String> getHeaders() {
        return headers;
    }


    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }


    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
    }


    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
    }


    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        headers.put(name, value);
    }


    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        headers.put(name, headers.containsKey(name) ? headers.get(name)+","+value : value);
    }


    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
    }


    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
    }


    @Override
    public String getContentType() {
        return contentType;
    }


    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }


    @Override
    public void flushBuffer() throws IOException {
        super.setContentType(contentType);
        super.flushBuffer();
    }

} // ResponseWrapper
