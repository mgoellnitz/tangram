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
package org.tangram.components.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
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


@Named
@Singleton
@LinkHandler
public class GaeToolHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GaeToolHandler.class);

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private Statistics statistics;


    @LinkAction("/clear/sessions")
    public TargetDescriptor clearSessions(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getParameter(Constants.ATTRIBUTE_ADMIN_USER)==null) {
            throw new Exception("User may not clear cache");
        } // if

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("_ah_SESSION");
        query.setFilter(new Query.FilterPredicate("_expires", FilterOperator.LESS_THAN, new Long(System.currentTimeMillis())));
        PreparedQuery results = datastore.prepare(query);
        FetchOptions limit = FetchOptions.Builder.withLimit(10000);
        LOG.info("clearSessions() deleting {} sessions from data store", results.countEntities(limit));
        for (Entity session : results.asIterable()) {
            datastore.delete(session.getKey());
        } // for
        if (LOG.isInfoEnabled()) {
            query = new Query("_ah_SESSION");
            results = datastore.prepare(query);
            LOG.info("clearSessions() {} sessions still available", results.countEntities(limit));
        } // if

        return new TargetDescriptor(statistics, null, null);
    } // clearSessions()


    @PostConstruct
    public void afterPropertiesSet() {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // GaeToolHandler
