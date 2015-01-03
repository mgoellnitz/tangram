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
package org.tangram.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.monitor.Statistics;
import org.tangram.util.StringUtil;


/**
 * The time measuring filter collects the average request handling times for any intercepted call.
 *
 * The result is calculated by means of the tangram statistics facility and a set of URLs to be ignored
 * can be filled with URIs if needed.
 *
 * The filter can be instanciated as a DI component but the configuration is held in static members.
 * So the separate instanciation from the web.xml get's the injected values and a mix of injection and static
 * configuration is possible in any environment.
 */
public class MeasureTimeFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(MeasureTimeFilter.class);

    private ServletContext context;

    private Set<String> freeUrls = new HashSet<>();


    public void setFreeUrls(Set<String> freeUrls) {
        this.freeUrls = freeUrls;
    }


    @Override
    public void destroy() {
    } // destroy()


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String thisURL = request.getRequestURI();
        long startTime = System.currentTimeMillis();
        request.setAttribute("start.time", startTime);
        chain.doFilter(request, response);
        if (!freeUrls.contains(thisURL)) {
            Statistics statistics = (Statistics) context.getAttribute(Constants.ATTRIBUTE_STATISTICS);
            LOG.debug("doFilter() statistics instance {}", statistics);
            statistics.avg("page render time", System.currentTimeMillis()-startTime);
        } // if
    } // doFilter()


    @Override
    @SuppressWarnings("rawtypes")
    public void init(FilterConfig config) throws ServletException {
        context = config.getServletContext();
        freeUrls.addAll(StringUtil.stringSetFromParameterString(config.getInitParameter("free.urls")));
        LOG.info("init() free urls {}", freeUrls);
        LOG.info("init() context {}", context);
    } // init()

} // MeasureTimeFilter
