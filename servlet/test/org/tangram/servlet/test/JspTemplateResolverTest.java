/*
 * 
 * Copyright 2014 Martin Goellnitz
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
package org.tangram.servlet.test;

import dinistiq.Dinistiq;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.tangram.Constants;
import org.tangram.components.SimpleStatistics;
import org.tangram.content.BeanFactory;
import org.tangram.monitor.Statistics;
import org.tangram.servlet.JspTemplateResolver;


/**
 * Test template lookup for JSP files.
 */
public class JspTemplateResolverTest {

    @Test
    public void testJspLookup() {
        try {
            BeanFactory beanFactory = Mockito.mock(BeanFactory.class);
            ServletContext servletContext = Mockito.mock(ServletContext.class);
            Mockito.when(servletContext.getRealPath("")).thenReturn(new File("test").getAbsolutePath());
            Map<String, Object> initialBeans = new HashMap<>();
            initialBeans.put("beansFactory", beanFactory);
            initialBeans.put("servletContext", servletContext);
            Set<String> packages = new HashSet<>();
            packages.add(SimpleStatistics.class.getPackage().getName());
            Dinistiq d = new Dinistiq(packages, initialBeans);
            JspTemplateResolver jspTemplateResolver = d.findBean(JspTemplateResolver.class);
            Assert.assertNotNull("Have no JSP template resolver to test", jspTemplateResolver);
            Map<String, Object> model = new HashMap<>();
            Statistics statistics = d.findBean(Statistics.class);
            Assert.assertNotNull("Have no object to lookup template for", jspTemplateResolver);
            model.put(Constants.THIS, statistics);
            String template = jspTemplateResolver.resolveTemplate("test", model, Locale.GERMANY);
            Assert.assertEquals("There should be a template called test.jsp", "/WEB-INF/view/jsp/org.tangram.monitor/Statistics.test.jsp", template);
            String noTemplate = jspTemplateResolver.resolveTemplate("fail", model, Locale.GERMANY);
            Assert.assertNull("There should be not template", noTemplate);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        } // try/catch
    } // testJspLookup()

} // JspTemplateResolverTest
