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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.components.spring;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.tangram.annotate.ActionForm;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkPart;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanFactoryAware;
import org.tangram.content.BeanListener;
import org.tangram.controller.ControllerHook;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.logic.ClassRepository;
import org.tangram.view.ModelAndViewFactory;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;


@Controller
public class MetaController extends AbstractController implements LinkHandlerRegistry, LinkFactory, BeanListener {

    private static Log log = LogFactory.getLog(MetaController.class);

    /**
     * Dummy instance to be placed in Maps and the like.
     */
    private static Method NULL_METHOD = MetaController.class.getMethods()[0];

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private DefaultController defaultController;

    private LinkFactoryAggregator linkFactory;

    @Inject
    protected ModelAndViewFactory modelAndViewFactory;

    @Inject
    private ClassRepository classRepository;

    @Autowired(required = false)
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();

    private Map<String, Method> cache = new HashMap<String, Method>();

    private Map<String, LinkHandler> staticLinkHandlers = new HashMap<String, LinkHandler>();

    private Map<String, LinkHandler> handlers;

    private Map<Pattern, Method> staticMethods = new HashMap<Pattern, Method>();

    private Map<Pattern, Method> methods;

    private Map<Pattern, Object> staticAtHandlers = new HashMap<Pattern, Object>();

    private Map<Pattern, Object> atHandlers;

    private Map<Object, Collection<String>> customViews = new HashMap<Object, Collection<String>>();


    // do autowiring here so the registration can be done automagically
    @Inject
    public void setLinkFactory(LinkFactoryAggregator linkFactory) {
        this.linkFactory = linkFactory;
        this.linkFactory.registerHandler(this);
    }


    @Override
    public void registerLinkHandler(Object handler) {
        if (handler instanceof LinkHandler) {
            LinkHandler linkHandler = (LinkHandler) handler;
            staticLinkHandlers.put(handler.getClass().getName(), linkHandler);
            handlers.put(handler.getClass().getName(), linkHandler);
        } else {
            Class<? extends Object> handlerClass = handler.getClass();
            if (handlerClass.getAnnotation(org.tangram.annotate.LinkHandler.class)!=null) {
                for (Method m : handlerClass.getMethods()) {
                    LinkAction linkAction = m.getAnnotation(LinkAction.class);
                    if (log.isDebugEnabled()) {
                        log.debug("registerLinkHandler() linkAction="+linkAction);
                        log.debug("registerLinkHandler() method.getReturnType()="+m.getReturnType());
                    } // if
                    if (!TargetDescriptor.class.equals(m.getReturnType())) {
                        linkAction = null;
                    } // if
                    if (linkAction!=null) {
                        if (StringUtils.isNotBlank(linkAction.value())) {
                            Pattern pathPattern = Pattern.compile(linkAction.value().replace("/", "\\/"));
                            if (log.isInfoEnabled()) {
                                log.info("registerLinkHandler() registering "+pathPattern+" for "+m.getName()+"@"+handler);
                            } // if
                            staticMethods.put(pathPattern, m);
                            methods.put(pathPattern, m);
                            staticAtHandlers.put(pathPattern, handler);
                            atHandlers.put(pathPattern, handler);
                        } // if
                    } // if
                } // for
            } // if
        } // if
    } // registerLinkHandler()


