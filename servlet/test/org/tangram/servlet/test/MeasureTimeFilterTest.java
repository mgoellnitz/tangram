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
package org.tangram.servlet.test;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.tangram.Constants;
import org.tangram.components.SimpleStatistics;
import org.tangram.monitor.Statistics;
import org.tangram.servlet.MeasureTimeFilter;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the measure time servlet filter.
 */
public class MeasureTimeFilterTest {

    @Test
    public void testMeasureTimeFilter() {
        MeasureTimeFilter measureTimeFilter = new MeasureTimeFilter();
        Statistics statistics = new SimpleStatistics();

        ServletContext context = new MockServletContext(".");
        context.setAttribute(Constants.ATTRIBUTE_STATISTICS, statistics);
        Set<String> emptyStringSet = Collections.emptySet();
        measureTimeFilter.setFreeUrls(emptyStringSet);
        FilterConfig config = new MockFilterConfig(context, "test");
        try {
            measureTimeFilter.init(config);
        } catch (ServletException e) {
            Assert.fail("Init should not fail.", e);
        } // try/catch
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();
        try {
            measureTimeFilter.doFilter(request, response, chain);
        } catch (IOException|ServletException e) {
            Assert.fail("DoFilter should not fail.", e);
        } // try/catch
    } // testMeasureTimeFilter

} // MeasureTimeFilterTest
