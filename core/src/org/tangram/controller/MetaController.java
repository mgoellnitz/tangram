/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.tangram.content.Content;
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
    protected Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
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


    private Method findMethod(Object target, String methodName) {
        Method method = null;
        Method[] methods = target.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                // TODO: Did this ever work?
                LinkAction linkAction = method.getAnnotation(LinkAction.class);
                if (log.isInfoEnabled()) {
                    log.info("callAction() linkAction="+linkAction);
                    log.info("callAction() method.getReturnType()="+method.getReturnType());
                } // if
                if ( !TargetDescriptor.class.equals(method.getReturnType())) {
                    linkAction = null;
                } // if
                if (log.isInfoEnabled()) {
                    log.info("callAction() linkAction="+linkAction);
                } // if
                if (linkAction!=null) {
                    method = m;
                } // if
            } // if
        } // for
        return method;
    } // findMethod()


    private Link callAction(HttpServletRequest request, HttpServletResponse response, Method method, TargetDescriptor descriptor,
            Object target) {
        if (log.isInfoEnabled()) {
            log.info("callAction() method="+method);
        } // if

        descriptor.action = null;
        List<Object> parameters = new ArrayList<Object>();
        // We don't want to access parameters via GET
        if ((method!=null)&&request.getMethod().equals("POST")) {
            Annotation[][] allAnnotations = method.getParameterAnnotations();
            Class<? extends Object>[] parameterTypes = method.getParameterTypes();
            int typeIndex = 0;
            for (Annotation[] annotations : allAnnotations) {
                Class<? extends Object> parameterType = parameterTypes[typeIndex++ ];
                for (Annotation annotation : annotations) {
                    if (annotation instanceof ActionParameter) {
                        String parameterName = ((ActionParameter)annotation).name();
                        String value = request.getParameter(parameterName);
                        if (log.isInfoEnabled()) {
                            log.info("callAction() parameter "+parameterName+" should be of type "+parameterType.getName());
                        } // if
                        Object derivedValue = value;
                        if (Content.class.isAssignableFrom(parameterType)) {
                            derivedValue = beanFactory.getBean(value);
                            if (log.isInfoEnabled()) {
                                log.info("callAction() converting parameter "+parameterName+" to "+derivedValue.getClass());
                            } // if
                        } // if
                          // TODO: more type conversions
                        parameters.add(derivedValue);
                    } // if
                } // for
            } // for
            try {
                if (log.isInfoEnabled()) {
                    log.info("callAction() calling method");
                } // if
                descriptor = (TargetDescriptor)method.invoke(target, parameters.toArray());
                if (log.isInfoEnabled()) {
                    log.info("callAction() result is "+descriptor);
                } // if
            } catch (IllegalArgumentException e) {
                log.error("callAction()", e);
            } catch (IllegalAccessException e) {
                log.error("callAction()", e);
            } catch (InvocationTargetException e) {
                log.error("callAction()", e);
            } // try/catch
        } // if
        Link link = linkFactory.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
        if (log.isInfoEnabled()) {
            log.info("callAction() link="+link);
        } // if
        return link;
    } // callAction()


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
                    if (descriptor.action==null) {
                        return modelAndViewFactory.createModelAndView(model, descriptor.view);
                    } else {
                        if (log.isInfoEnabled()) {
                            log.info("handleRequestInternal() trying call action "+descriptor.action);
                        } // if
                          // TODO: find a less expensive way of digging out action calls
                        Method method = findMethod(linkScheme, descriptor.action);
                        Link link = callAction(request, response, method, descriptor, linkScheme);
                        for (Object value : model.values()) {
                            method = findMethod(value, descriptor.action);
                            link = callAction(request, response, method, descriptor, value);
                        } // for
                        if (log.isInfoEnabled()) {
                            log.info("handleRequestInternal() received link "+link.getUrl());
                        } // if
                        response.sendRedirect(link.getUrl());
                        return result;
                    } // if
                } // if
            } catch (Exception ex) {
                return modelAndViewFactory.createModelAndView(ex, request, response);
            } // try/catch
        } // for
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return result;
    } // handleRequestInternal()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
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