    @Override
    public void reset() {
        methods = new HashMap<Pattern, Method>();
        methods.putAll(staticMethods);
        atHandlers = new HashMap<Pattern, Object>();
        atHandlers.putAll(staticAtHandlers);
        handlers = new HashMap<String, LinkHandler>();
        handlers.putAll(staticLinkHandlers);
        // remove current custom views from default controller
        for (Object key : customViews.keySet()) {
            for (String view : customViews.get(key)) {
                defaultController.getCustomLinkViews().remove(view);
            } // for
        } // for
        if (log.isInfoEnabled()) {
            log.info("reset() custom views in default controller "+defaultController.getCustomLinkViews());
        } // if
        for (Map.Entry<String, Class<LinkHandler>> entry : classRepository.get(LinkHandler.class).entrySet()) {
            try {
                String annotation = entry.getKey();
                Class<LinkHandler> clazz = entry.getValue();
                if (LinkHandler.class.isAssignableFrom(clazz)) {
                    if (log.isInfoEnabled()) {
                        log.info("reset() "+clazz.getName()+" is a LinkScheme");
                    } // if
                    LinkHandler linkHandler = clazz.newInstance();
                    if (log.isInfoEnabled()) {
                        log.info("reset() "+clazz.getName()+" instanciated");
                    } // if
                    if (linkHandler instanceof BeanFactoryAware) {
                        ((BeanFactoryAware) linkHandler).setBeanFactory(beanFactory);
                    } // if
                    Collection<String> schemeCustomViews = linkHandler.getCustomViews();
                    customViews.put(linkHandler, schemeCustomViews);
                    defaultController.getCustomLinkViews().addAll(schemeCustomViews);
                    if (log.isInfoEnabled()) {
                        log.info("reset() adding custom views "+schemeCustomViews);
                        log.debug("reset() custom views in default controller "+defaultController.getCustomLinkViews());
                    } // if
                    handlers.put(annotation, linkHandler);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("reset() "+clazz.getName()+" is not a LinkScheme");
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
    } // reset()


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
        Class<? extends Object> targetClass = target.getClass();
        String key = targetClass.getName()+"#"+methodName;
        Method method = cache.get(key);
        if (method!=null) {
            return method==NULL_METHOD ? null : method;
        } // if
        /*
         if (targetClass.getAnnotation(org.tangram.annotate.LinkHandler.class)==null) {
         return null;
         } // if
         */
        Method[] methods = target.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                LinkAction linkAction = m.getAnnotation(LinkAction.class);
                if (log.isInfoEnabled()) {
                    log.info("findMethod() linkAction="+linkAction);
                    log.info("findMethod() method.getReturnType()="+m.getReturnType());
                } // if
                if (!TargetDescriptor.class.equals(m.getReturnType())) {
                    linkAction = null;
                } // if
                if (log.isDebugEnabled()) {
                    log.debug("findMethod() linkAction="+linkAction);
                } // if
                if (linkAction!=null) {
                    method = m;
                } // if
            } // if
        } // for
        cache.put(key, method==null ? NULL_METHOD : method);
        return method;
    } // findMethod()


    private TargetDescriptor callAction(HttpServletRequest request, HttpServletResponse response, Matcher matcher, Method method, TargetDescriptor descriptor,
                                        Object target) throws Throwable, IllegalAccessException {
        TargetDescriptor result = null;
        if (log.isDebugEnabled()) {
            log.debug("callAction() method="+method);
        } // if

        if (method!=null) {
            // TODO: Do type conversion or binding with spring or its coversion service
            PropertyConverter propertyConverter = Utils.getPropertyConverter();
            descriptor.action = null;
            List<Object> parameters = new ArrayList<Object>();
            Annotation[][] allAnnotations = method.getParameterAnnotations();
            Class<? extends Object>[] parameterTypes = method.getParameterTypes();
            for (int typeIndex = 0; typeIndex<parameterTypes.length; typeIndex++) {
                Annotation[] annotations = allAnnotations[typeIndex];
                Class<? extends Object> type = parameterTypes[typeIndex];
                if (type.equals(HttpServletRequest.class)) {
                    parameters.add(request);
                } // if
                if (type.equals(HttpServletResponse.class)) {
                    parameters.add(response);
                } // if
                for (Annotation annotation : annotations) {
                    if (annotation instanceof LinkPart) {
                        int partNumber = ((LinkPart) annotation).number();
                        String valueString = matcher.group(partNumber);
                        if (log.isDebugEnabled()) {
                            log.debug("callAction() parameter #"+typeIndex+"='"+valueString+"' should be of type "+type.getName());
                        } // if
                        Object value = propertyConverter.getStorableObject(valueString, type, request);
                        parameters.add(value);
                    } // if
                    if (annotation instanceof ActionParameter) {
                        String parameterName = ((ActionParameter) annotation).name();
                        if ("--empty--".equals(parameterName)) {
                            parameterName = type.getSimpleName().toLowerCase();
                        } // if
                        String valueString = request.getParameter(parameterName);
                        if (log.isDebugEnabled()) {
                            log.debug("callAction() parameter "+parameterName+" should be of type "+type.getName());
                        } // if
                        Object value = propertyConverter.getStorableObject(valueString, type, request);
                        parameters.add(value);
                    } // if
                    if (annotation instanceof ActionForm) {
                        try {
                            Object form = type.newInstance();
                            BeanWrapper wrapper = Utils.createWrapper(form);
                            for (PropertyDescriptor property : wrapper.getPropertyDescriptors()) {
                                String propertyName = property.getName();
                                String valueString = request.getParameter(propertyName);
                                Object value = propertyConverter.getStorableObject(valueString, property.getPropertyType(), request);
                                wrapper.setPropertyValue(propertyName, value);
                            } // for
                            parameters.add(form);
                        } catch (Exception e) {
                            log.error("callAction() cannot create and fill form "+type.getName());
                        } // try/catch
                    } // if
                } // for
            } // for

            if (log.isInfoEnabled()) {
                log.info("callAction() calling method "+method.getName()+" with "+parameters.size()+" parameters");
            } // if
            try {
                descriptor = (TargetDescriptor) method.invoke(target, parameters.toArray());
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            } // try/catch
            if (log.isInfoEnabled()) {
                log.info("callAction() result is "+descriptor);
            } // if
            result = descriptor;
        } // if
        if (log.isInfoEnabled()) {
            log.info("callAction() link="+result);
        } // if
        return result;
    } // callAction()


