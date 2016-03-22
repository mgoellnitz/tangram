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

import java.util.List;
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
import org.tangram.components.test.GroovyClassRepositoryTest;
import org.tangram.content.Content;
import org.tangram.content.TransientCode;
import org.tangram.content.test.MockContent;
import org.tangram.link.Link;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.tangram.logic.ClassRepository;
import org.tangram.mock.MockMutableBeanFactory;
import org.tangram.mock.MockOrmManager;
import org.tangram.protection.AuthorizationService;
import org.tangram.view.GenericPropertyConverter;
import org.tangram.view.PropertyConverter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class EditingHandlerTest {

    private static final String DUMMY_ID = "pling:plong";

    @Spy
    private PropertyConverter propertyConverter = new GenericPropertyConverter();

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private LinkHandlerRegistry linkHandlerRegistry;

    @Spy
    private MockMutableBeanFactory beanFactory = new MockMutableBeanFactory();

    @Spy
    private ClassRepository repository;

    @InjectMocks
    private EditingHandler handler = new EditingHandler();


    /**
     * Mock the name pattern of generated ORM classes.
     */
    private class Content$Test extends MockContent {

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
            Assert.assertEquals(link.getUrl(), "/"+a, "The generation of "+a+" action link with strange result.");
        } // for

        for (String a : EditingHandler.ID_URL_ACTIONS) {
            Link link = handler.createLink(null, null, c, a, null);
            Assert.assertNotNull(link, "The generation of "+a+" action link failed.");
            Assert.assertEquals(link.getUrl(), "/"+a+"/id_"+DUMMY_ID, "The generation of "+a+" action link with strange result.");
        } // for
    } // testUrlGeneration()


    @Test
    public void testList() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/list");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        try {
            handler.list(TransientCode.class.getName(), null, null, request, response);
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
        Assert.assertEquals(((List<?>) ormClasses).size(), 1, "Fixed number of classes in list expected.");
        Object me = request.getAttribute(Constants.THIS);
        Assert.assertNotNull(me, "There should be some instances list object..");
        Assert.assertEquals(((List<?>) me).size(), 6, "Fixed number of instances in list expected.");
    } // testList()


    @Test
    public void testEdit() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/edit");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor edit = null;
        String resourceId = "CodeResource:8";
        Content bean = beanFactory.getBean(Content.class, resourceId);
        try {
            edit = handler.edit(resourceId, request, response);
        } catch (Exception e) {
            Assert.fail("No exception expected on editing a content element.", e);
        } // try/catch
        Assert.assertEquals(request.getAttribute("beanFactory"), beanFactory, "Mock bean factory instance expected.");
        Assert.assertEquals(request.getAttribute("cmprefix"), "/testapp/editor/codemirror", "URI path to codemirror expected.");
        Assert.assertEquals(request.getAttribute("ckprefix"), "/testapp/editor/ckeditor", "URI path to codemirror expected.");
        Assert.assertEquals(request.getAttribute("propertyConverter"), propertyConverter, "Property converter expected.");
        Assert.assertEquals(edit.bean, bean, "Prepared bean expected.");
        Assert.assertEquals(edit.action, null, "No action expected.");
        Assert.assertEquals(edit.view, "edit", "Edit view expected.");
    } // testList()

} // EditingHandlerTest
