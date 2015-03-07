/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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
package org.tangram.spring;

import java.util.Set;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.tangram.monitor.Statistics;


/**
 * The time measuring interceptor collects the average request handling times for any intercepted call.
 *
 * The result is calculated by means of the tangram statistics facility and a set of URLs to be ignored
 * can be filled with URIs if needed.
 */
public class MeasureTimeInterceptor extends HandlerInterceptorAdapter {

    @Resource(name="freeUrls")
    private Set<String> freeUrls;

    @Inject
    private Statistics statistics;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("start.time", System.currentTimeMillis());
        return super.preHandle(request, response, handler);
    } // preHandle()


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        String thisURL = request.getRequestURI();
        if (!freeUrls.contains(thisURL)) {
            Long startTime = (Long) request.getAttribute("start.time");
            statistics.avg("page render time", System.currentTimeMillis()-startTime);
        } // if
        super.afterCompletion(request, response, handler, ex);
    } // afterCompletion()

} // MeasureTimeInterceptor
