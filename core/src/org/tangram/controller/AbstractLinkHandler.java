/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.MetaLinkHandler;
import org.tangram.content.BeanFactory;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.ViewContextFactory;


/**
 * base class for spring MVC @controllers used for rendering something in the outcome.
 *
 * Just provides convenience methods.
 *
 * Now independent of any spring classes to be able to support other frameworks and environments
 * in the future.
 *
 */
public abstract class AbstractLinkHandler implements LinkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLinkHandler.class);

    @Inject
    protected BeanFactory beanFactory;

    @Inject
    protected ViewContextFactory viewContextFactory;

    private LinkFactoryAggregator linkFactory;

    @Inject
    private MetaLinkHandler metaLinkHandler;


    public BeanFactory getBeanFactory() {
        return beanFactory;
    }


    public LinkFactoryAggregator getLinkFactory() {
        return linkFactory;
    }


    // do autowiring here so the registration can be done automagically
    @Inject
    public void setLinkFactory(LinkFactoryAggregator linkFactory) {
        this.linkFactory = linkFactory;
        this.linkFactory.registerFactory(this);
    }


    /**
     * Creates model from a common set of parameters.
     *
     * Uses the meta link handler and thus also calls any registered hooks.
     *
     * @param descriptor description of the model to wrap
     * @param request request context to create model in
     * @param response response context to create model in
     * @return map resembling the model
     * @throws Exception no exceptions are expected but any can occur
     */
    protected Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return metaLinkHandler.createModel(descriptor, request, response);
    } // createModel()


    @Override
    public abstract Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);



    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet() {}", getClass().getSimpleName());
        this.metaLinkHandler.registerLinkHandler(this);
    } // afterPropertiesSet()

} // AbstractRenderingBase
