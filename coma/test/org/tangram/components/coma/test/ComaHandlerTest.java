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
package org.tangram.components.coma.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.coma.ComaBeanPopulator;
import org.tangram.coma.ComaBlob;
import org.tangram.coma.ComaContent;
import org.tangram.components.coma.ComaHandler;
import org.tangram.content.BeanFactory;
import org.tangram.link.Link;
import org.tangram.link.TargetDescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test default handler behaviour.
 */
public class ComaHandlerTest {

    private final static String CONTENT_ID = "46";

    private final static String BLOB_CONTENT_ID = "42";

    private ComaContent comaContent;

    private ComaContent comaBlobContent;

    private ComaBlob comaBlob;

    @Spy
    private Set<ComaBeanPopulator> populators = new HashSet<>();

    @Mock
    private final BeanFactory beanFactory = Mockito.mock(BeanFactory.class);

    @InjectMocks
    private final ComaHandler comaHandler = new ComaHandler();


    @BeforeClass
    public void init() {
        comaContent = new ComaContent(CONTENT_ID, "Content", null);
        comaBlob = new ComaBlob(BLOB_CONTENT_ID, "data", "image/png", 0, new byte[0]);
        Map<String,Object> properties = new HashMap<>();
        properties.put("data", comaBlob);
        comaBlobContent = new ComaContent(BLOB_CONTENT_ID, "Image", properties);
        MockitoAnnotations.initMocks(this);
    } // init()


    @Test
    public void testRender() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(beanFactory.getBean(CONTENT_ID)).thenReturn(comaContent);
        TargetDescriptor target = comaHandler.render(CONTENT_ID, "page", request, response);
        Assert.assertEquals(target.bean, comaContent, "We prepared the content which now cannot be retrieved.");
        target = comaHandler.render("56", "page", request, response);
        Assert.assertNull(target, "Error message an null target should be returned for invalid IDs.");
        Assert.assertEquals(response.getStatus(), 404, "Error message an null target should be returned for invalid IDs.");
    } // testRender()


    @Test
    public void testRenderBlob() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(beanFactory.getBean(BLOB_CONTENT_ID)).thenReturn(comaBlobContent);
        TargetDescriptor target = comaHandler.renderBlob(BLOB_CONTENT_ID, "data", "x", "image", request, response);
        Assert.assertEquals(target.bean, comaBlob, "We prepared the blob which now cannot be retrieved.");
        target = comaHandler.renderBlob("52", "data", "x", "image", request, response);
        Assert.assertNull(target, "Error message an null target should be returned for invalid IDs.");
        Assert.assertEquals(response.getStatus(), 404, "Error message an null target should be returned for invalid IDs.");
    } // testRenderBlob()


    @Test
    public void testCreateContentLink() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Link link = comaHandler.createLink(request, response, comaContent, null, "page");
        Assert.assertEquals(link.getUrl(), "/content/46?view=page", "Unexpected link generated.");
    } // testCreateContentLink()


    @Test
    public void testCreateBlobLink() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Link link = comaHandler.createLink(request, response, comaBlob, null, null);
        Assert.assertEquals(link.getUrl(), "/contentblob/42/data/123", "Unexpected link generated.");
    } // testCreateBlobLink()


    @Test
    public void testInvalidLink() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Link link = comaHandler.createLink(request, response, this, "fail", null);
        Assert.assertNull(link, "There should be not generated link for non null actions.");
    } // testInvalidLink()

} // ComaHandlerTest
