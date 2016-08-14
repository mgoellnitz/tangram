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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.annotate.ActionForm;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.components.MetaLinkHandler;
import org.tangram.components.SimpleStatistics;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanFactoryAware;
import org.tangram.controller.ControllerHook;
import org.tangram.link.GenericLinkFactoryAggregator;
import org.tangram.link.Link;
import org.tangram.link.TargetDescriptor;
import org.tangram.logic.ClassRepository;
import org.tangram.mock.content.MockBeanFactory;
import org.tangram.monitor.Statistics;
import org.tangram.view.DynamicViewContextFactory;
import org.tangram.view.GenericPropertyConverter;
import org.tangram.view.PropertyConverter;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test behaviour of the meta link handler.
 */
public class MetaLinkHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetaLinkHandlerTest.class);

    private static final Link ACTION_LINK = new Link("/actionlinkresult");

    @Spy
    private final MockBeanFactory beanFactory;

    @Spy
    private final Statistics statistics = new SimpleStatistics(); // NOPMD - this field is not really unused

    @Spy
    private final PropertyConverter propertyConverter = new GenericPropertyConverter(); // NOPMD - this field is not really unused

    @Spy
    @InjectMocks
    private final DynamicViewContextFactory viewContextFactory = new DynamicViewContextFactory();

    @Spy
    @InjectMocks
    private final GenericLinkFactoryAggregator aggregator = new GenericLinkFactoryAggregator();

    @Spy
    private ClassRepository repository; // NOPMD - this field is not really unused

    @Spy
    private final Set<ControllerHook> controllerHooks = new HashSet<>();

    @Mock
    private ViewUtilities viewUtilities; // NOPMD - this field is not really unused

    @InjectMocks
    private final MetaLinkHandler metaLinkHandler = new MetaLinkHandler();

    private final TestHandler testHandler = new TestHandler();


    /**
     * Mock test class to bring bean factory aware and line handler together like in real world code.
     */
    public class MockLinkHandler implements org.tangram.link.LinkHandler, BeanFactoryAware {

        private BeanFactory<?> beanFactory;


        @Override
        public void setBeanFactory(BeanFactory<?> factory) {
            beanFactory = factory;
            LOG.debug("MockLinkHandler.setBeanFactory() {} {}", beanFactory, factory);
        }


        public BeanFactory<?> getBeanFactory() {
            LOG.info("MockLinkHandler.getBeanFactory() {}", beanFactory);
            return beanFactory;
        }


        @Override
        public TargetDescriptor parseLink(String url, HttpServletResponse response) {
            return null;
        }


        @Override
        public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
            LOG.debug("MockLinkHandler.createLink() {} {} {}", bean, action, view);
            if ("viewit".equals(bean)) {
                if ("DoIt".equals(action)) {
                    return ACTION_LINK;
                }
            }
            return null;
        } // createLink()

    };


    /**
     * Test implementation for a statically registered handler using annotations.
     */
    @LinkHandler
    public class AtHandler {

        @LinkAction("/athandler")
        public TargetDescriptor render(@ActionParameter(value = "a") String ap, @ActionForm FormBean af, HttpServletRequest req) {
            return new TargetDescriptor(req.getAttribute("self"), ap+af.getProperty(), null);
        } // render

    } // AtHandler


    /**
     * Test implementation for a statically registered handler using annotations returning an action target.
     */
    @LinkHandler
    public class AtActionHandler {

        @LinkAction("/actionhandler/(.*)")
        public TargetDescriptor render(@LinkPart(1) String lp, HttpServletRequest req, HttpServletResponse resp) {
            return new TargetDescriptor(req.getAttribute("self"), null, lp);
        } // render

    } // AtActionHandler


    /**
     * Instance carrying methods to react to HTTP calls.
     */
    public class ActionMethods {

        @LinkAction
        public TargetDescriptor method() {
            return new TargetDescriptor(this, "method", null);
        } // method()

    } // ActionMethods


    /**
     * Test implementation for a statically registered handler using interface implementation.
     */
    public class TestHandler implements org.tangram.link.LinkHandler {

        @Override
        public TargetDescriptor parseLink(String url, HttpServletResponse response) {
            LOG.debug("TestHandler.parseLink() {}", url);
            TargetDescriptor view = new TargetDescriptor(response, "linkHandler", null);
            TargetDescriptor call = new TargetDescriptor(this, null, "methodAtHandler");
            TargetDescriptor method = new TargetDescriptor(new ActionMethods(), null, "method");
            return "/testhandler".equals(url) ? view : "/testcall".equals(url) ? call : "/testmethod".equals(url) ? method : null;
        }


        @Override
        public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
            return null;
        }


        @LinkAction
        public TargetDescriptor methodAtHandler() {
            return new TargetDescriptor(this, "method", null);
        } // method()

    } // TestHandler


    public MetaLinkHandlerTest() throws Exception {
        aggregator.setDispatcherPath("/testapp");

        repository = new GroovyClassRepositoryTest().getInstance();
        beanFactory = MockBeanFactory.getInstance();
        MockitoAnnotations.initMocks(this);
        viewContextFactory.afterPropertiesSet();
        metaLinkHandler.afterPropertiesSet();
        AtHandler atHandler = new AtHandler();
        metaLinkHandler.registerLinkHandler(atHandler);
        AtActionHandler atActionHandler = new AtActionHandler();
        metaLinkHandler.registerLinkHandler(atActionHandler);
        metaLinkHandler.registerLinkHandler(testHandler);
    } // ()


    @Test
    public void testLinkCreation() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Link link = metaLinkHandler.createLink(request, response, this, null, null);
        Assert.assertNull(link, "Should not be able to create link for test instance.");
    } // testLinkCreation()


    @Test
    public void testHandleRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_46");
        HttpServletResponse response = new MockHttpServletResponse();
        try {
            ViewContext context = metaLinkHandler.handleRequest(request, response);
            Assert.assertNull(context, "The should be no view context in the result.");
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
    } // testHandleRequest()


    @Test
    public void testAtHandler() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/athandler;jsessionid=1234");
        request.setAttribute("self", "viewit");
        request.setParameter("a", "Hallo");
        request.setParameter("property", "Tangram");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertEquals(response.getStatus(), 200, "Normal response status expected.");
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getViewName(), "HalloTangram", "Didn't find expected view name.");
        Assert.assertEquals(context.getModel().get("self"), "viewit", "Didn't find expected model bean.");
    } // testAtHandler()


    @Test(priority = 1)
    public void testFailingActionTargetResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/actionhandler/DoIt");
        request.setAttribute("self", "viewit");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertEquals(response.getStatus(), 200, "Normal response status expected.");
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getModel().get("self"), "viewit", "Didn't find expected model bean.");
        Assert.assertEquals(context.getViewName(), "NULL", "Failing action redirect should have certain view.");
    } // testFailingActionTargetResponse()


    @Test(priority = 2)
    public void testActionTargetResponse() {
        String bean = "viewit";
        String action = "DoIt";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/actionhandler/"+action);
        request.setAttribute("self", bean);
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        MockLinkHandler mockLinkHandler = new MockLinkHandler();
        metaLinkHandler.registerLinkHandler(mockLinkHandler);
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertNotNull(mockLinkHandler.getBeanFactory(), "Expected to see a bean factory at the instance.");
        Assert.assertEquals(mockLinkHandler.getBeanFactory(), beanFactory, "Expected mock bean factory instance.");
        Assert.assertEquals(response.getStatus(), 302, "Redirect expected.");
        Assert.assertNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(response.getHeader("Location"), ACTION_LINK.getUrl(), "Unexpected redirect url detected.");
    } // testActionTargetResponse()


    @Test
    public void testLinkHandler() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/testhandler");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        controllerHooks.clear();
        ControllerHook hookOne = Mockito.mock(ControllerHook.class);
        controllerHooks.add(hookOne);
        ControllerHook hookTwo = Mockito.mock(ControllerHook.class);
        controllerHooks.add(hookTwo);
        try {
            Map<String, Object> model = viewContextFactory.createModel(response, request, response);
            TargetDescriptor target = new TargetDescriptor(response, "linkHandler", null);
            Mockito.when(hookTwo.intercept(target, model, request, response)).thenReturn(false);
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getViewName(), "linkHandler", "Didn't find expected view context.");
        Assert.assertEquals(context.getModel().get("self"), response, "Didn't find expected view context.");
    } // testLinkHandler()


    @Test
    public void testLinkHandlerAction() {
        LOG.info("testLinkHandlerAction()");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/testcall");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        LOG.info("testLinkHandlerAction() end");
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getViewName(), "method", "Didn't find expected view context.");
        Assert.assertEquals(context.getModel().get("self"), testHandler, "Didn't find expected self instance.");
    } // testLinkHandlerAction()


    @Test
    public void testExternalAction() {
        LOG.info("testExternalAction()");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/testmethod");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        LOG.info("testExternalAction() end");
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getViewName(), "method", "Didn't find expected view context.");
        Object self = context.getModel().get("self");
        Assert.assertNotNull(self, "Didn't find any self object in view context.");
        Assert.assertEquals(self.getClass(), ActionMethods.class, "Didn't find expected type for self.");
    } // testExternalAction()


    @Test
    public void testControllerHookInterception() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/testhandler");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        controllerHooks.clear();
        ControllerHook hook = Mockito.mock(ControllerHook.class);
        controllerHooks.add(hook);
        try {
            Map<String, Object> model = viewContextFactory.createModel(response, request, response);
            TargetDescriptor target = new TargetDescriptor(response, "linkHandler", null);
            Mockito.when(hook.intercept(target, model, request, response)).thenReturn(true);
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertNull(context, "We expected to get no view context result instance due to interception.");
    } // testControllerHookInterception()

} // MetaLinkHandlerTest
