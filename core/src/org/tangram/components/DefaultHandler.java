/**
 *
 * Copyright 2015-2016 Martin Goellnitz
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
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.content.Content;
import org.tangram.content.blob.MimedBlob;
import org.tangram.controller.AbstractLinkHandler;
import org.tangram.link.InternalLinkFactory;
import org.tangram.link.Link;
import org.tangram.link.TargetDescriptor;
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
@Named
@Singleton
@LinkHandler
public class DefaultHandler extends AbstractLinkHandler implements InternalLinkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHandler.class);

    private int maxInlinedBlobSize = 1024;


    /**
     * Set maximum size of blobs to be inlined into their respective URL as data scheme, base64 encoded mimed blobs.
     *
     * @param maxInlinedBlobSize maximum size in bytes
     */
    public void setMaxInlinedBlobSize(int maxInlinedBlobSize) {
        this.maxInlinedBlobSize = maxInlinedBlobSize;
        LOG.info("setMaxInlinedBlobSize() max size is {}", this.maxInlinedBlobSize);
    } // setMaxInlinedBlobSize()


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
        Link result = null;
        if ((bean instanceof Content)&&(action==null)) {
            if ((view==null)&&(bean instanceof MimedBlob)) {
                MimedBlob blob = (MimedBlob) bean;
                LOG.debug("createLink() mimed blob link for {}", blob);
                StringBuilder url = new StringBuilder(maxInlinedBlobSize);
                byte[] bytes = blob.getBytes();
                if (bytes.length<maxInlinedBlobSize) {
                    url.append("data:");
                    url.append(blob.getMimeType());
                    url.append(";base64,");
                    // TODO: Java8 has this internal in java.util
                    // url.append(Base64.getEncoder().encodeToString(bytes));
                    url.append(Base64.encodeBase64String(bytes));
                } else {
                    url.append("/id_");
                    url.append(((Content) bean).getId());
                    if (view!=null) {
                        url.append("/view_");
                        url.append(view);
                    } // if
                } // if
                result = new Link(url.toString());
            } else {
                String url = "/id_"+((Content) bean).getId()+(view==null ? "" : "/view_"+view);
                result = new Link(url);
                result.setTarget("_tangram_view");
            } // if
        } // if
        return result;
    } // createLink()

} // DefaultHandler