    private ModelAndView handleResultDescriptor(TargetDescriptor resultDescriptor, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView result = null;
        if (resultDescriptor==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            if (log.isInfoEnabled()) {
                log.info("handleRequestInternal() received link "+resultDescriptor);
            } // if
            if (resultDescriptor!=TargetDescriptor.DONE) {
                if ((resultDescriptor.action!=null)||!"GET".equals(request.getMethod())) {
                    Link link = linkFactory.createLink(request, response, resultDescriptor.bean, resultDescriptor.action, resultDescriptor.view);
                    response.sendRedirect(link.getUrl());
                } else {
                    result = modelAndViewFactory.createModelAndView(resultDescriptor.bean, resultDescriptor.view, request, response);
                } // if
            } // if
        } // if
        return result;
    } // handleResultDescriptor()


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURI().substring(linkFactory.getPrefix(request).length());
        if (log.isInfoEnabled()) {
            log.info("handleRequestInternal() "+url);
        } // if
        try {
            for (Entry<Pattern, Method> entry : methods.entrySet()) {
                Pattern p = entry.getKey();
                if (log.isDebugEnabled()) {
                    log.debug("handleRequestInternal() url pattern "+p.pattern());
                } // if
                Matcher matcher = p.matcher(url);
                if (matcher.matches()) {
                    if (log.isDebugEnabled()) {
                        log.debug("handleRequestInternal() match "+matcher.groupCount());
                    } // if
                    Object target = atHandlers.get(entry.getKey());
                    TargetDescriptor descriptor = new TargetDescriptor(target, null, null);
                    TargetDescriptor resultDescriptor = callAction(request, response, matcher, entry.getValue(), descriptor, target);
                    return handleResultDescriptor(resultDescriptor, request, response);
                } // if
            } // for
            for (String className : handlers.keySet()) {
                LinkHandler linkHandler = handlers.get(className);
                TargetDescriptor descriptor = linkHandler.parseLink(url, response);
                if (descriptor!=null) {
                    if (log.isInfoEnabled()) {
                        log.info("handleRequestInternal() "+linkHandler.getClass().getName()+" hit for "+url);
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
                        if (log.isDebugEnabled()) {
                            log.debug("handleRequestInternal() trying to call action "+descriptor.action);
                        } // if
                        Method method = findMethod(linkHandler, descriptor.action);
                        TargetDescriptor resultDescriptor = callAction(request, response, null, method, descriptor, linkHandler);
                        if (resultDescriptor==null) {
                            for (Object value : model.values()) {
                                // This has to be checked because of special null values in context like $null
                                // if the link is already set don't try to call other methods
                                if ((value!=null)&&(resultDescriptor==null)) {
                                    method = findMethod(value, descriptor.action);
                                    resultDescriptor = callAction(request, response, null, method, descriptor, value);
                                } // if
                            } // for
                        } // if
                        return handleResultDescriptor(resultDescriptor, request, response);
                    } // if
                } // if
            } // for
        } catch (Throwable ex) {
            return modelAndViewFactory.createModelAndView(ex, request, response);
        } // try/catch

        response.sendError(HttpServletResponse.SC_NOT_FOUND);

        return null;
    } // handleRequestInternal()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        Link result = null;
        for (LinkHandler linkScheme : handlers.values()) {
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
