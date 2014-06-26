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
package org.tangram.coma;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.controller.RenderingBase;
import org.tangram.link.Link;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.view.TargetDescriptor;


/**
 * LinkHandler to handle some of the default URL schemes of the CoreMedia Content Application Engine.
 *
 * This code still is more demo than the default link handler instance to use.
 */
@Named
@LinkHandler
public class ComaHandler extends RenderingBase {

    private static final Logger LOG = LoggerFactory.getLogger(ComaHandler.class);

    // @Autowired(required = false)
    @Inject
    private ComaBeanPopulator populator;

    @Inject
    private LinkHandlerRegistry linkHandlerRegistry;


    @LinkAction("/content/(.*)")
    public TargetDescriptor render(@LinkPart(1) String id, @ActionParameter(value = "view") String view, HttpServletRequest request,
                                   HttpServletResponse response) {
        if (LOG.isInfoEnabled()) {
            LOG.info("render() id="+id);
            LOG.info("render() view="+view);
        } // if
        ComaContent content = (ComaContent) beanFactory.getBean(id);
        if (LOG.isInfoEnabled()) {
            LOG.info("render() content="+content);
        } // if
        if (content==null) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } catch (IOException ioe) {
                return new TargetDescriptor(ioe, null, null);
            } // try/catch
        } // if
        if (populator!=null) {
            populator.populate(content);
        } // if
        return new TargetDescriptor(content, view, null);
    } // render()


    @LinkAction("/contentblob/(.*)/(.*)/(.*)")
    public TargetDescriptor renderBlob(@LinkPart(1) String id, @LinkPart(2) String property, @LinkPart(3) String propertyId,
                                       @ActionParameter("view") String view, HttpServletRequest request, HttpServletResponse response) {
        if (LOG.isInfoEnabled()) {
            LOG.info("render() id="+id);
            LOG.info("render() property="+id);
            LOG.info("render() view="+view);
        } // if
        ComaContent content = (ComaContent) beanFactory.getBean(id);
        if (LOG.isInfoEnabled()) {
            LOG.info("render() content="+content);
        } // if
        if (content==null) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } catch (IOException ioe) {
                return new TargetDescriptor(ioe, null, null);
            } // try/catch
        } // if
        if (populator!=null) {
            populator.populate(content);
        } // if

        Object propertyValue = content.get(property);

        return new TargetDescriptor(propertyValue, view, null);
    } // renderBlob()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        if (action==null) {
            Link result = null;
            if (bean instanceof ComaContent) {
                result = new Link();
                ComaContent cc = (ComaContent) bean;
                String url = "/content/"+cc.getId()+(view==null ? "" : "?view="+view);
                result.setUrl(url);
            } // if
            if (bean instanceof ComaBlob) {
                result = new Link();
                ComaBlob cb = (ComaBlob) bean;

                String url = "/contentblob/"+cb.getContentId()+"/"+cb.getPropertyName()+"/123"
                        +(view==null ? "" : "?view="+view);
                result.setUrl(url);
            } // if
            return result;
        } // if
        return null;
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() {
        linkHandlerRegistry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // ComaHandler
