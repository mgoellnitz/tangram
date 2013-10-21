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
package org.tangram.components;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tangram.controller.ControllerHook;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

@Component
public class UniqueUrlHook implements ControllerHook {

    private static Log log = LogFactory.getLog(UniqueUrlHook.class);

    @Autowired
    private LinkFactory linkFactory;


    @Override
    public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Link link = null;
        try {
            link = linkFactory.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("intercept() expensive things happen "+((descriptor.bean==null) ? "" : descriptor.bean.getClass().getName()));
            } // if
        } // try/catch
        if (link!=null) {
            // If you run into trouble with encodings, this might be a place to search
            // String decodedUrl = URLDecoder.decode(link.getUrl(), "UTF-8");
            // String requestURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
            String decodedUrl = link.getUrl();
            String requestURI = request.getRequestURI();
            if ( !decodedUrl.equals(requestURI)) {
                if (log.isInfoEnabled()) {
                    log.info("render() sending redirect for "+requestURI+" to "+decodedUrl);
                } // if
                response.setHeader("Location", link.getUrl());
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                return true;
            } // if
        } // if
        return false;
    } // intercept()

} // UniqueUrlHook
