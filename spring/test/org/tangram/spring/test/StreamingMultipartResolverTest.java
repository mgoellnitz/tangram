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
package org.tangram.spring.test;

import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.tangram.spring.StreamingMultipartResolver;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test handling of multipart form-data requests.
 */
public class StreamingMultipartResolverTest {

    @Test
    public void testMultipartResolver() throws Exception {
        MockServletContext context = new MockServletContext();
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(context);
        request.setMethod("POST");
        request.setRequestURI("/testapp/id_RootTopic:1");
        request.setContentType("multipart/form-data; boundary=----------tangram");
        String content = "\r\n------------tangram\r\n"
                +"Content-Disposition: form-data; name=\"field\"\r\n\r\n"
                +"content of the field\r\n"
                +"------------tangram\r\n"
                +"Content-Disposition: form-data; name=\"file\"; filename=\"testfile.txt\"\r\n"
                +"Content-Type: text/plain\r\n\r\n"
                +"Please test for these contents here.\r\n\r\n"
                +"------------tangram--\r\n\r\n";
        byte[] bytes = content.getBytes("ISO-8859-1");
        request.setContent(bytes);
        StreamingMultipartResolver resolver = new StreamingMultipartResolver();
        Assert.assertEquals(resolver.getMaxUploadSize(), 50000, "Non default value for upload max size found.");
        resolver.setMaxUploadSize(12345);
        Assert.assertEquals(resolver.getMaxUploadSize(), 12345, "Cannot customize max upload size.");
        Assert.assertTrue(resolver.isMultipart(request), "We have prepared a multipart request which is not recognized.");
        MultipartHttpServletRequest resolved = resolver.resolveMultipart(request);
        Assert.assertEquals(resolved.getParameterMap().size(), 1, "Unexpected number of parameters.");
        Assert.assertEquals(resolved.getFileMap().size(), 1, "Expected one available blob in the request parameters.");
        Assert.assertEquals(resolved.getParameter("field"), "content of the field", "Unexpected field value");
        MultipartFile file = resolved.getFile("file");
        Assert.assertEquals(file.getBytes().length, 38, "Unexpected file size");
        Assert.assertEquals(file.getName(), "testfile.txt", "Unexpected file name");
    } // testMultipartResolver()

} // StreamingMultipartResolverTest
