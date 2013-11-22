/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.components.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;
import org.tangram.annotate.LinkAction;
import org.tangram.link.Link;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.monitor.Statistics;
import org.tangram.view.TargetDescriptor;


@Named
public class GaeToolHandler implements LinkHandler {

    private static final Log log = LogFactory.getLog(GaeToolHandler.class);

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private Statistics statistics;


    @LinkAction
    public TargetDescriptor clearSessions(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getParameter(Constants.ATTRIBUTE_ADMIN_USER)==null) {
            throw new Exception("User may not clear cache");
        } // if

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("_ah_SESSION");
        query.setFilter(new Query.FilterPredicate("_expires", FilterOperator.LESS_THAN, new Long(System.currentTimeMillis())));
        PreparedQuery results = datastore.prepare(query);
        FetchOptions limit = FetchOptions.Builder.withLimit(10000);
        if (log.isInfoEnabled()) {
            log.info("clearSessions() deleting "+results.countEntities(limit)+" sessions from data store");
        } // if
        for (Entity session : results.asIterable()) {
            datastore.delete(session.getKey());
        } // for
        if (log.isInfoEnabled()) {
            query = new Query("_ah_SESSION");
            results = datastore.prepare(query);
            log.info("clearSessions() "+results.countEntities(limit)+" sessions still available");
        } // if

        return new TargetDescriptor(statistics, null, null);
    } // clearSessions()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        return null;
    } // createLink()



    @Override
    public Collection<String> getCustomViews() {
        return Collections.emptySet();
    } // getCustomViews()


    @Override
    public TargetDescriptor parseLink(String uri, HttpServletResponse response) {
        TargetDescriptor result = null;
        if ("/clean/sessions".equals(uri)) {
            result = new TargetDescriptor(this, null, "clearSessions");
        } // if
        return result;
    } // parseLink()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // GaeToolHandler
