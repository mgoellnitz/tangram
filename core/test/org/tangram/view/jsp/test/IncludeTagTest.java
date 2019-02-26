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
package org.tangram.view.jsp.test;

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
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.mock.content.MockBeanFactory;
import org.tangram.view.ViewUtilities;
import org.tangram.view.jsp.IncludeTag;
import org.tangram.view.jsp.LinkTag;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the include tag for jsp templates.
 */
public class IncludeTagTest {

    private final BeanFactory<?> beanFactory;


    public IncludeTagTest() throws FileNotFoundException {
        beanFactory = MockBeanFactory.getInstance();
    } // ()


    @Test
    public void testRelease() {
        IncludeTag includeTag = new IncludeTag();
        includeTag.setPageContext(new MockPageContext());
        includeTag.setView("view");
        Assert.assertEquals(includeTag.getView(), "view", "Setting the view not recognized.");
        Object bean = new Object();
        includeTag.setBean(bean);
        Assert.assertEquals(includeTag.getBean(), bean, "Setting the bean not recognized.");
        Tag parent = new LinkTag();
        includeTag.setParent(parent);
        Assert.assertEquals(includeTag.getParent(), parent, "Setting the parent not recognized.");
        includeTag.release();
        Assert.assertNull(includeTag.getBean(), "No default bean expected.");
        Assert.assertNull(includeTag.getParent(), "No default parent expected.");
        Assert.assertNull(includeTag.getView(), "No default view expected.");
    } // testRelease()


    @Test
    public void testTagStart() {
        IncludeTag includeTag = new IncludeTag();
        int doStartTag = 0;
        try {
            doStartTag = includeTag.doStartTag();
        } catch (JspException e) {
            Assert.fail("doStartTag() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(doStartTag, Tag.SKIP_BODY, "Link tag should skip body.");
    } // testTagStart()


    @Test
    public void testTagEnd() {
        IncludeTag includeTag = new IncludeTag();
        String id = "RootTopic:1";
        Content bean = beanFactory.getBean(id);
        Assert.assertNotNull(bean, "Bean to be used for test should not be null.");
        includeTag.setBean(bean);

        ServletContext app = new MockServletContext();
        String url = "/testapp/id_"+id;
        HttpServletRequest request = new MockHttpServletRequest(app, "GET", url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        PageContext context = new MockPageContext(app, request, response);
        ViewUtilities factory = Mockito.mock(ViewUtilities.class);
        app.setAttribute(Constants.ATTRIBUTE_VIEW_UTILITIES, factory);
        includeTag.setPageContext(context);

        int doEndTag = 0;
        try {
            doEndTag = includeTag.doEndTag();
        } catch (JspException e) {
            Assert.fail("doEndTag() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(doEndTag, Tag.EVAL_PAGE, "Include tag should eval page.");
        String tagContent = null;
        try {
            tagContent = response.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            Assert.fail("getContentAsString() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(tagContent, "", "Include tag rendering failed.");
    } // testTagEnd()


    @Test
    public void testNullBean() {
        IncludeTag includeTag = new IncludeTag();

        ServletContext app = new MockServletContext();
        HttpServletRequest request = new MockHttpServletRequest(app);
        MockHttpServletResponse response = new MockHttpServletResponse();
        PageContext context = new MockPageContext(app, request, response);
        includeTag.setPageContext(context);

        int doEndTag = 0;
        try {
            doEndTag = includeTag.doEndTag();
        } catch (JspException e) {
            Assert.fail("doEndTag() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(doEndTag, Tag.EVAL_PAGE, "Include tag should eval page.");
        String tagContent = null;
        try {
            tagContent = response.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            Assert.fail("getContentAsString() should not issue an exception.", e);
        } // try/catch
        Assert.assertEquals(tagContent, "", "Include tag rendering failed.");
    } // testNullBean()

} // IncludeTagTest
