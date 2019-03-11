/**
 *
 * Copyright 2014-2019 Martin Goellnitz
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
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
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.tangram.logic.ClassRepository;
import org.tangram.util.JavaBean;
import org.tangram.view.PropertyConverter;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.Utils;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


/**
 * Meta handler to deal with all registered - static in java code or dynamic in the repository - link handlers.
 *
 * Calls methods associated with a given request and handles their results, redirects, or error conditions.
 */
@Named
@Singleton
public class MetaLinkHandler implements LinkHandlerRegistry, LinkFactory, BeanListener {

    private static final Logger LOG = LoggerFactory.getLogger(MetaLinkHandler.class);

    @Inject
    private BeanFactory<?> beanFactory;

    @Inject
    private ClassRepository classRepository;

    @Inject
    private ViewUtilities viewUtilities;

    @Inject
    private ViewContextFactory viewContextFactory;

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    @Inject
    private Set<ControllerHook> controllerHooks;

    @Inject
    private PropertyConverter propertyConverter;

    private final Map<String, LinkHandler> staticLinkHandlers = new HashMap<>();

    private Map<String, LinkHandler> handlers;

    private final Map<Pattern, Method> staticMethods = new HashMap<>();

    private Map<Pattern, Method> methods;

    private final Map<Pattern, Object> staticAtHandlers = new HashMap<>();

    private Map<Pattern, Object> atHandlers;


