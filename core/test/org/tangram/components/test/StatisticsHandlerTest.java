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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.Constants;
import org.tangram.components.SimpleStatistics;
import org.tangram.components.StatisticsHandler;
import org.tangram.link.TargetDescriptor;
import org.tangram.monitor.Statistics;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test default handler elements.
 */
public class StatisticsHandlerTest {

    @Mock
    private final Statistics statistics = new SimpleStatistics();

    @InjectMocks
    private final StatisticsHandler statisticsHandler = new StatisticsHandler();


    @Test
    public void testStatisticsHandler() {
        MockitoAnnotations.initMocks(this);
        boolean result = false;
        try {
            statisticsHandler.afterPropertiesSet();
        } catch (RuntimeException e) {
            result = true;
        } // try
        Assert.assertEquals(result, result, "Intentionally missing dependency should result in an Exception.");
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        TargetDescriptor target = statisticsHandler.stats(request, response);
        Assert.assertEquals(response.getCharacterEncoding(), "utf-8", "Result has unexpected character encoding.");
        Assert.assertEquals(response.getContentType(), Constants.MIME_TYPE_HTML, "Result has unexpected content type.");
        Assert.assertEquals(target.getBean(), statistics, "Bean to view is statistics themselves.");
        Assert.assertNull(target.getAction(), "Statistics should be viewed in default view.");
        Assert.assertNull(target.getView(), "Statistics should be viewed in default view.");
    } // StatisticsHandler()

} // StatisticsHandlerTest
