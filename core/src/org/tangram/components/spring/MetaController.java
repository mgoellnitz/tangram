/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.components.spring;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.controller.ActionForm;
import org.tangram.controller.ActionParameter;
import org.tangram.controller.ControllerHook;
import org.tangram.controller.LinkAction;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkScheme;
import org.tangram.logic.ClassRepository;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;

@Controller
public class MetaController extends AbstractController implements LinkHandler, BeanListener {

    private static Log log = LogFactory.getLog(MetaController.class);

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private DefaultController defaultController;

    private LinkFactory linkFactory;

    @Inject
    protected ModelAndViewFactory modelAndViewFactory;

    @Inject
    private ClassRepository classRepository;

    @Autowired(required = false)
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();

    private Map<String, LinkScheme> schemes;

    private Map<Object, Collection<String>> customViews = new HashMap<Object, Collection<String>>();


    // do autowiring here so the registration can be done automagically
    @Inject
    public void setLinkFactory(LinkFactory linkFactory) {
        this.linkFactory = linkFactory;
        this.linkFactory.registerHandler(this);
    }


    @Override
    public void reset() {
        schemes = new HashMap<String, LinkScheme>();
        // remove current custom views from default controller
        for (Object key : customViews.keySet()) {
            for (String view : customViews.get(key)) {
                defaultController.getCustomLinkViews().remove(view);
            } // for
        } // for
        if (log.isInfoEnabled()) {
            log.info("reset() custom views in default controller "+defaultController.getCustomLinkViews());
        } // if
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
                    Collection<String> schemeCustomViews = linkScheme.getCustomViews();
                    customViews.put(linkScheme, schemeCustomViews);
                    defaultController.getCustomLinkViews().addAll(schemeCustomViews);
                    if (log.isInfoEnabled()) {
                        log.info("reset() adding custom views "+schemeCustomViews);
                        log.info("reset() custom views in default controller "+defaultController.getCustomLinkViews());
                    } // if
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
        if (log.isInfoEnabled()) {
            log.info("reset() custom views in default controller "+defaultController.getCustomLinkViews());
        } // if
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
        if (log.isInfoEnabled()) {
            log.info("findMethod() trying to find "+methodName+" in "+target.getClass().getName());
        } // if
        Method method = null;
        Method[] methods = target.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                LinkAction linkAction = m.getAnnotation(LinkAction.class);
                if (log.isInfoEnabled()) {
                    log.info("findMethod() linkAction="+linkAction);
                    log.info("findMethod() method.getReturnType()="+m.getReturnType());
                } // if
                if ( !TargetDescriptor.class.equals(m.getReturnType())) {
                    linkAction = null;
                } // if
                if (log.isInfoEnabled()) {
                    log.info("findMethod() linkAction="+linkAction);
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
        Link link = null;
        if (log.isInfoEnabled()) {
            log.info("callAction() method="+method);
        } // if

        // We don't want to access parameters via GET
        if ((method!=null)&&request.getMethod().equals("POST")) {
            // TODO: Do type conversion or binding with spring or its coversion service
            PropertyConverter propertyConverter = Utils.getPropertyConverter();
            descriptor.action = null;
            List<Object> parameters = new ArrayList<Object>();
            Annotation[][] allAnnotations = method.getParameterAnnotations();
            Class<? extends Object>[] parameterTypes = method.getParameterTypes();
            int typeIndex = 0;
            // TODO: Do real binding like spring here - or allow spring stuff in groovy
            for (Annotation[] annotations : allAnnotations) {
                Class<? extends Object> parameterType = parameterTypes[typeIndex++ ];
                for (Annotation annotation : annotations) {
                    if (annotation instanceof ActionParameter) {
                        String parameterName = ((ActionParameter)annotation).name();
                        if ("--empty--".equals(parameterName)) {
                            parameterName = parameterType.getSimpleName().toLowerCase();
                        } // if
                        String valueString = request.getParameter(parameterName);
                        if (log.isInfoEnabled()) {
                            log.info("callAction() parameter "+parameterName+" should be of type "+parameterType.getName());
                        } // if
                        // Object derivedValue = value;
                        // if (Content.class.isAssignableFrom(parameterType)) {
                        // derivedValue = beanFactory.getBean(value);
                        // if (log.isInfoEnabled()) {
                        // log.info("callAction() converting parameter "+parameterName+" to "+derivedValue.getClass());
                        // } // if
                        // } // if
                        Object value = propertyConverter.getStorableObject(valueString, parameterType, request);
                        parameters.add(value);
                    } // if
                    if (annotation instanceof ActionForm) {
                        try {
                            Object form = parameterType.newInstance();
                            BeanWrapper wrapper = Utils.createWrapper(form);
                            for (PropertyDescriptor property : wrapper.getPropertyDescriptors()) {
                                String propertyName = property.getName();
                                String valueString = request.getParameter(propertyName);
                                Object value = propertyConverter.getStorableObject(valueString, property.getPropertyType(), request);
                                wrapper.setPropertyValue(propertyName, value);
                            } // for
                            parameters.add(form);
                        } catch (Exception e) {
                            log.error("callAction() cannot create and fill form "+parameterType.getName());
                        } // try/catch
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
            link = linkFactory.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
        } // if
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
                        if (log.isInfoEnabled()) {
                            log.info("handleRequestInternal() handing over to view "+descriptor.view);
                        } // if
                        return modelAndViewFactory.createModelAndView(model, descriptor.view);
                    } else {
                        if (log.isInfoEnabled()) {
                            log.info("handleRequestInternal() trying to call action "+descriptor.action);
                        } // if
                          // TODO: find a less expensive way of digging out action calls
                        Method method = findMethod(linkScheme, descriptor.action);
                        Link link = callAction(request, response, method, descriptor, linkScheme);
                        for (Object value : model.values()) {
                            // This has to be checked because of special null values in context like $null
                            // if the link is already set don't try to call other methods
                            if ((value!=null)&&(link==null)) {
                                method = findMethod(value, descriptor.action);
                                link = callAction(request, response, method, descriptor, value);
                            } // if
                        } // for
                        if (link==null) {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info("handleRequestInternal() received link "+link.getUrl());
                            } // if
                            response.sendRedirect(link.getUrl());
                        } // if
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


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        classRepository.addListener(this);
        reset();
    } // afterPropertiesSet()

} // MetaController
