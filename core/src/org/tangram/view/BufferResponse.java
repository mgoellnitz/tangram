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
package org.tangram.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BufferResponse implements HttpServletResponse {

    private static final Log log = LogFactory.getLog(BufferResponse.class);


    int bufferSize = 256;

    ByteArrayOutputStream out;

    private ServletOutputStream stream;

    private PrintWriter writer;

    private int size;

    private String contentType = "text/html";

    private String encoding = "UTF-8";

    private Locale locale = Locale.getDefault();


    public BufferResponse() {
        reset();
    }


    @Override
    public String getCharacterEncoding() {
        if (log.isInfoEnabled()) {
            log.info("getCharacterEncoding() "+encoding);
        }
        return encoding;
    }


    @Override
    public String getContentType() {
        if (log.isInfoEnabled()) {
            log.info("getContentType() "+contentType);
        }
        return contentType;
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("getOutputStream() "+stream);
        }
        return stream;
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("getWriter() "+writer);
        }
        return writer;
    }


    @Override
    public void setCharacterEncoding(String charset) {
        if (log.isInfoEnabled()) {
            log.info("setCharacterEncoding("+charset+")");
        }
        encoding = charset;
    }


    @Override
    public void setContentLength(int len) {
        if (log.isInfoEnabled()) {
            log.info("setContentLength("+len+")");
        }
        size = len;
    }


    @Override
    public void setContentType(String type) {
        if (log.isInfoEnabled()) {
            log.info("setContenType("+type+")");
        }
        contentType = type;
    }


    @Override
    public void setBufferSize(int size) {
        if (log.isInfoEnabled()) {
            log.info("setBufferSize("+size+")");
        }
        bufferSize = size;
        reset();
    }


    @Override
    public int getBufferSize() {
        if (log.isInfoEnabled()) {
            log.info("getBufferSize() "+bufferSize);
        }
        return bufferSize;
    }


    @Override
    public void flushBuffer() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("flushBuffer()");
        }
        writer.flush();
        stream.flush();
        out.flush();
    }


    @Override
    public void resetBuffer() {
        if (log.isInfoEnabled()) {
            log.info("resetBuffer()");
        }
        reset();
    }


    @Override
    public boolean isCommitted() {
        if (log.isInfoEnabled()) {
            log.info("isCommitted()");
        }
        return false;
    }


    @Override
    public void reset() {
        if (log.isInfoEnabled()) {
            log.info("reset()");
        }
        out = new ByteArrayOutputStream(bufferSize);

        stream = new ServletOutputStream() {

            @Override
            public void write(int b) throws IOException {
                if (log.isWarnEnabled()) {
                    log.warn("write() "+b);
                }
                out.write(b);
            }

        };

        writer = new PrintWriter(stream);
    } // reset()


    @Override
    public void setLocale(Locale loc) {
        locale = loc;
    }


    @Override
    public Locale getLocale() {
        return locale;
    }


    public String getContents() throws UnsupportedEncodingException, IOException {
        flushBuffer();
        out.close();
        return new String(out.toByteArray(), getCharacterEncoding());
    } // getContents()


    /**
     * HttpServletResponse *
     */
    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    @Deprecated
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void sendError(int sc, String msg) throws IOException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void sendError(int sc) throws IOException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void sendRedirect(String location) throws IOException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setStatus(int sc) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    @Deprecated
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException("NYI");
    }

} // BufferResponse
