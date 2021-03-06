/**
 *
 * Copyright 2011-2020 Martin Goellnitz
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

import java.net.URLDecoder;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.controller.ControllerHook;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.TargetDescriptor;


/**
 * Controller hook to ensure a unique URL usage on every request.
 * It checks every request if it is delivered through a unique URL and triggers a redirect
 * if the content described by this URL should be delivered through another one.
 */
@Named
@Singleton
public class UniqueUrlHook implements ControllerHook {

    private static final Logger LOG = LoggerFactory.getLogger(UniqueUrlHook.class);

    @Inject
    private LinkFactoryAggregator linkFactory;


    @Override
    public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Link link = null;
        try {
            link = linkFactory.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("intercept() expensive things happen "+((descriptor.bean==null) ? "" : descriptor.bean.getClass().getName()));
            } // if
        } // try/catch
        if (link!=null) {
            String queryString = request.getQueryString();
            queryString = StringUtils.isBlank(queryString) ? "" : "?"+queryString;
            // If you run into trouble with encodings, this might be a place to search
            // String decodedUrl = link.getUrl()+queryString;
            // String requestURI = request.getRequestURI()+queryString;
            String decodedUrl = URLDecoder.decode(link.getUrl(), "UTF-8")+queryString;
            String requestURI = URLDecoder.decode(request.getRequestURI(), "UTF-8")+queryString;
            if (!decodedUrl.equals(requestURI)) {
                LOG.info("intercept() sending redirect for {} to {}", requestURI, decodedUrl);
                response.setHeader("Location", link.getUrl());
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                return true;
            } // if
        } // if
        return false;
    } // intercept()

} // UniqueUrlHook
