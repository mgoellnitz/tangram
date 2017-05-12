/**
 *
 * Copyright 2013-2017 Martin Goellnitz
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
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BufferResponse implements HttpServletResponse {

    private static final Logger LOG = LoggerFactory.getLogger(BufferResponse.class);

    private int bufferSize = 256;

    private ByteArrayOutputStream out;

    private ServletOutputStream stream;

    private PrintWriter writer;

    private String contentType = "text/html";

    private String encoding = "UTF-8";

    private Locale locale = Locale.getDefault();


    public BufferResponse() {
        reset();
    }


    @Override
    public String getCharacterEncoding() {
        LOG.debug("getCharacterEncoding() {}", encoding);
        return encoding;
    }


    @Override
    public String getContentType() {
        LOG.debug("getContentType() {}", contentType);
        return contentType;
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        LOG.debug("getOutputStream() {}", stream);
        return stream;
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        LOG.debug("getWriter() {}", writer);
        return writer;
    }


    @Override
    public void setCharacterEncoding(String charset) {
        LOG.debug("setCharacterEncoding({})", charset);
        encoding = charset;
    }


    @Override
    public void setContentLength(int len) {
        LOG.debug("setContentLength({})", len);
    }


    @Override
    public void setContentType(String type) {
        LOG.debug("setContenType({})", type);
        contentType = type;
    }


    @Override
    public void setBufferSize(int size) {
        LOG.debug("setBufferSize({})", size);
        bufferSize = size;
        reset();
    }


    @Override
    public int getBufferSize() {
        LOG.debug("getBufferSize() {}", bufferSize);
        return bufferSize;
    }


    @Override
    public void flushBuffer() throws IOException {
        LOG.debug("flushBuffer()");
        writer.flush();
        stream.flush();
        out.flush();
    }


    @Override
    public void resetBuffer() {
        LOG.debug("resetBuffer()");
        reset();
    }


    @Override
    public boolean isCommitted() {
        LOG.debug("isCommitted()");
        return false;
    }

    /**
     * Wrap a simple byte array output stream as a servlet output stream.
     */
    private class ServletOutputStreamWrapper extends ServletOutputStream {

            private final ByteArrayOutputStream out;

            public ServletOutputStreamWrapper(ByteArrayOutputStream o) {
                out = o;
            }

            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }


            @Override
            public boolean isReady() {
                return true;
            }


            @Override
            public void setWriteListener(WriteListener wl) {
                throw new UnsupportedOperationException("NYI");
            }

    }


    @Override
    public final void reset() {
        LOG.debug("reset()");
        out = new ByteArrayOutputStream(bufferSize);
        stream = new ServletOutputStreamWrapper(out);
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
        return new String(getBytes());
    } // getContents()


    public byte[] getBytes() throws UnsupportedEncodingException, IOException {
        flushBuffer();
        out.close();
        return out.toByteArray();
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


    @Override
    public int getStatus() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String getHeader(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Collection<String> getHeaders(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setContentLengthLong(long l) {
        throw new UnsupportedOperationException("NYI");
    }

} // BufferResponse
