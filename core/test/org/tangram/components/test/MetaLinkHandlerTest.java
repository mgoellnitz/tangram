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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.components.MetaLinkHandler;
import org.tangram.components.SimpleStatistics;
import org.tangram.link.GenericLinkFactoryAggregator;
import org.tangram.link.Link;
import org.tangram.link.TargetDescriptor;
import org.tangram.logic.ClassRepository;
import org.tangram.monitor.Statistics;
import org.tangram.view.DynamicViewContextFactory;
import org.tangram.view.GenericPropertyConverter;
import org.tangram.view.PropertyConverter;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewUtilities;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test behaviour of the meta link handler.
 */
public class MetaLinkHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetaLinkHandlerTest.class);

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

    @Mock
    private ViewUtilities viewUtilities; // NOPMD - this field is not really unused

    @InjectMocks
    private final MetaLinkHandler metaLinkHandler = new MetaLinkHandler();


    /**
     * Test implementation for a statically registered handler using annotations.
     */
    @LinkHandler
    public class AtHandler {

        @LinkAction("/athandler/(.*)")
        public TargetDescriptor render(@LinkPart(1) String view, @ActionParameter(value = "a") String action, HttpServletRequest request, HttpServletResponse response) {
            return new TargetDescriptor(request.getAttribute("self"), view+action, null);
        } // render

    } // AtHandler


    /**
     * Test implementation for a statically registered handler using interface implementation.
     */
    public class TestHandler implements org.tangram.link.LinkHandler {

        @Override
        public TargetDescriptor parseLink(String url, HttpServletResponse response) {
            LOG.debug("TestHandler.parseLink() {}", url);
            return "/testhandler".equals(url) ? new TargetDescriptor(response, "linkHandler", null) : null;
        }


        @Override
        public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
            return null;
        }

    } // TestHandler


    @BeforeClass
    public void init() throws Exception {
        aggregator.setDispatcherPath("/testapp");
        GroovyClassRepositoryTest groovyClassRepositoryTest = new GroovyClassRepositoryTest();
        groovyClassRepositoryTest.init();
        repository = groovyClassRepositoryTest.getInstance();
        MockitoAnnotations.initMocks(this);
        viewContextFactory.afterPropertiesSet();
        metaLinkHandler.afterPropertiesSet();
        AtHandler atHandler = new AtHandler();
        metaLinkHandler.registerLinkHandler(atHandler);
        TestHandler testHandler = new TestHandler();
        metaLinkHandler.registerLinkHandler(testHandler);
    } // init()


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
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/athandler/Hallo");
        request.setAttribute("self", "viewit");
        request.setParameter("a", "Tangram");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getViewName(), "HalloTangram", "Didn't find expected view name.");
        Assert.assertEquals(context.getModel().get("self"), "viewit", "Didn't find expected model bean.");
    } // testAtHandler()


    @Test
    public void testLinkHandler() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/testhandler");
        HttpServletResponse response = new MockHttpServletResponse();
        ViewContext context = null;
        try {
            context = metaLinkHandler.handleRequest(request, response);
        } catch (Throwable t) {
            Assert.fail("Cannot handle request.", t);
        } // try/catch
        Assert.assertNotNull(context, "We expected to get a view context result instance.");
        Assert.assertEquals(context.getViewName(), "linkHandler", "Didn't find expected view context.");
        Assert.assertEquals(context.getModel().get("self"), response, "Didn't find expected view context.");
    } // testLinkHandler()

} // MetaLinkHandlerTest
