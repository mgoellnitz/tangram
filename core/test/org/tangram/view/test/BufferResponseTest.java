/*
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.view.test;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import org.tangram.view.BufferResponse;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test behaviour of the BufferResponse.
 */
public class BufferResponseTest {

    @Test
    public void testBufferReponse() {
        BufferResponse bufferResponse = new BufferResponse();
        bufferResponse.setBufferSize(1024);
        Assert.assertEquals(bufferResponse.getLocale(), Locale.getDefault(), "Locale should be machine default.");
        bufferResponse.setLocale(Locale.CANADA_FRENCH);
        Assert.assertEquals(bufferResponse.getLocale(), Locale.CANADA_FRENCH, "Unexpected locale discovered.");
        Assert.assertEquals(bufferResponse.getCharacterEncoding(), "UTF-8", "Expected UTF-8 as default encoding.");
        bufferResponse.setCharacterEncoding("UTF-16");
        Assert.assertEquals(bufferResponse.getCharacterEncoding(), "UTF-16", "Expected UTF-16 encoding.");
        Assert.assertEquals(bufferResponse.getContentType(), "text/html", "Expected text/html as default content type.");
        bufferResponse.setContentType("text/plain");
        Assert.assertEquals(bufferResponse.getContentType(), "text/plain", "Expected text/plain content type.");
        Assert.assertEquals(bufferResponse.getBufferSize(), 1024, "Unexpected size of buffer discovered.");
        Assert.assertFalse(bufferResponse.isCommitted(), "Buffer should not be committed.");
        try {
            bufferResponse.getWriter().write("Hallo");
            Assert.assertEquals(bufferResponse.getBytes().length, 5, "Unexpected size of buffer content discovered.");
            bufferResponse.resetBuffer();
            Assert.assertEquals(bufferResponse.getBufferSize(), 1024, "Unexpected size of buffer discovered.");
            ServletOutputStream outputStream = bufferResponse.getOutputStream();
            Assert.assertTrue(outputStream.isReady(), "Stream should always be ready.");
            outputStream.write('a');
            Assert.assertEquals(bufferResponse.getBytes().length, 1, "Unexpected size of buffer content discovered.");
            Assert.assertEquals(bufferResponse.getContents(), "a", "Unexpected buffer content discovered.");
        } catch (IOException e) {
            Assert.fail("Unexpected exception occured.", e);
        } // try/catch

    } // testBufferReponse()


    @Test
    public void testUnimplementedMethods() {
        BufferResponse bufferResponse = new BufferResponse();
        boolean success = false;
        try {
            bufferResponse.setContentLengthLong(13);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.getHeaderNames();
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.getHeaders("dontcare");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.getHeader("dontcare");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.getStatus();
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.setStatus(0);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.addIntHeader("dontcare", 0);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.setIntHeader("dontcare", 0);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.addHeader("dontcare", "nothing");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.setHeader("dontcare", "nothing");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.addDateHeader("dontcare", 0);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.setDateHeader("dontcare", 0);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.sendRedirect("http://do.away/");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } catch (IOException ioe) {
            Assert.fail("Didn't expect any I/O to really happen during test.");
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.sendError(0);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } catch (IOException ioe) {
            Assert.fail("Didn't expect any I/O to really happen during test.");
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.sendError(0, "http://do.away/");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } catch (IOException ioe) {
            Assert.fail("Didn't expect any I/O to really happen during test.");
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.encodeRedirectURL("http://do.away/");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.encodeURL("http://do.away/");
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
        success = false;
        try {
            bufferResponse.addCookie(null);
        } catch (UnsupportedOperationException uoe) {
            success = "NYI".equals(uoe.getMessage());
        } // try/catch
        Assert.assertTrue(success, "Expectedly unsupported operation incidentally now working.");
    } // testUnimplementedMethods()

} // BufferResponseTest
