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
package org.tangram.controller.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.Constants;
import org.tangram.controller.AbstractLinkHandler;
import org.tangram.controller.ControllerHook;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.TargetDescriptor;
import org.tangram.view.DynamicViewContextFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test protected methods of abstract handlers directly.
 */
public class AbstractLinkHandlerTest {

    @Mock
    private LinkFactoryAggregator linkFactoryAggregator; // NOPMD - this field is not really unused

    @Spy
    private final Set<ControllerHook> controllerHooks = new HashSet<>();

    @Spy
    private final DynamicViewContextFactory viewContextFactory = new DynamicViewContextFactory();

    @InjectMocks
    private final TestLinkHandler handler = new TestLinkHandler();


    /**
     * Create derived class for test to reach protected methods.
     */
    private class TestLinkHandler extends AbstractLinkHandler {

        @Override
        public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
            throw new UnsupportedOperationException("Not needed for test.");
        }


        public Map<String, Object> execute(HttpServletRequest request, HttpServletResponse response, TargetDescriptor descriptor) throws Exception {
            return this.createModel(descriptor, request, response);
        }

    } // TestLinkHandler


    @Test
    public void testCreateModel() throws Exception {
        ControllerHook hook = new ControllerHook() {
            @Override
            public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
                return false;
            }
        };
        controllerHooks.add(hook);
        MockitoAnnotations.initMocks(viewContextFactory);
        MockitoAnnotations.initMocks(this);
        viewContextFactory.afterPropertiesSet();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        TargetDescriptor descriptor = new TargetDescriptor(this, null, null);
        Map<String, Object> result = handler.execute(request, response, descriptor);
        Assert.assertNotNull(result, "Some result map required.");
        Object self = result.get(Constants.THIS);
        Assert.assertNotNull(self, "Some result required.");
        Assert.assertEquals(self.getClass(), this.getClass(), "Unexpected result type.");
        Assert.assertNotNull(handler.getLinkFactory(), "Link factory aggretator should be present as a mock.");
    } // testCreateModel()

} // AbstractLinkHandlerTest
