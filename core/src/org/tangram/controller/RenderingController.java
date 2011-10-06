package org.tangram.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;
import org.tangram.view.link.LinkHandler;

/**
 * base class for controllers used for rendering something in the outcome. Just provides convenience methods.
 */
public abstract class RenderingController implements LinkHandler {

    private static final Log log = LogFactory.getLog(RenderingController.class);

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected ModelAndViewFactory modelAndViewFactory;

    private LinkFactory linkFactory;

    @Autowired(required = false)
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();


    public BeanFactory getBeanFactory() {
        return beanFactory;
    }


    public LinkFactory getLinkFactory() {
        return linkFactory;
    }


    // do autowiring here so the registration can be done automagically
    @Autowired
    public void setLinkFactory(LinkFactory linkFactory) {
        this.linkFactory = linkFactory;
        this.linkFactory.registerHandler(this);
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
    protected Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
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
	public abstract Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean,
            String action, String view);

} // RenderingController
