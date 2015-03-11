/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
package org.tangram.components;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.monitor.Statistics;
import org.tangram.view.TargetDescriptor;


/**
 * Handler implementation to view the contents of the stats instance of event counters and calculated average.
 */
@Named
@Singleton
@LinkHandler
public class StatisticsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsHandler.class);

    public static final String STATS_URI = "/stats";

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private Statistics statistics;


    @LinkAction("/stats")
    public TargetDescriptor stats(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("stats()");
        response.setContentType(Constants.MIME_TYPE_HTML);
        response.setCharacterEncoding("utf-8");
        return new TargetDescriptor(statistics, null, null);
    } // stats()


    @PostConstruct
    public void afterPropertiesSet() {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // StatisticsHandler
