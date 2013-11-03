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
package org.tangram.coma;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.controller.RenderingController;
import org.tangram.view.Utils;
import org.tangram.view.link.Link;

@Controller
public class ComaController extends RenderingController {

    private static final Log log = LogFactory.getLog(ComaController.class);

    @Autowired(required = false)
    private ComaBeanPopulator populator;


    @RequestMapping(value = "/content/{id}")
    public ModelAndView render(@PathVariable("id") String id,
            @RequestParam(value = "view", required = false) String view, HttpServletRequest request,
            HttpServletResponse response) {
        if (log.isInfoEnabled()) {
            log.info("render() id="+id);
            log.info("render() view="+view);
        } // if
        ComaContent content = (ComaContent)beanFactory.getBean(id);
        if (log.isInfoEnabled()) {
            log.info("render() content="+content);
        } // if
        if (content==null) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } catch (IOException ioe) {
                return Utils.getModelAndViewFactory().createModelAndView(ioe, null, request, response);
            } // try/catch
        } // if
        if (populator!=null) {
            populator.populate(content);
        } // if
        return Utils.getModelAndViewFactory().createModelAndView(content, view, request, response);
    } // render()


    @RequestMapping(value = "/contentblob/{id}/{property}/{propertyid}")
    public ModelAndView renderBlob(@PathVariable("id") String id, @PathVariable("property") String property,
            @PathVariable("propertyid") String propertyId, @RequestParam(value = "view", required = false) String view,
            HttpServletRequest request, HttpServletResponse response) {
        if (log.isInfoEnabled()) {
            log.info("render() id="+id);
            log.info("render() property="+id);
            log.info("render() view="+view);
        } // if
        ComaContent content = (ComaContent)beanFactory.getBean(id);
        if (log.isInfoEnabled()) {
            log.info("render() content="+content);
        } // if
        if (content==null) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } catch (IOException ioe) {
                return Utils.getModelAndViewFactory().createModelAndView(ioe, null, request, response);
            } // try/catch
        } // if
        if (populator!=null) {
            populator.populate(content);
        } // if

        Object propertyValue = content.get(property);

        return Utils.getModelAndViewFactory().createModelAndView(propertyValue, view, request, response);
    } // renderBlob()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        if (action==null) {
            Link result = null;
            if (bean instanceof ComaContent) {
                result = new Link();
                ComaContent cc = (ComaContent)bean;
                String url = "/content/"+cc.getId()+(view==null ? "" : "?view="+view);
                result.setUrl(url);
            } // if
            if (bean instanceof ComaBlob) {
                result = new Link();
                ComaBlob cb = (ComaBlob)bean;

                String url = "/contentblob/"+cb.getContentId()+"/"+cb.getPropertyName()+"/123"
                        +(view==null ? "" : "?view="+view);
                result.setUrl(url);
            } // if
            return result;
        } // if
        return null;
    } // createLink()

} // ComaController