    /**
     * Create a model and also calls any registered controller hooks.
     *
     * TODO: This is duplicate code from abstract link handler
     *
     * @param descriptor target descriptor to generate model map from
     * @param request currently handled request
     * @param response response to answer given request
     * @return map resembling the model
     * @throws Exception no exception is expected but anything can happen from the controller hooks
     */
    private Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = viewContextFactory.createModel(descriptor.bean, request, response);
        for (ControllerHook controllerHook : controllerHooks) {
            LOG.debug("createModel() {}", controllerHook.getClass().getName());
            boolean result = controllerHook.intercept(descriptor, model, request, response);
            if (result) {
                return null;
            } // if
        } // for
        return model;
    } // createModel()


    private TargetDescriptor callAction(HttpServletRequest request, HttpServletResponse response, Matcher matcher, Method method, TargetDescriptor descriptor,
            Object target) throws Throwable, IllegalAccessException {
        TargetDescriptor result = null;
        LOG.debug("callAction() {} @ {}", method, target);

        if (method!=null) {
            descriptor.action = null;
            List<Object> parameters = new ArrayList<>();
            Annotation[][] allAnnotations = method.getParameterAnnotations();
            Class<? extends Object>[] parameterTypes = method.getParameterTypes();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            RequestParameterAccess parameterAccess = viewUtilities.createParameterAccess(request);
            for (int typeIndex = 0; typeIndex<parameterTypes.length; typeIndex++) {
                Annotation[] annotations = allAnnotations[typeIndex];
                Class<? extends Object> type = parameterTypes[typeIndex];
                Type genericType = genericParameterTypes[typeIndex];
                if (type.equals(HttpServletRequest.class)) {
                    parameters.add(request);
                } // if
                if (type.equals(HttpServletResponse.class)) {
                    parameters.add(response);
                } // if
                for (Annotation annotation : annotations) {
                    if (annotation instanceof LinkPart) {
                        String valueString = matcher.group(((LinkPart) annotation).value());
                        LOG.debug("callAction() parameter #{}='{}' should be of type {}", typeIndex, valueString, type.getName());
                        parameters.add(propertyConverter.getStorableObject(null, valueString, type, genericType, request));
                    } // if
                    if (annotation instanceof ActionParameter) {
                        String parameterName = ((ActionParameter) annotation).value();
                        if ("--empty--".equals(parameterName)) {
                            parameterName = type.getSimpleName().toLowerCase();
                        } // if
                        LOG.debug("callAction() parameter {} should be of type {} {}", parameterName, type.getSimpleName(), type.getName());
                        boolean isBlob = "byte[]".equals(type.getSimpleName());
                        Object value = isBlob ? parameterAccess.getData(parameterName) : propertyConverter.getStorableObject(null, request.getParameter(parameterName), type, genericType, request);
                        parameters.add(value);
                    } // if
                    if (annotation instanceof ActionForm) {
                        try {
                            Object form = type.newInstance();
                            JavaBean wrapper = new JavaBean(form);
                            for (String propertyName : wrapper.propertyNames()) {
                                String valueString = request.getParameter(propertyName);
                                Class<? extends Object> t = wrapper.getType(propertyName);
                                Type gt = wrapper.getGenericType(propertyName);
                                boolean isBlob = "byte[]".equals(t.getSimpleName());
                                Object value = isBlob ? parameterAccess.getData(propertyName) : propertyConverter.getStorableObject(null, valueString, t, gt, request);
                                wrapper.set(propertyName, value);
                            } // for
                            parameters.add(form);
                        } catch (Exception e) {
                            LOG.error("callAction() cannot create and fill form "+type.getName(), e);
                        } // try/catch
                    } // if
                } // for
            } // for

            LOG.info("callAction() calling method {} with {} parameters", method.getName(), parameters.size());
            try {
                descriptor = (TargetDescriptor) method.invoke(target, parameters.toArray());
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            } // try/catch
            LOG.info("callAction() result is {}", descriptor);
            result = descriptor;
        } // if
        LOG.info("callAction() link={}", result);
        return result;
    } // callAction()


    private ViewContext handleResultDescriptor(TargetDescriptor resultDescriptor, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ViewContext result = null;
        if ((resultDescriptor!=null)&&(resultDescriptor!=TargetDescriptor.DONE)) {
            LOG.info("handleResultDescriptor() received link {}", resultDescriptor);
            if (resultDescriptor.action!=null) {
                try {
                    Link link = linkFactoryAggregator.createLink(request, response, resultDescriptor.bean, resultDescriptor.action, resultDescriptor.view);
                    response.sendRedirect(link.getUrl());
                } catch (Exception e) {
                    LOG.error("handleResultDescriptor()", e);
                    result = viewContextFactory.createViewContext(resultDescriptor.bean, resultDescriptor.view, request, response);
                } // try/catch
            } else {
                Map<String, Object> model = createModel(resultDescriptor, request, response);
                result = viewContextFactory.createViewContext(model, resultDescriptor.view);
            } // if
        } // if
        return result;
    } // handleResultDescriptor()


    /**
     * Register a potential at handler.
     *
     * @param handlerClass class annotated as link handler or implementing the interface
     * @param handler instance of the given class
     * @param immutable true if called by static java classes, false for classes from repository
     * @throws SecurityException
     */
    private void registerAtHandler(Class<? extends Object> handlerClass, Object handler, boolean immutable) throws SecurityException {
        if (handlerClass.getAnnotation(org.tangram.annotate.LinkHandler.class)!=null) {
            for (Method m : handlerClass.getMethods()) {
                LinkAction linkAction = m.getAnnotation(LinkAction.class);
                LOG.debug("registerAtHandler({}) linkAction={}", handlerClass.getName(), linkAction);
                LOG.debug("registerAtHandler() {} :{}", m.getName(), m.getReturnType());
                if (!TargetDescriptor.class.equals(m.getReturnType())) {
                    linkAction = null;
                } // if
                if ((linkAction!=null)&&(StringUtils.isNotBlank(linkAction.value()))) {
                    Pattern pathPattern = Pattern.compile(linkAction.value().replace("/", "\\/"));
                    LOG.info("registerAtHandler() registering {} for {}@{}", pathPattern, m.getName(), handler);
                    methods.put(pathPattern, m);
                    atHandlers.put(pathPattern, handler);
                    if (immutable) {
                        if (!staticMethods.containsKey(pathPattern)) {
                            staticMethods.put(pathPattern, m);
                        } // if
                        if (!staticAtHandlers.containsKey(pathPattern)) {
                            staticAtHandlers.put(pathPattern, handler);
                        } // if
                    } // if
                } // if
            } // if
        } // if
    } // registerAtHandler()


    private void registerInterfaceHandler(LinkHandler handler, boolean immutable) {
        handlers.put(handler.getClass().getName(), handler);
        if (immutable) {
            staticLinkHandlers.put(handler.getClass().getName(), handler);
        } // if
    } // registerInterfaceHandler()


    private void registerLinkHandler(Object handler, boolean isAnnotated, boolean immutable) {
        if (handler instanceof BeanFactoryAware) {
            ((BeanFactoryAware) handler).setBeanFactory(beanFactory);
        } // if
        if (handler instanceof LinkHandler) {
            LinkHandler linkHandler = (LinkHandler) handler;
            registerInterfaceHandler(linkHandler, immutable);
            if (!staticLinkHandlers.containsValue(linkHandler)) {
                staticLinkHandlers.put(handler.getClass().getName(), linkHandler);
                handlers.put(handler.getClass().getName(), linkHandler);
            } // if
        } // if
        if (isAnnotated) {
            Class<? extends Object> handlerClass = handler.getClass();
            registerAtHandler(handlerClass, handler, immutable);
        } // if
    } // registerLinkHandler()


    @Override
    public void registerLinkHandler(Object handler) {
        registerLinkHandler(handler, handler.getClass().getAnnotation(org.tangram.annotate.LinkHandler.class)!=null, true);
    } // registerLinkHandler()


    private <T extends Object> T createInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        LOG.info("createInstance() {} is marked @LinkHandler", clazz.getName());
        T instance = clazz.newInstance();
        LOG.info("createInstance() {} instanciated", instance.getClass().getName());
        return instance;
    } // createInstance()


    @Override
    public void reset() {
        methods = new HashMap<>();
        methods.putAll(staticMethods);
        atHandlers = new HashMap<>();
        atHandlers.putAll(staticAtHandlers);
        handlers = new HashMap<>();
        handlers.putAll(staticLinkHandlers);
        for (Map.Entry<String, Class<Object>> entry : classRepository.getAnnotated(org.tangram.annotate.LinkHandler.class).entrySet()) {
            try {
                registerLinkHandler(createInstance(entry.getValue()), true, false);
            } catch (InstantiationException|IllegalAccessException e) {
                LOG.error("reset()", e);
            } // try/catch
        } // for
        for (Map.Entry<String, Class<LinkHandler>> entry : classRepository.get(LinkHandler.class).entrySet()) {
            try {
                registerLinkHandler(createInstance(entry.getValue()), false, false);
            } catch (IllegalAccessException|InstantiationException e) {
                LOG.error("reset()", e);
            } // try/catch
        } // for
    } // reset()


    public ViewContext handleRequest(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String url = request.getRequestURI().substring(linkFactoryAggregator.getPrefix(request).length());
        int idx = url.indexOf(';');
        if (idx>0) {
            url = url.substring(0, idx);
        } // if
        LOG.info("handleRequest() {}", url);
        Utils.setPrimaryBrowserLanguageForJstl(request);
        for (Map.Entry<Pattern, Method> entry : methods.entrySet()) {
            Pattern p = entry.getKey();
            LOG.debug("handleRequest() url pattern {}", p.pattern());
            Matcher matcher = p.matcher(url);
            if (matcher.matches()) {
                LOG.debug("handleRequest() match {}", matcher.groupCount());
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
                LOG.info("handleRequest() {} hit for {}", linkHandler.getClass().getName(), url);
                LOG.debug("handleRequest() found bean {}", descriptor.bean);

                // Map<String, Object> model = createModel(descriptor, request, response);
                TargetDescriptor resultDescriptor = descriptor;
                if (descriptor.action!=null) {
                    LOG.debug("handleRequest() Calling {}@{}", descriptor.action, linkHandler.getClass().getSimpleName());
                    Method method = linkFactoryAggregator.findMethod(linkHandler, descriptor.action);
                    resultDescriptor = callAction(request, response, null, method, descriptor, linkHandler);
                    if (resultDescriptor==null) {
                        LOG.debug("handleRequest() Calling '{}' at descriptor bean values", descriptor.action);
                        for (Object value : createModel(descriptor, request, response).values()) {
                            // This has to be checked because of special null values in context like $null
                            // if the link is already set don't try to call other methods
                            LOG.debug("handleRequest() trying to find action {} at {}", descriptor.action, value);
                            if ((value!=null)&&(resultDescriptor==null)) {
                                LOG.debug("handleRequest() trying to call action {}@{}", descriptor.action, value.getClass().getSimpleName());
                                method = linkFactoryAggregator.findMethod(value, descriptor.action);
                                resultDescriptor = callAction(request, response, null, method, descriptor, value);
                            } // if
                        } // for
                    } // if
                } // if
                return handleResultDescriptor(resultDescriptor, request, response);
            } // if
        } // for
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    } // handleRequest()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        for (LinkFactory factory : handlers.values()) {
            Link result = factory.createLink(request, response, bean, action, view);
            if (result!=null) {
                return result;
            } // if
        } // for
        return null;
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        linkFactoryAggregator.registerFactory(this);
        classRepository.addListener(this);
    } // afterPropertiesSet()

} // MetaLinkHandler
