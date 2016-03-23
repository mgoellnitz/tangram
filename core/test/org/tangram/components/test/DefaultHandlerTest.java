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
package org.tangram.components.test;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.components.DefaultHandler;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.content.blob.MimedBlob;
import org.tangram.link.Link;
import org.tangram.link.TargetDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test default handler behaviour.
 */
public class DefaultHandlerTest {

    @Mock
    private BeanFactory beanFactory; // NOPMD - this field is not really unused

    @InjectMocks
    private final DefaultHandler defaultHandler = new DefaultHandler();

    private interface BlobContent extends MimedBlob, Content {

    } // BlobContent

    @Test
    public void testDefaultHandler() {
        MockitoAnnotations.initMocks(this);
        defaultHandler.setMaxInlinedBlobSize(2);
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        try {
            TargetDescriptor target = defaultHandler.render("Content:42", request, response);
            Assert.assertNull(target, "Null target expected for invalid content ID.");
            Assert.assertEquals(response.getStatus(), 404, "The given content ID is intentionally wrong.");
        } catch (IOException e) {
            Assert.fail("Exceptions should not occur during test.", e);
        } // try/catch
        Content c = Mockito.mock(Content.class);
        Mockito.when(c.getId()).thenReturn("RootTopic:1");
        Link link = defaultHandler.createLink(request, response, c, null, "inline");
        Assert.assertEquals(link.getUrl(), "/id_RootTopic:1/view_inline", "Unexpected URL returned.");

        BlobContent blob = Mockito.mock(BlobContent.class);
        Mockito.when(blob.getId()).thenReturn("Image:13");
        byte[] bytes = new byte[2];
        bytes[0] = 65;
        bytes[1] = 66;
        Mockito.when(blob.getBytes()).thenReturn(bytes);
        link = defaultHandler.createLink(request, response, blob, null, null);
        Assert.assertEquals(link.getUrl(), "/id_Image:13", "Unexpected large blob URL returned.");

        blob = Mockito.mock(BlobContent.class);
        Mockito.when(blob.getId()).thenReturn("Image:13");
        Mockito.when(blob.getMimeType()).thenReturn("image/png");
        bytes = new byte[1];
        bytes[0] = 65;
        Mockito.when(blob.getBytes()).thenReturn(bytes);
        link = defaultHandler.createLink(request, response, blob, null, null);
        Assert.assertEquals(link.getUrl(), "data:image/png;base64,QQ==", "Unexpected small blob URL returned.");
    } // testDefaultHandler()

} // DefaultHandlerTest
