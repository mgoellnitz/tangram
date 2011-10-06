package org.tangram.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.logic.ClassRepository;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;
import org.tangram.view.link.LinkHandler;
import org.tangram.view.link.LinkScheme;

@Controller
public class MetaController extends AbstractController implements InitializingBean, LinkHandler, BeanListener {

    private static Log log = LogFactory.getLog(MetaController.class);

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private DefaultController defaultController;

    private LinkFactory linkFactory;

    @Autowired
    protected ModelAndViewFactory modelAndViewFactory;

    @Autowired
    private ClassRepository classRepository;

    @Autowired(required = false)
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();

    private Map<String, LinkScheme> schemes;


    // do autowiring here so the registration can be done automagically
    @Autowired
    public void setLinkFactory(LinkFactory linkFactory) {
        this.linkFactory = linkFactory;
        this.linkFactory.registerHandler(this);
    }


    /**
     * also calls any registered hooks
     * 
     * copied from rendering controller
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
                if (log.isDebugEnabled()) {
                    log.debug("createModel() "+controllerHook.getClass().getName());
                } // if
                boolean result = controllerHook.intercept(descriptor, model, request, response);
                if (result) {
                    return null;
                } // if
            } // for
        } catch (Exception e) {
            return modelAndViewFactory.createModel(e, request, response);
        } // try/catch
        return model;
    } // createModel()


    @Override
	public void reset() {
        schemes = new HashMap<String, LinkScheme>();
        for (Map.Entry<String, Class<LinkScheme>> entry : classRepository.get(LinkScheme.class).entrySet()) {
            try {
                String annotation = entry.getKey();
                Class<LinkScheme> clazz = entry.getValue();
                if (LinkScheme.class.isAssignableFrom(clazz)) {
                    if (log.isInfoEnabled()) {
                        log.info("reset() "+clazz.getName()+" is a LinkScheme");
                    } // if
                    LinkScheme linkScheme = clazz.newInstance();
                    if (log.isInfoEnabled()) {
                        log.info("reset() "+clazz.getName()+" instanciated");
                    } // if
                    linkScheme.setBeanFactory(beanFactory);
                    linkScheme.setDefaultController(defaultController);
                    schemes.put(annotation, linkScheme);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("reset() "+clazz.getName()+" is not a LinkScheme");
                    } // if
                } // if
            } catch (Throwable e) {
                // who cares
                if (log.isErrorEnabled()) {
                    log.error("reset()", e);
                } // if
            } // try/catch
        } // for
    } // fillSchemes()


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String url = request.getRequestURI().substring(linkFactory.getPrefix(request).length());
        if (log.isDebugEnabled()) {
            log.debug("handleRequestInternal() "+url);
        } // if
        ModelAndView result = null;
        for (String className : schemes.keySet()) {
            try {
                LinkScheme linkScheme = schemes.get(className);
                TargetDescriptor descriptor = linkScheme.parseLink(url, response);
                if (descriptor!=null) {
                    if (log.isInfoEnabled()) {
                        log.info("handleRequestInternal() "+linkScheme.getClass().getName()+" hit for "+url);
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("handleRequestInternal() found bean "+descriptor.bean);
                    } // if

                    Map<String, Object> model = createModel(descriptor, request, response);
                    return modelAndViewFactory.createModelAndView(model, descriptor.view);
                } // if
            } catch (Exception ex) {
                return modelAndViewFactory.createModelAndView(ex, request, response);
            } // try/catch
        } // for
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return result;
    } // handleRequestInternal()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action,
            String view) {
        Link result = null;
        for (LinkScheme linkScheme : schemes.values()) {
            result = linkScheme.createLink(request, response, bean, action, view);
            if (result!=null) {
                break;
            } // if
        } // for
        return result;
    } // createLink()


    @Override
	public void afterPropertiesSet() throws Exception {
        classRepository.addListener(this);
        reset();
    } // afterPropertiesSet()

} // MetaController
