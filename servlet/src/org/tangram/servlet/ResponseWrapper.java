/**
 *
 * Copyright 2013 Martin Goellnitz
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
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * Jetty - at least up to the version now packagd with gradle - comes with the bug that every compiled JSP
 * get a 'response.setContentType("text/html")' so that you cannot change this type with a
 * response.setContentType().
 */
public class ResponseWrapper implements HttpServletResponse {

    String contentType;

    HttpServletResponse delegate;


    public ResponseWrapper(HttpServletResponse delegate) {
        this.delegate = delegate;
        this.contentType = delegate.getContentType();
    }


    @Override
    public void addCookie(Cookie cookie) {
        delegate.addCookie(cookie);
    }


    @Override
    public boolean containsHeader(String name) {
        return delegate.containsHeader(name);
    }


    @Override
    public String encodeURL(String url) {
        return delegate.encodeURL(url);
    }


    @Override
    public String encodeRedirectURL(String url) {
        return encodeRedirectURL(url);
    }


    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return encodeUrl(url);
    }


    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return encodeRedirectUrl(url);
    }


    @Override
    public void sendError(int sc, String msg) throws IOException {
        delegate.sendError(sc, msg);
    }


    @Override
    public void sendError(int sc) throws IOException {
        delegate.sendError(sc);
    }


    @Override
    public void sendRedirect(String location) throws IOException {
        delegate.sendRedirect(location);
    }


    @Override
    public void setDateHeader(String name, long date) {
        delegate.setDateHeader(name, date);
    }


    @Override
    public void addDateHeader(String name, long date) {
        delegate.addDateHeader(name, date);
    }


    @Override
    public void setHeader(String name, String value) {
        delegate.setHeader(name, value);
    }


    @Override
    public void addHeader(String name, String value) {
        delegate.addHeader(name, value);
    }


    @Override
    public void setIntHeader(String name, int value) {
        delegate.setIntHeader(name, value);
    }


    @Override
    public void addIntHeader(String name, int value) {
        delegate.addIntHeader(name, value);
    }


    @Override
    public void setStatus(int sc) {
        delegate.setStatus(sc);
    }


    @Override
    @Deprecated
    public void setStatus(int sc, String sm) {
        delegate.setStatus(sc, sm);
    }


    @Override
    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }


    @Override
    public String getContentType() {
        return contentType;
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        return delegate.getWriter();
    }


    @Override
    public void setCharacterEncoding(String charset) {
        delegate.setCharacterEncoding(charset);
    }


    @Override
    public void setContentLength(int len) {
        delegate.setBufferSize(len);
    }


    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }


    @Override
    public void setBufferSize(int size) {
        delegate.setBufferSize(size);
    }


    @Override
    public int getBufferSize() {
        return delegate.getBufferSize();
    }


    @Override
    public void flushBuffer() throws IOException {
        delegate.setContentType(contentType);
        delegate.flushBuffer();
    }


    @Override
    public void resetBuffer() {
        delegate.resetBuffer();
    }


    @Override
    public boolean isCommitted() {
        return delegate.isCommitted();
    }


    @Override
    public void reset() {
        delegate.reset();
    }


    @Override
    public void setLocale(Locale loc) {
        delegate.setLocale(loc);
    }


    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

} // ResponseWrapper
