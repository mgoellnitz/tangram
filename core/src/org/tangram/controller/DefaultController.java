package org.tangram.controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.content.Content;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;

@Controller
public class DefaultController extends RenderingController {

    private static final Log log = LogFactory.getLog(DefaultController.class);

    @Autowired(required = false)
    protected HashSet<String> customLinkViews = new HashSet<String>();


    public Set<String> getCustomLinkViews() {
        return customLinkViews;
    } // getCustomLinkViews


    @RequestMapping(value = "/id_{id}/view_{view}")
    public ModelAndView render(@PathVariable("id") String id, @PathVariable("view") String view,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            if (customLinkViews.contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "custom view required.");
                return null;
            } // if
            if (log.isDebugEnabled()) {
                log.debug("render() id="+id);
                log.debug("render() view="+view);
            } // if
            Content content = beanFactory.getBean(id);
            if (log.isDebugEnabled()) {
                log.debug("render() content="+content);
            } // if
            if (content==null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } // if

            Map<String, Object> model = createModel(new TargetDescriptor(content, view, null), request, response);
            return modelAndViewFactory.createModelAndView(model, view);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // render()


    @RequestMapping(value = "/id_{id}")
    public ModelAndView render(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        return render(id, null, request, response);
    } // render()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        if (bean instanceof Content) {
            if ( !customLinkViews.contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                return RenderingController.createDefaultLink(bean, action, view);
            } // if
        } // if
        return null;
    } // createLink()

} // GenericController
