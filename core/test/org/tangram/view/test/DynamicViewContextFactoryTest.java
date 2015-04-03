/**
 *
 * Copyright 2015 Martin Goellnitz
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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.Constants;
import org.tangram.content.test.BeanClass;
import org.tangram.logic.Shim;
import org.tangram.logic.test.BeanShim;
import org.tangram.logic.test.ViewShim;
import org.tangram.util.SystemUtils;
import org.tangram.view.DynamicViewContextFactory;
import org.tangram.view.ViewContext;


public class DynamicViewContextFactoryTest {

    private class TestFactory extends DynamicViewContextFactory {

        @Override
        public void afterPropertiesSet() {
            super.afterPropertiesSet();
            Constructor<Shim> beanShim = SystemUtils.convert(BeanShim.class.getConstructors()[0]);
            Constructor<Shim> viewShim = SystemUtils.convert(ViewShim.class.getConstructors()[0]);
            this.defineBeanShim(BeanClass.class, beanShim);
            this.defineViewShim(BeanClass.class, viewShim);
        } // afterPropertiesSet()

    } // TestFactory


    @Test
    public void testViewContextCreation() {
        DynamicViewContextFactory factory = new TestFactory();
        factory.afterPropertiesSet();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BeanClass bean = new BeanClass();

        Map<String, Object> shims = factory.getShims(request, bean);
        Assert.assertEquals("Expected no shims at all", 2, shims.size());
        ViewContext viewContext = factory.createViewContext(bean, request, response);
        Assert.assertEquals("Null view expected", Constants.DEFAULT_VIEW, viewContext.getViewName());
        Assert.assertEquals("Unexpected number of beans in model", 6, viewContext.getModel().size());
    } // testViewContextCreation()

} // DynamicViewContextFactoryTest
