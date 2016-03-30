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
package org.tangram.coma.test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.tangram.Constants;
import org.tangram.coma.tags.LinkTag;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.mock.content.MockBeanFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the link tag for jsp templates.
 */
public class LinkTagTest {

    private final BeanFactory beanFactory;


    public LinkTagTest() throws FileNotFoundException {
        beanFactory = MockBeanFactory.getInstance();
    } // init()


    @Test
    public void testRelease() {
        LinkTag linkTag = new LinkTag();
        linkTag.setView("view");
        Assert.assertEquals(linkTag.getView(), "view", "Setting the view not recognized.");
        Object bean = new Object();
        linkTag.setTarget(bean);
        Assert.assertEquals(linkTag.getTarget(), bean, "Setting the target not recognized.");
        Tag parent = new LinkTag();
        linkTag.setParent(parent);
        Assert.assertEquals(linkTag.getParent(), parent, "Setting the parent not recognized.");
        linkTag.release();
        Assert.assertNull(linkTag.getTarget(), "No default target expected");
        Assert.assertNull(linkTag.getParent(), "No default parent expected");
        Assert.assertNull(linkTag.getView(), "No default view expected");
    } // testRelease()


    @Test
    public void testTagStart() {
        LinkTag linkTag = new LinkTag();
        int doStartTag = 0;
        try {
            doStartTag = linkTag.doStartTag();
        } catch (JspException e) {
            Assert.fail("doStartTag() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(doStartTag, Tag.SKIP_BODY, "Link tag should skip body.");
    } // testTagStart()


    @Test
    public void testTagEnd() {
        LinkTag linkTag = new LinkTag();
        String id = "RootTopic:1";
        Content bean = beanFactory.getBean(id);
        Assert.assertNotNull(bean, "Bean to be used for test should not be null.");
        linkTag.setTarget(bean);

        ServletContext app = new MockServletContext();
        String url = "/testapp/id_"+id;
        HttpServletRequest request = new MockHttpServletRequest(app, "GET", url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        PageContext context = new MockPageContext(app, request, response);

        LinkFactoryAggregator aggregator = Mockito.mock(LinkFactoryAggregator.class);
        Link link = new Link(url);
        link.addHandler("onclick", "click();");
        Mockito.when(aggregator.createLink(request, response, bean, null, null)).thenReturn(link);
        app.setAttribute(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR, aggregator);
        linkTag.setPageContext(context);

        int doEndTag = 0;
        try {
            doEndTag = linkTag.doEndTag();
        } catch (JspException e) {
            Assert.fail("doEndTag() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(doEndTag, Tag.EVAL_PAGE, "Link tag should eval page.");
        String tagContent = null;
        try {
            tagContent = response.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            Assert.fail("getContentAsString() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(tagContent, url, "Link tag rendering failed");
    } // testTagEnd()

} // LinkTagTest
