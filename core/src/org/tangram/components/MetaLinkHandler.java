/**
 *
 * Copyright 2014 Martin Goellnitz
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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.ActionForm;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkPart;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanFactoryAware;
import org.tangram.content.BeanListener;
import org.tangram.controller.ControllerHook;
import org.tangram.controller.CustomViewProvider;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.logic.ClassRepository;
import org.tangram.util.JavaBean;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


@Named
@Singleton
public class MetaLinkHandler implements LinkHandlerRegistry, LinkFactory, BeanListener {

    private static final Logger LOG = LoggerFactory.getLogger(MetaLinkHandler.class);

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private ClassRepository classRepository;

    @Inject
    private ViewUtilities viewUtilities;

    @Inject
    private ViewContextFactory viewContextFactory;

    @Inject
    private CustomViewProvider customViewProvider;

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    @Inject
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();

    @Inject
    private PropertyConverter propertyConverter;

    private final Map<String, LinkHandler> staticLinkHandlers = new HashMap<String, LinkHandler>();

    private Map<String, LinkHandler> handlers;

    private final Map<Pattern, Method> staticMethods = new HashMap<Pattern, Method>();

    private Map<Pattern, Method> methods;

    private final Map<Pattern, Object> staticAtHandlers = new HashMap<Pattern, Object>();

    private Map<Pattern, Object> atHandlers;

    private final Map<Object, Collection<String>> customViews = new HashMap<Object, Collection<String>>();


    /**
     * creates a model and also calls any registered controller hooks.
     *
     * @param descriptor
     * @param request
     * @param response
     * @return map resembling the model
     * @throws Exception
     */
    public Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = viewContextFactory.createModel(descriptor.bean, request, response);
        try {
            for (ControllerHook controllerHook : controllerHooks) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createModel() "+controllerHook.getClass().getName());
                } // if
                boolean result = controllerHook.intercept(descriptor, model, request, response);
                if (result) {
                    return null;
                } // if
            } // for
        } catch (Exception e) {
            return viewContextFactory.createModel(e, request, response);
        } // try/catch
        return model;
    } // createModel()


    private TargetDescriptor callAction(HttpServletRequest request, HttpServletResponse response, Matcher matcher, Method method, TargetDescriptor descriptor,
                                        Object target) throws Throwable, IllegalAccessException {
        TargetDescriptor result = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("callAction() "+method+"@"+target);
        } // if

        if (method!=null) {
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
                Map<String, String[]> parameterMap = null;
                for (Annotation annotation : annotations) {
                    if (annotation instanceof LinkPart) {
                        String valueString = matcher.group(((LinkPart) annotation).value());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("callAction() parameter #"+typeIndex+"='"+valueString+"' should be of type "+type.getName());
                        } // if
                        parameters.add(propertyConverter.getStorableObject(null, valueString, type, request));
                    } // if
                    if (annotation instanceof ActionParameter) {
                        String parameterName = ((ActionParameter) annotation).value();
                        if ("--empty--".equals(parameterName)) {
                            parameterName = type.getSimpleName().toLowerCase();
                        } // if
                        if (parameterMap==null) {
                            parameterMap = viewUtilities.createParameterAccess(request).getParameterMap();
                        } // if
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("callAction() parameter "+parameterName+" should be of type "+type.getName());
                        } // if
                        Object value = propertyConverter.getStorableObject(null, request.getParameter(parameterName), type, request);
                        parameters.add(value);
                    } // if
                    if (annotation instanceof ActionForm) {
                        try {
                            Object form = type.newInstance();
                            JavaBean wrapper = new JavaBean(form);
                            for (String propertyName : wrapper.propertyNames()) {
                                String valueString = request.getParameter(propertyName);
                                Object value = propertyConverter.getStorableObject(null, valueString, wrapper.getType(propertyName), request);
                                wrapper.set(propertyName, value);
                            } // for
                            parameters.add(form);
                        } catch (Exception e) {
                            LOG.error("callAction() cannot create and fill form "+type.getName());
                        } // try/catch
                    } // if
                } // for
            } // for

            if (LOG.isInfoEnabled()) {
                LOG.info("callAction() calling method "+method.getName()+" with "+parameters.size()+" parameters");
            } // if
            try {
                descriptor = (TargetDescriptor) method.invoke(target, parameters.toArray());
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            } // try/catch
            if (LOG.isInfoEnabled()) {
                LOG.info("callAction() result is "+descriptor);
            } // if
            result = descriptor;
        } // if
        if (LOG.isInfoEnabled()) {
            LOG.info("callAction() link="+result);
        } // if
        return result;
    } // callAction()


    private ViewContext handleResultDescriptor(TargetDescriptor resultDescriptor, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ViewContext result = null;
        if (resultDescriptor!=null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("handleResultDescriptor() received link "+resultDescriptor);
            } // if
            if (resultDescriptor!=TargetDescriptor.DONE) {
                if (resultDescriptor.action!=null) {
                    try {
                        Link link = linkFactoryAggregator.createLink(request, response, resultDescriptor.bean, resultDescriptor.action, resultDescriptor.view);
                        response.sendRedirect(link.getUrl());
                    } catch (Exception e) {
                        LOG.error("handleResultDescriptor()", e);
                        result = viewContextFactory.createViewContext(resultDescriptor.bean, resultDescriptor.view, request, response);
                    } // try/catch
                } else {
                    result = viewContextFactory.createViewContext(resultDescriptor.bean, resultDescriptor.view, request, response);
                } // if
            } // if
        } // if
        return result;
    } // handleResultDescriptor()


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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("registerLinkHandler("+handlerClass.getName()+") linkAction="+linkAction);
                        LOG.debug("registerLinkHandler() "+m.getName()+" :"+m.getReturnType());
                    } // if
                    if (!TargetDescriptor.class.equals(m.getReturnType())) {
                        linkAction = null;
                    } // if
                    if (linkAction!=null) {
                        if (StringUtils.isNotBlank(linkAction.value())) {
                            Pattern pathPattern = Pattern.compile(linkAction.value().replace("/", "\\/"));
                            if (LOG.isInfoEnabled()) {
                                LOG.info("registerLinkHandler() registering "+pathPattern+" for "+m.getName()+"@"+handler);
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
        // remove current custom views from view provider
        for (Object key : customViews.keySet()) {
            for (String view : customViews.get(key)) {
                customViewProvider.getCustomLinkViews().remove(view);
            } // for
        } // for
        if (LOG.isInfoEnabled()) {
            LOG.info("reset() view provider "+customViewProvider);
            LOG.info("reset() custom views in provider "+customViewProvider.getCustomLinkViews());
        } // if
        for (Map.Entry<String, Class<LinkHandler>> entry : classRepository.get(LinkHandler.class).entrySet()) {
            try {
                String annotation = entry.getKey();
                Class<LinkHandler> clazz = entry.getValue();
                if (LinkHandler.class.isAssignableFrom(clazz)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("reset() "+clazz.getName()+" is a LinkScheme");
                    } // if
                    LinkHandler linkHandler = clazz.newInstance();
                    if (LOG.isInfoEnabled()) {
                        LOG.info("reset() "+clazz.getName()+" instanciated");
                    } // if
                    if (linkHandler instanceof BeanFactoryAware) {
                        ((BeanFactoryAware) linkHandler).setBeanFactory(beanFactory);
                    } // if
                    Collection<String> schemeCustomViews = linkHandler.getCustomViews();
                    customViews.put(linkHandler, schemeCustomViews);
                    customViewProvider.getCustomLinkViews().addAll(schemeCustomViews);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("reset() adding custom views "+schemeCustomViews);
                    } // if
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("reset() custom views in provider "+customViewProvider.getCustomLinkViews());
                    } // if
                    handlers.put(annotation, linkHandler);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("reset() "+clazz.getName()+" is not a LinkScheme");
                    } // if
                } // if
            } catch (Throwable e) {
                // who cares
                if (LOG.isErrorEnabled()) {
                    LOG.error("reset()", e);
                } // if
            } // try/catch
        } // for
        if (LOG.isInfoEnabled()) {
            LOG.info("reset() custom views in default controller "+customViewProvider.getCustomLinkViews());
        } // if
    } // reset()


    public ViewContext handleRequest(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String url = request.getRequestURI().substring(linkFactoryAggregator.getPrefix(request).length());
        if (LOG.isInfoEnabled()) {
            LOG.info("handleRequestl() "+url);
        } // if
        Utils.setPrimaryBrowserLanguageForJstl(request);
        for (Map.Entry<Pattern, Method> entry : methods.entrySet()) {
            Pattern p = entry.getKey();
            if (LOG.isDebugEnabled()) {
                LOG.debug("handleRequest() url pattern "+p.pattern());
            } // if
            Matcher matcher = p.matcher(url);
            if (matcher.matches()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("handleRequest() match "+matcher.groupCount());
                } // if
                Object target = atHandlers.get(entry.getKey());
                TargetDescriptor descriptor = new TargetDescriptor(target, null, null);
                TargetDescriptor resultDescriptor = callAction(request, response, matcher, entry.getValue(), descriptor, target);
                return (resultDescriptor!=TargetDescriptor.DONE) ? handleResultDescriptor(resultDescriptor, request, response) : null;
            } // if
        } // for
        for (String className : handlers.keySet()) {
            LinkHandler linkHandler = handlers.get(className);
            TargetDescriptor descriptor = linkHandler.parseLink(url, response);
            if (descriptor!=null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("handleRequest() "+linkHandler.getClass().getName()+" hit for "+url);
                } // if
                if (LOG.isDebugEnabled()) {
                    LOG.debug("handleRequest() found bean "+descriptor.bean);
                } // if

                Map<String, Object> model = createModel(descriptor, request, response);
                if (descriptor.action==null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("handleRequest() handing over to view "+descriptor.view);
                    } // if
                    return viewContextFactory.createViewContext(model, descriptor.view);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("handleRequest() trying to call action "+descriptor.action);
                    } // if
                    Method method = linkFactoryAggregator.findMethod(linkHandler, descriptor.action);
                    TargetDescriptor resultDescriptor = callAction(request, response, null, method, descriptor, linkHandler);
                    if (resultDescriptor==null) {
                        for (Object value : model.values()) {
                            // This has to be checked because of special null values in context like $null
                            // if the link is already set don't try to call other methods
                            if ((value!=null)&&(resultDescriptor==null)) {
                                method = linkFactoryAggregator.findMethod(value, descriptor.action);
                                resultDescriptor = callAction(request, response, null, method, descriptor, value);
                            } // if
                        } // for
                    } // if
                    return handleResultDescriptor(resultDescriptor, request, response);
                } // if
            } // if
        } // for
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    } // handleRequest()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        return linkFactoryAggregator.createLink(handlers.values(), request, response, bean, action, view);
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        linkFactoryAggregator.registerFactory(this);
        classRepository.addListener(this);
        reset();
    } // afterPropertiesSet()

} // MetaLinkHandler
