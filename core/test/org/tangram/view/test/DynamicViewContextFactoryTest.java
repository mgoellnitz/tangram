/**
 *
 * Copyright 2015-2016 Martin Goellnitz
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
package org.tangram.view.test;

import java.lang.reflect.Constructor;
import java.util.Map;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.tangram.Constants;
import org.tangram.logic.Shim;
import org.tangram.logic.test.BeanShim;
import org.tangram.logic.test.ProviderAwareBeanShim;
import org.tangram.logic.test.ProviderAwareViewShim;
import org.tangram.logic.test.ViewShim;
import org.tangram.logic.test.WrongConstructorBeanShim;
import org.tangram.logic.test.WrongConstructorViewShim;
import org.tangram.mock.content.MockContent;
import org.tangram.util.SystemUtils;
import org.tangram.view.DynamicViewContextFactory;
import org.tangram.view.ViewContext;
import org.testng.Assert;
import org.testng.annotations.Test;


public class DynamicViewContextFactoryTest {

    /**
     * Derived test class to avoid dependencies.
     */
    private class TestFactory extends DynamicViewContextFactory {

        @Override
        public void afterPropertiesSet() {
            super.afterPropertiesSet();
            Constructor<Shim> beanShim = SystemUtils.convert(BeanShim.class.getConstructors()[0]);
            Constructor<Shim> viewShim = SystemUtils.convert(ViewShim.class.getConstructors()[0]);
            this.defineBeanShim(MockContent.class, beanShim);
            this.defineViewShim(MockContent.class, viewShim);
            beanShim = SystemUtils.convert(WrongConstructorBeanShim.class.getConstructors()[0]);
            viewShim = SystemUtils.convert(WrongConstructorViewShim.class.getConstructors()[0]);
            this.defineBeanShim(MockContent.class, beanShim);
            this.defineViewShim(MockContent.class, viewShim);
            beanShim = SystemUtils.convert(ProviderAwareBeanShim.class.getConstructors()[0]);
            viewShim = SystemUtils.convert(ProviderAwareViewShim.class.getConstructors()[0]);
            this.defineBeanShim(MockContent.class, beanShim);
            this.defineViewShim(MockContent.class, viewShim);
        } // afterPropertiesSet()

    } // TestFactory


    @Test
    public void testViewContextCreation() {
        DynamicViewContextFactory factory = new TestFactory();
        factory.afterPropertiesSet();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        MockContent bean = new MockContent();

        Map<String, Object> shims = factory.getShims(request, bean);
        Assert.assertEquals(shims.size(), 4, "Unexpected number of shims discovered.");
        for (Object so : shims.values()) {
            Assert.assertFalse(so instanceof WrongConstructorBeanShim, "Those instances should have been left out.");
            Assert.assertFalse(so instanceof WrongConstructorViewShim, "Those instances should have been left out.");
            if (so instanceof Shim) {
                Shim s = (Shim) so;
                Assert.assertTrue(s.getId().startsWith("MockContent:1"), "Unexpected mock bean id discovered.");
                if (s instanceof ViewShim) {
                    ViewShim vs = (ViewShim) s;
                    Assert.assertEquals(vs.getRequest(), request, "Unexpected mock bean id discovered.");
                    Assert.assertNull(vs.getSession(), "Unexpected session found.");
                    vs.setSession(session);
                    Assert.assertEquals(vs.getSession(), session, "Unexpected session found.");
                    vs.setRequest(request);
                    Assert.assertNull(vs.getSession(), "Unexpected session found.");
                    if (s instanceof ProviderAwareViewShim) {
                        ProviderAwareViewShim pavs = (ProviderAwareViewShim) s;
                        Assert.assertEquals(pavs.getShimProvider(), factory, "Factory should be the shim provider for instances.");
                    } // if
                } // if
                if (s instanceof ProviderAwareBeanShim) {
                    ProviderAwareBeanShim pabs = (ProviderAwareBeanShim) s;
                    Assert.assertEquals(pabs.getShimProvider(), factory, "Factory should be the shim provider for instances.");
                } // if
            } // if
        } // for
        MockHttpServletResponse response = new MockHttpServletResponse();
        ViewContext viewContext = factory.createViewContext(bean, request, response);
        Assert.assertEquals(viewContext.getViewName(), Constants.DEFAULT_VIEW, "Null view expected.");
        Assert.assertEquals(viewContext.getModel().size(), 8, "Unexpected number of beans in model discovered.");
    } // testViewContextCreation()

} // DynamicViewContextFactoryTest
