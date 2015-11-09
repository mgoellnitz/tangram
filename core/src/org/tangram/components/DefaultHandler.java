/**
 *
 * Copyright 2015 Martin Goellnitz
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

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.content.Content;
import org.tangram.controller.AbstractRenderingBase;
import org.tangram.link.InternalLinkFactory;
import org.tangram.link.Link;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;


/**
 * Handler implementation to support the minimal default URL scheme.
 *
 * The basic format is
 *
 * /&lt;prefix&gt;/id_&lt;id&gt;(/view_&lt;view&gt;)
 *
 * URLs of this format are not generated here.
 */
@Named("defaultHandler")
@Singleton
@LinkHandler
public class DefaultHandler extends AbstractRenderingBase implements InternalLinkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHandler.class);

    @Inject
    private LinkHandlerRegistry registry;


    @LinkAction("/id_([A-Z][a-zA-Z]+:[0-9]+)/view_([a-zA-Z0-9]+)")
    public TargetDescriptor render(@LinkPart(1) String id, @LinkPart(2) String view, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Utils.setPrimaryBrowserLanguageForJstl(request);
        LOG.debug("render() id={} view={}", id, view);
        Content content = beanFactory.getBean(id);
        LOG.debug("render() content={}", content);
        if (content==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
            return null;
        } // if
        return new TargetDescriptor(content, view, null);
    } // render()


    @LinkAction("/id_([A-Z][a-zA-Z]+:[0-9]+)")
    public TargetDescriptor render(@LinkPart(1) String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return render(id, null, request, response);
    } // render()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        return AbstractRenderingBase.createDefaultLink(bean, action, view);
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // DefaultHandler
