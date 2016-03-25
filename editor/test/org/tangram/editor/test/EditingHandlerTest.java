/**
 *
 * Copyright 2014-2016 Martin Goellnitz
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
package org.tangram.editor.test;

import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.Constants;
import org.tangram.components.editor.EditingHandler;
import org.tangram.components.servlet.ServletViewUtilities;
import org.tangram.components.test.GroovyClassRepositoryTest;
import org.tangram.content.Content;
import org.tangram.content.TransientCode;
import org.tangram.link.Link;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.tangram.logic.ClassRepository;
import org.tangram.mock.MockMutableBeanFactory;
import org.tangram.mock.MockOrmManager;
import org.tangram.mock.content.MockContent;
import org.tangram.mock.content.RootTopic;
import org.tangram.mock.content.Topic;
import org.tangram.mutable.components.test.ToolHandlerTest;
import org.tangram.protection.AuthorizationService;
import org.tangram.view.GenericPropertyConverter;
import org.tangram.view.PropertyConverter;
import org.tangram.view.ViewUtilities;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class EditingHandlerTest {

    private static final String DUMMY_ID = "pling:plong";

    private static final TargetDescriptor DUMMY_LOGIN = new TargetDescriptor(ToolHandlerTest.class, "dummy", "login");

    @Spy
    private final PropertyConverter propertyConverter = new GenericPropertyConverter();

    @Spy
    private final ViewUtilities viewUtilities = new ServletViewUtilities();

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private LinkHandlerRegistry linkHandlerRegistry; // NOPMD - this field is not really unused

    @Spy
    private final MockMutableBeanFactory beanFactory = new MockMutableBeanFactory();

    @Spy
    private ClassRepository repository; // NOPMD - this field is not really unused

    @InjectMocks
    private final EditingHandler handler = new EditingHandler();


    /**
     * Mock the name pattern of generated ORM classes.
     */
    private class Content$Test extends MockContent { // NOPMD - We need this strange name

    }


    @BeforeClass
    public void init() throws Exception {
        GroovyClassRepositoryTest repositoryTest = new GroovyClassRepositoryTest();
        repositoryTest.init();
        repository = repositoryTest.getInstance();
        MockitoAnnotations.initMocks(this);
        beanFactory.init();
        handler.afterPropertiesSet();
    }


    @Test
    public void testOrmClassNames() {
        Assert.assertEquals(EditingHandler.getDesignClass(Content.class), Content.class, "Content is no generated class.");
        Assert.assertEquals(EditingHandler.getDesignClass(Content$Test.class), MockContent.class, "Simulated ORM class should issue design class.");
    } // testOrmClassNames()


    @Test
    public void testUrlGeneration() {
        Content c = new MockContent(DUMMY_ID);
        for (String a : EditingHandler.PARAMETER_ACTIONS) {
            Link link = handler.createLink(null, null, handler, a, null);
            Assert.assertNotNull(link, "The generation of "+a+" action link failed.");
            Assert.assertEquals(link.getUrl(), "/"+a, "Generation of "+a+" action link with strange result.");
        } // for

        for (String a : EditingHandler.ID_URL_ACTIONS) {
            Link link = handler.createLink(null, null, c, a, null);
            Assert.assertNotNull(link, "The generation of "+a+" action link failed.");
            Assert.assertEquals(link.getUrl(), "/"+a+"/id_"+DUMMY_ID, "Generation of "+a+" action link with strange result.");
        } // for
        Link link = handler.createLink(null, null, c, null, "edit");
        Assert.assertNotNull(link, "The generation of edit view link failed.");
        Assert.assertEquals(link.getUrl(), "/edit/id_"+DUMMY_ID, "Generation of edit view link with strange result.");
    } // testUrlGeneration()


    @Test
    public void testList() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/list");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        try {
            handler.list(TransientCode.class.getName(), "annotation", "org.tangram", request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on listing resources.", e);
        } // try/catch
        Assert.assertEquals(request.getAttribute("editingHandler"), handler, "Prepared instance expected.");
        Assert.assertEquals(request.getAttribute("implementation"), MockOrmManager.class.getPackage().getName(), "This should be the mock ORM.");
        Assert.assertEquals(request.getAttribute("prefix"), "/testapp", "Wrong app prefix detected.");
        Assert.assertEquals(request.getAttribute("note"), "Plain", "Mock classes are not transformed.");
        Assert.assertEquals(request.getAttribute("contentClass"), TransientCode.class, "Class should be TransientCode.");
        Assert.assertEquals(request.getAttribute("designClass"), TransientCode.class, "Class should be TransientCode.");
        Assert.assertEquals(request.getAttribute("designClassPackage"), TransientCode.class.getPackage(), "Package of TransientCode expected.");
        Assert.assertEquals(request.getAttribute(Constants.ATTRIBUTE_REQUEST), request, "Current request instance expected.");
        Assert.assertEquals(request.getAttribute(Constants.ATTRIBUTE_RESPONSE), response, "Current response instance expected.");

        Object ormClasses = request.getAttribute("classes");
        Assert.assertNotNull(ormClasses, "There should be some classes list object.");
        Assert.assertEquals(((Collection<?>) ormClasses).size(), 7, "Fixed number of classes in list expected.");
        Object me = request.getAttribute(Constants.THIS);
        Assert.assertNotNull(me, "There should be some instances list object..");
        Assert.assertEquals(((Collection<?>) me).size(), 7, "Fixed number of instances in list expected.");
    } // testList()


    @Test
    public void testEdit() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/edit");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        String resourceId = "CodeResource:8";
        Content bean = beanFactory.getBean(Content.class, resourceId);
        TargetDescriptor target = null;
        try {
            target = handler.edit(resourceId, request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on editing a content element.", e);
        } // try/catch
        Assert.assertEquals(request.getAttribute("beanFactory"), beanFactory, "Mock bean factory instance expected.");
        Assert.assertEquals(request.getAttribute("cmprefix"), "/testapp/editor/codemirror", "URI path to codemirror expected.");
        Assert.assertEquals(request.getAttribute("ckprefix"), "/testapp/editor/ckeditor", "URI path to codemirror expected.");
        Assert.assertEquals(request.getAttribute("propertyConverter"), propertyConverter, "Property converter expected.");
        Assert.assertEquals(target.bean, bean, "Prepared bean expected.");
        Assert.assertEquals(target.action, null, "No action expected.");
        Assert.assertEquals(target.view, "edit", "Edit view expected.");
        try {
            target = handler.edit("NotValid:13", request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on editing a content element.", e);
        } // try/catch
        Assert.assertNull(target, "Editing of non existent content instance should return null result.");
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND, "Editing of non existen content instance should issue NOT FOUND.");
    } // testEdit()


    @Test
    public void testLink() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/link");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        String resourceId = "RootTopic:1";
        TargetDescriptor target = null;
        try {
            target = handler.link(Topic.class.getName(), resourceId, "bottomLinks", request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on linking a content element.", e);
        } // try/catch
        Assert.assertEquals(target.bean.getClass(), Topic.class, "Bean of given class bean expected.");
        Assert.assertEquals(target.action, "edit", "Edit action expected.");
        Assert.assertEquals(target.view, null, "No view expected.");
    } // testLink()


    @Test
    public void testStore() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/store");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        String resourceId = "RootTopic:1";
        TargetDescriptor target = null;
        request.addParameter("title", "Test root topic");
        request.addParameter("teaser", "<p>Never mind!</p>");
        request.addParameter("logo", "");
        try {
            target = handler.store(resourceId, request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on storing a content element.", e);
        } // try/catch
        Assert.assertEquals(target.bean.getClass(), RootTopic.class, "Bean of given class bean expected.");
        Assert.assertEquals(target.action, "edit", "Edit action expected.");
        Assert.assertEquals(target.view, null, "No view expected.");
    } // testStore()


    @Test
    public void testDelete() {
        String resourceId = "CodeResource:8";
        Content bean = beanFactory.getBean(Content.class, resourceId);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/delete/id_"+bean.getId());
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor target = null;
        try {
            target = handler.delete(bean.getId(), request, response);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Object deletion not activated", "Expected error since deletion is deactivated.");
        } // try/catch
        handler.setDeleteMethodEnabled(true);
        try {
            target = handler.delete(bean.getId(), request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on editing a content element.", e);
        } // try/catch
        Assert.assertNotNull(target, "Non null target descriptor expected after deletion.");
        Assert.assertNotNull(target.bean, "Non null result list expected after returning from deletion.");
        Assert.assertEquals(target.action, null, "No action expected.");
        Assert.assertEquals(target.view, "tangramEditorList", "List view expected.");
        Assert.assertEquals(((Collection<?>) (target.bean)).size(), 7, "Unexpected list of contents to display.");
    } // testDelete()


    @Test
    public void testCreate() throws Exception {
        String resourceId = "CodeResource:8";
        Content bean = beanFactory.getBean(Content.class, resourceId);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/delete/id_"+bean.getId());
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        TargetDescriptor target = null;
        try {
            target = handler.create(MockContent.class.getName(), request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on creating a content element.", e);
        } // try/catch
        Assert.assertNotNull(target, "Non null target descriptor expected after deletion.");
        Assert.assertNotNull(target.bean, "Non null result list expected after returning from deletion.");
        Assert.assertEquals(target.action, "edit", "Edit action expected.");
        Assert.assertEquals(target.view, null, "No view expected.");
        Assert.assertTrue(((MockContent) target.bean).getId().startsWith("MockContent:"), "Unexpected id for created instance.");
    } // testCreate()


    @Test
    public void testNoAdminCall() {
        String[] urls = {"delete", "edit", "list"};
        for (String url : urls) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/"+url);
            request.setContextPath("/testapp");
            HttpServletResponse response = new MockHttpServletResponse();
            Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(false);
            Mockito.when(authorizationService.getLoginTarget(request)).thenReturn(DUMMY_LOGIN);
            TargetDescriptor tool = null;
            String id = "CodeResource:8";
            try {
                if ("delete".equals(url)) {
                    tool = handler.delete(id, request, response);
                } // if
                if ("edit".equals(url)) {
                    tool = handler.edit(id, request, response);
                } // if
                if ("list".equals(url)) {
                    tool = handler.list(TransientCode.class.getName(), null, null, request, response);
                } // if
            } catch (Exception e) {
                Assert.fail("Editing handler should not issue exceptions when redirecting to login for "+url+".", e);
            } // try/catch
            Assert.assertEquals(tool, DUMMY_LOGIN, "Non logged in calls should issue login redirect for "+url+".");
        } // for
    } // testNoAdminCall()

} // EditingHandlerTest
