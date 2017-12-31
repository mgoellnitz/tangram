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
package org.tangram.link;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.LinkAction;
import org.tangram.monitor.Statistics;


/**
 * Right at the moment we cannot think of a second necessary implementation of this.
 *
 * So this is the generic not just default implementation of a link factory aggregator to be used to create
 * any link you might need.
 */
@Named("linkFactoryAggregator")
@Singleton
public class GenericLinkFactoryAggregator implements LinkFactoryAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(GenericLinkFactoryAggregator.class);

    /**
     * Dummy instance to be placed in Maps and the like.
     */
    private static final Method NULL_METHOD = GenericLinkFactoryAggregator.class.getMethods()[0];

    @Inject
    private Statistics statistics;

    private String dispatcherPath = "/s";

    private final List<LinkFactory> factories = new ArrayList<>();

    /**
     * method classname#methodname to method cache
     */
    private final Map<String, Method> cache = new HashMap<>();

    private String prefix = null;


    @Override
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
    public String getPrefix(HttpServletRequest request) {
        if (prefix==null) {
            String contextPath = request.getContextPath();
            prefix = (contextPath.length()==1 ? "" : contextPath)+dispatcherPath;
        } // if
        return prefix;
    } // getPrefix()


    @Override
    public void registerFactory(LinkFactory factory) {
        factories.add(factory);
        Collections.sort(factories, (LinkFactory o1, LinkFactory o2) -> (o2 instanceof InternalLinkFactory) ? -1 : 1);
    } // registerFactory()


    @Override
    public void unregisterFactory(LinkFactory factory) {
        factories.remove(factory);
    } // unregisterFactory()


    public void postProcessResult(Link result, HttpServletRequest request) {
        StringBuffer url = new StringBuffer(result.getUrl());
        int idx = url.indexOf("/");
        LOG.debug("postProcessResult() {} ({})", idx, url);
        if (idx>=0) {
            url.insert(idx, getPrefix(request));
        } // if
        LOG.debug("postProcessResult() {} ({})", idx, url);
        result.setUrl(url.toString());
    } // postProcessResult()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        if (bean==null) {
            throw new RuntimeException("No bean issued for link generation in action "+action+" for view "+view);
        } // if
        LOG.debug("createLink() {}", factories);
        for (LinkFactory factory : factories) {
            long startTime = System.currentTimeMillis();
            Link result = factory.createLink(request, response, bean, action, view);
            LOG.debug("createLink() {} -> {} [{}]", factory.getClass().getName(), result, bean.getClass().getSimpleName());
            if (result!=null) {
                postProcessResult(result, request);
                statistics.avg("generate url time", System.currentTimeMillis()-startTime);
                return result;
            } // if
        } // for
        throw new RuntimeException("Cannot create link for "+bean+" in action "+action+" for view "+view);
    } // createLink()


    @Override
    public Method findMethod(Object target, String methodName) {
        Class<? extends Object> targetClass = target.getClass();
        String key = targetClass.getName()+"#"+methodName;
        LOG.info("findMethod() trying to find {}", key);
        Method method = cache.get(key);
        if (method!=null) {
            return method==NULL_METHOD ? null : method;
        } // if
        for (Method m : targetClass.getMethods()) {
            if (m.getName().equals(methodName)) {
                LinkAction linkAction = m.getAnnotation(LinkAction.class);
                LOG.info("findMethod() linkAction={}  method.getReturnType()={}", linkAction, m.getReturnType());
                if (!TargetDescriptor.class.equals(m.getReturnType())) {
                    linkAction = null;
                } // if
                LOG.debug("findMethod() linkAction={}", linkAction);
                if (linkAction!=null) {
                    method = m;
                } // if
            } // if
        } // for
        cache.put(key, method==null ? NULL_METHOD : method);
        return method;
    } // findMethod()

} // GenericLinkFactoryAggregator
