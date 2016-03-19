/**
 *
 * Copyright 2011-2016 Martin Goellnitz
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
package org.tangram.controller;

import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.TargetDescriptor;


/**
 * Instances of this optional controller hook can be used to check if the content is delivered from a unique
 * host name in the calling URLs.
 *
 * In many scenarios there are alternate names for the delivering components so it is possible to configure
 * a hook of this class and select the unique host to be used in URLs - accepted and generated.
 */
public class UniqueHostHook implements ControllerHook {

    private static final Logger LOG = LoggerFactory.getLogger(UniqueHostHook.class);

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    private String primaryDomain = null;


    public void setPrimaryDomain(String primaryDomain) {
        this.primaryDomain = primaryDomain;
    }


    @Override
    public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean isOnLocalhost = request.getServerName().equals("localhost");
        LOG.debug("intercept() serverName={} isOnLocalhost={}", request.getServerName(), isOnLocalhost);
        if ((isOnLocalhost)||!(request.getServerName().equals(primaryDomain))) {
            Link redirectLink = linkFactoryAggregator.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
            response.setHeader("Location", "http://"+primaryDomain+redirectLink.getUrl());
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            return true;
        } // if
        return false;
    } // intercept()

} // UniqueHostHook
