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
package org.tangram.link;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.LinkAction;
import org.tangram.monitor.Statistics;
import org.tangram.view.TargetDescriptor;


/**
 * Right at the moment we cannot think of a second necessary implementation of this.
 *
 * So this is the generic not just default implementation of a link factory aggregator to be used to create
 * any link you might need.
 */
@Singleton
public class GenericLinkFactoryAggregator implements LinkFactoryAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(GenericLinkFactoryAggregator.class);

    /**
     * Dummy instance to be placed in Maps and the like.
     */
    private static final Method NULL_METHOD = GenericLinkFactoryAggregator.class.getMethods()[0];

    @Inject
    private Statistics statistics;

    private String dispatcherPath = "";

    private List<LinkFactory> handlers = new ArrayList<LinkFactory>();

    /**
     * method classname#methodname to method cache
     */
    private Map<String, Method> cache = new HashMap<String, Method>();


    public String getDispatcherPath() {
        return dispatcherPath;
    }


    /**
     * Set the path of the dispatcher servlet.
     *
     * Needed as a prefix for every URL to generate subsequently.
     *
     * @param dispatcherPath path of the servlet including leading slash
     */
    public void setDispatcherPath(String dispatcherPath) {
        this.dispatcherPath = dispatcherPath;
    }


    @Override
    public void registerFactory(LinkFactory handler) {
        handlers.add(handler);
    } // registerFactory()


    @Override
    public void unregisterFactory(LinkFactory factory) {
        handlers.remove(factory);
    } // unregisterFactory()

    String prefix = null;


    @Override
    public String getPrefix(HttpServletRequest request) {
        if (prefix==null) {
            String contextPath = request.getContextPath();
            prefix = (contextPath.length() == 1 ? "" : contextPath)+dispatcherPath;
        } // if
        return prefix;
    } // getPrefix()


    public void postProcessResult(Link result, HttpServletRequest request) {
        StringBuffer url = new StringBuffer(result.getUrl());
        int idx = url.indexOf("/");
        if (LOG.isDebugEnabled()) {
            LOG.debug("postProcessResult() "+idx+" ("+url+")");
        } // if
        if (idx>=0) {
            url.insert(idx, getPrefix(request));
        } // if
        if (LOG.isDebugEnabled()) {
            LOG.debug("postProcessResult() "+idx+" ("+url+")");
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("createLink() "+handler.getClass().getName()+" -> "+result+" ["+bean.getClass().getSimpleName()+"]");
            } // if
            if (result!=null) {
                postProcessResult(result, request);
                statistics.avg("generate url time", System.currentTimeMillis()-startTime);
                return result;
            } // if
        } // for
        throw new RuntimeException("Cannot create link for "+bean+" in action "+action+" for view "+view);
    } // createLink()


    @Override
    public Link createLink(Collection<? extends LinkFactory> handlers, HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        for (LinkFactory factory : handlers) {
            Link result = factory.createLink(request, response, bean, action, view);
            if (result!=null) {
                return result;
            } // if
        } // for
        return null;
    } // createLink()


    @Override
    public Method findMethod(Object target, String methodName) {
        Class<? extends Object> targetClass = target.getClass();
        String key = targetClass.getName()+"#"+methodName;
        if (LOG.isInfoEnabled()) {
            LOG.info("findMethod() trying to find "+key);
        } // if
        Method method = cache.get(key);
        if (method!=null) {
            return method==NULL_METHOD ? null : method;
        } // if
        for (Method m : targetClass.getMethods()) {
            if (m.getName().equals(methodName)) {
                LinkAction linkAction = m.getAnnotation(LinkAction.class);
                if (LOG.isInfoEnabled()) {
                    LOG.info("findMethod() linkAction="+linkAction+"  method.getReturnType()="+m.getReturnType());
                } // if
                if (!TargetDescriptor.class.equals(m.getReturnType())) {
                    linkAction = null;
                } // if
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findMethod() linkAction="+linkAction);
                } // if
                if (linkAction!=null) {
                    method = m;
                } // if
            } // if
        } // for
        cache.put(key, method==null ? NULL_METHOD : method);
        return method;
    } // findMethod()

} // GenericLinkBuilder
