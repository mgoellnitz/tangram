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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.link;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.monitor.Statistics;

/**
 *
 * right at the moment we cannot think of a second necessary implementation of this.
 *
 * So this is the generic not just default implementation of a link factory.
 *
 */
@Singleton
public class GenericLinkFactoryAggregator implements LinkFactoryAggregator {

    private static final Log log = LogFactory.getLog(GenericLinkFactoryAggregator.class);

    @Inject
    private Statistics statistics;

    private String dispatcherPath = "";

    private List<LinkFactory> handlers = new ArrayList<LinkFactory>();


    public String getDispatcherPath() {
        return dispatcherPath;
    }


    public void setDispatcherPath(String dispatcherPath) {
        this.dispatcherPath = dispatcherPath;
    }


    @Override
    public void registerFactory(LinkFactory handler) {
        handlers.add(handler);
    } // registerFactory()


    @Override
    public void unregisterHandler(LinkFactory handler) {
        handlers.remove(handler);
    } // unregisterHandler()

    String prefix = null;


    @Override
    public String getPrefix(HttpServletRequest request) {
        if (prefix==null) {
            String contextPath = request.getContextPath();
            if (contextPath.length()==1) {
                contextPath = "";
            } // if
            prefix = contextPath+dispatcherPath;
        } // if
        return prefix;
    } // getPrefix()


    public void postProcessResult(Link result, HttpServletRequest request) {
        String urlString = result.getUrl();
        StringBuffer url = new StringBuffer(urlString);
        int idx = urlString.indexOf('/');
        if (log.isDebugEnabled()) {
            log.debug("postProcessResult() "+idx+" ("+url+")");
        } // if
        if (idx>=0) {
            url.insert(idx, getPrefix(request));
        } // if
        if (log.isDebugEnabled()) {
            log.debug("postProcessResult() "+idx+" ("+url+")");
        } // if
        result.setUrl(url.toString());
    } // postProcessResult()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        if (bean==null) {
            throw new RuntimeException("No bean issued for link generation in action "+action+" for view "+view);
        } // if
        for (LinkFactory handler : handlers) {
            long startTime = System.currentTimeMillis();
            Link result = handler.createLink(request, response, bean, action, view);
            if (log.isDebugEnabled()) {
                log.debug("createLink() "+handler.getClass().getName()+" -> "+result+" ["+bean.getClass().getSimpleName()+"]");
            } // if
            if (result!=null) {
                postProcessResult(result, request);
                statistics.avg("generate url time", System.currentTimeMillis()-startTime);
                return result;
            } // if
        } // for
        throw new RuntimeException("Cannot create link for "+bean+" in action "+action+" for view "+view);
    } // createLink()

} // GenericLinkBuilder
