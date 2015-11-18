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
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Inject
    protected BeanFactory beanFactory;

    @Inject
    protected ViewContextFactory viewContextFactory;

    private LinkFactoryAggregator linkFactory;

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


    // do autowiring here so the registration can be done automagically
    @Inject
    public void setMetaLinkHandler(MetaLinkHandler metaLinkHandler) {
        this.metaLinkHandler = metaLinkHandler;
        this.metaLinkHandler.registerLinkHandler(this);
    }


    /**
     * Creates model from a common set of parameters.
     *
     * Uses the meta link handler and thus also calls any registered hooks.
     *
     * @param descriptor description of the model to wrap
     * @param request
     * @param response
     * @return map resembling the model
     * @throws Exception
     */
    protected Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return metaLinkHandler.createModel(descriptor, request, response);
    } // createModel()


    @Override
    public abstract Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);

} // AbstractRenderingBase
