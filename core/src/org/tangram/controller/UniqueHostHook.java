/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

public class UniqueHostHook implements ControllerHook {

    private static Log log = LogFactory.getLog(UniqueHostHook.class);

    @Autowired
    private LinkFactory linkFactory;

    private String primaryDomain = null;


    public void setPrimaryDomain(String primaryDomain) {
        this.primaryDomain = primaryDomain;
    }


    @Override
    public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("intercept() serverName="+request.getServerName());
        } // if
        boolean isOnLocalhost = request.getServerName().equals("localhost");
        if ( !(request.getServerName().equals(primaryDomain)||(isOnLocalhost))) {
            Link redirectLink = linkFactory.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
            response.setHeader("Location", "http://"+primaryDomain+redirectLink.getUrl());
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            return true;
        } // if
        return false;
    } // intercept()

} // UniqueUrlHook
