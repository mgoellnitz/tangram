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

import java.util.Map;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.components.SimpleStatistics;
import org.tangram.components.spring.MetaController;
import org.tangram.components.spring.TangramSpringServices;
import org.tangram.components.spring.TangramViewHandler;
import org.tangram.content.BeanFactory;
import org.tangram.spring.MeasureTimeInterceptor;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test different aspects of the spring chain of view handling.
 */
public class SpringChainTest {

    private static final Logger LOG = LoggerFactory.getLogger(SpringChainTest.class);

    @Spy
    private final MockServletContext servletContext = new MockServletContext("."); // NOPMD - this field is not really unused

    private final ApplicationContext appContext;


    public SpringChainTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setServletContext(servletContext);
        context.setConfigLocations("/tangram/tangram-configurer.xml", "/tangram/mutable-configurer.xml", "/tangram/tangram-test-configurer.xml");
        context.afterPropertiesSet();
        LOG.info("init() # of beans is {}.", context.getBeanDefinitionCount());
        appContext = context;
    } // ()


    @Test
    public void testServletViewUtilities() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");

        Object bean = new Throwable("Test Code");
        ViewContextFactory viewContextFactory = appContext.getBean(ViewContextFactory.class);
        Map<String, Object> model = viewContextFactory.createModel(bean, request, response);

        ViewUtilities viewUtilities = appContext.getBean(ViewUtilities.class);
        viewUtilities.render(null, model, null);
        Assert.assertEquals(response.getContentAsString(), "", "The result is empty for mock instances.");

        TangramViewHandler viewHandler = appContext.getBean(TangramViewHandler.class);
        Assert.assertTrue(viewHandler.isDetectAllModelAwareViewResolvers(), "All view resolvers should be considered.");
        viewHandler.setDetectAllModelAwareViewResolvers(false);
        Assert.assertFalse(viewHandler.isDetectAllModelAwareViewResolvers(), "All view handler recognition should be turned off.");
    } // testServletViewUtilities()


    @Test
    public void testCreateParameterAccess() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");
        ViewUtilities viewUtilities = appContext.getBean(ViewUtilities.class);
        RequestParameterAccess parameterAccess = viewUtilities.createParameterAccess(request);
        Assert.assertNotNull(parameterAccess, "Access instance should have been created.");
    } // testCreateParameterAccess()


    @Test
    public void testFormEncodedParameterAccess() throws Exception {
        MockServletContext context = new MockServletContext();
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(context);
        request.setMethod("POST");
        request.setRequestURI("/testapp/id_RootTopic:1");
        // request.setContentType("multipart/form-data; boundary=----------tangram");
        byte[] content = "Please test for these contents here.".getBytes("UTF-8");
        MockMultipartFile f = new MockMultipartFile("file", "testfile.txt", "text/plain", content);
        request.addFile(f);
        request.setParameter("field", "content of the field");
        Assert.assertTrue(ServletFileUpload.isMultipartContent(request), "Multipart request expected.");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");
        ViewUtilities viewUtilities = appContext.getBean(ViewUtilities.class);
        RequestParameterAccess parameters = viewUtilities.createParameterAccess(request);
        Assert.assertNotNull(parameters, "Access instance should have been created.");
        Assert.assertEquals(parameters.getParameterMap().size(), 1, "Unexpected number of parameters.");
        Assert.assertEquals(parameters.getBlobNames().size(), 1, "Expected one available blob in the request parameters.");
        Assert.assertEquals(parameters.getParameter("field"), "content of the field", "Unexpected field value");
        Assert.assertEquals(parameters.getData("file").length, 36, "Unexpected file size");
        Assert.assertEquals(parameters.getOriginalName("file"), "testfile.txt", "Unexpected file name");
    } // testFormEncodedParameterAccess()


    @Test
    public void testSpringServices() throws Exception {
        Assert.assertEquals(TangramSpringServices.getApplicationContext(), appContext, "Expected the hand made context.");
        boolean result = false;
        try {
            Object bean = new Object();
            BeanWrapper wrap = TangramSpringServices.createWrapper(bean);
            Assert.assertNull(wrap, "Bean wrapping with spring is not (yet) used.");
        } catch (ExceptionInInitializerError e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "Bean wrapping with spring is not (yet) used.");
        BeanFactory beanFactory = appContext.getBean(BeanFactory.class);
        Assert.assertEquals(TangramSpringServices.getBeanFromContext(BeanFactory.class), beanFactory, "Didn't find mock bean factory.");
        ViewUtilities viewUtilities = appContext.getBean(ViewUtilities.class);
        ViewUtilities beanFromContext = TangramSpringServices.getBeanFromContext(ViewUtilities.class, "viewUtilities");
        Assert.assertEquals(beanFromContext, viewUtilities, "Didn't find expected view utiities.");
    } // testSpringServices()


    @Test
    public void testMetaController() throws Exception {
        MetaController controller = TangramSpringServices.getBeanFromContext(MetaController.class);
        Assert.assertNotNull(controller, "Need a meta controller to do the test.");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");
        // TODO: mock enough stuff to do a real test here.
        ModelAndView result = controller.handleRequest(request, response);
        Assert.assertNull(result, "Meta controller should issue a result.");
    } // testMetaController()


    @Test
    public void testMeasureTimeInterceptor() throws Exception {
        ApplicationContext applicationContext = TangramSpringServices.getApplicationContext();
        SimpleStatistics statistics = applicationContext.getBean(SimpleStatistics.class);
        Assert.assertNotNull(statistics, "Need a statistics instance to do the test.");
        MeasureTimeInterceptor interceptor = applicationContext.getBean(MeasureTimeInterceptor.class);
        Assert.assertNotNull(interceptor, "Need a time measring interceptor to do the test.");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");
        Long avg = statistics.getCounter().get("page render time");
        Assert.assertNull(avg, "There should be no average page render time before test.");
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);
        avg = statistics.getCounter().get("page render time");
        Assert.assertNotNull(avg, "There should be an average page render time after test.");
        // Assert.assertEquals((long) avg, 0L, "There should be an average page render time of 0.");
    } // testMeasureTimeInterceptor()

} // SpringChainTest
