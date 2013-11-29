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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.TargetDescriptor;

/**
 * base class for spring MVC @controllers used for rendering something in the outcome.
 *
 * Just provides convenience methods.
 *
 * Now independent of any spring classes to be able ot support other frameworks and environments
 * in the future.
 *
 */
public abstract class RenderingBase implements LinkFactory {

    private static final Log log = LogFactory.getLog(RenderingBase.class);

    @Inject
    protected BeanFactory beanFactory;

    @Inject
    protected ModelAndViewFactory modelAndViewFactory;

    private LinkFactoryAggregator linkFactory;

    // @Autowired(required = false)
    // TODO: Odd feature of JSR330: no optional injections possible so there must be at least one hook
    @Inject
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();


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
     * also calls any registered hooks
     *
     * @param request
     * @param response
     * @param bean
     * @return
     * @throws Exception
     */
    protected Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = modelAndViewFactory.createModel(descriptor.bean, request, response);
        try {
            for (ControllerHook controllerHook : controllerHooks) {
                controllerHook.intercept(descriptor, model, request, response);
            } // for
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("createModel() returning null", e);
            } // if
            return null;
        } // try/catch
        return model;
    } // createModel()


    public static Link createDefaultLink(Object bean, String action, String view) {
        Link result = null;
        if (action==null) {
            String url = "/id_"+((Content)bean).getId()+(view==null ? "" : "/view_"+view);
            result = new Link();
            result.setUrl(url);
            result.setTarget("_tangram_view");
        } // if
        return result;
    } // createDefaultLink()


    @Override
    public abstract Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);

} // RenderingBase
