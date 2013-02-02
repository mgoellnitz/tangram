/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.components.gae;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.components.StatisticsController;
import org.tangram.controller.RenderingController;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@Controller
public class GaeToolController extends RenderingController {

    private static final Log log = LogFactory.getLog(GaeToolController.class);

    @Autowired
    private StatisticsController statisticsController;


    @Override
    public void setLinkFactory(LinkFactory linkFactory) {
        // Don't register with link generation - this one doesn't generate links
    } // setLinkFactory()


    @RequestMapping(value = "/clear/sessions")
    public ModelAndView clearSessions(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getParameter(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new IOException("User may not clear cache");
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

            return modelAndViewFactory.createModelAndView(statisticsController, request, response);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // clearSessions()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        return null;
    } // createLink()

} // GaeToolController
