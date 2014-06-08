/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.tangram.annotate.LinkAction;
import org.tangram.components.ControllerServices;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanFactoryAware;
import org.tangram.content.BeanListener;
import org.tangram.controller.CustomViewProvider;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.logic.ClassRepository;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;


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
    private ClassRepository classRepository;

    @Inject
    private CustomViewProvider customViewProvider;

    @Inject
    protected ViewContextFactory viewContextFactory;

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    @Inject
    private ControllerServices controllerServices;

    private final Map<String, LinkHandler> staticLinkHandlers = new HashMap<String, LinkHandler>();

    private Map<String, LinkHandler> handlers;

    private final Map<Pattern, Method> staticMethods = new HashMap<Pattern, Method>();

    private Map<Pattern, Method> methods;

    private final Map<Pattern, Object> staticAtHandlers = new HashMap<Pattern, Object>();

    private Map<Pattern, Object> atHandlers;

    private final Map<Object, Collection<String>> customViews = new HashMap<Object, Collection<String>>();


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
                customViewProvider.getCustomLinkViews().remove(view);
            } // for
        } // for
        if (log.isInfoEnabled()) {
            log.info("reset() custom views in default controller "+customViewProvider.getCustomLinkViews());
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
                    customViewProvider.getCustomLinkViews().addAll(schemeCustomViews);
                    if (log.isInfoEnabled()) {
                        log.info("reset() adding custom views "+schemeCustomViews);
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("reset() custom views in default controller "+customViewProvider.getCustomLinkViews());
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
            log.info("reset() custom views in default controller "+customViewProvider.getCustomLinkViews());
        } // if
    } // reset()


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURI().substring(linkFactoryAggregator.getPrefix(request).length());
        if (log.isInfoEnabled()) {
            log.info("handleRequestInternal() "+url);
        } // if
        Utils.setPrimaryBrowserLanguageForJstl(request);
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
                    TargetDescriptor resultDescriptor = controllerServices.callAction(request, response, matcher, entry.getValue(), descriptor, target);
                    ViewContext viewContext = controllerServices.handleResultDescriptor(resultDescriptor, request, response);
                    if (log.isDebugEnabled()) {
                        log.debug("handleRequestInternal() view context "+viewContext);
                    } // if
                    return SpringViewUtilities.createModelAndView(viewContext);
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

                    Map<String, Object> model = controllerServices.createModel(descriptor, request, response);
                    if (descriptor.action==null) {
                        if (log.isInfoEnabled()) {
                            log.info("handleRequestInternal() handing over to view "+descriptor.view);
                        } // if
                        ViewContext viewContext = viewContextFactory.createViewContext(model, descriptor.view);
                        return SpringViewUtilities.createModelAndView(viewContext);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("handleRequestInternal() trying to call action "+descriptor.action);
                        } // if
                        Method method = linkFactoryAggregator.findMethod(linkHandler, descriptor.action);
                        TargetDescriptor resultDescriptor = controllerServices.callAction(request, response, null, method, descriptor, linkHandler);
                        if (resultDescriptor==null) {
                            for (Object value : model.values()) {
                                // This has to be checked because of special null values in context like $null
                                // if the link is already set don't try to call other methods
                                if ((value!=null)&&(resultDescriptor==null)) {
                                    method = linkFactoryAggregator.findMethod(value, descriptor.action);
                                    resultDescriptor = controllerServices.callAction(request, response, null, method, descriptor, value);
                                } // if
                            } // for
                        } // if
                        ViewContext viewContext = controllerServices.handleResultDescriptor(resultDescriptor, request, response);
                        return SpringViewUtilities.createModelAndView(viewContext);
                    } // if
                } // if
            } // for
        } catch (Throwable ex) {
            ViewContext viewContext = viewContextFactory.createViewContext(ex, request, response);
            return SpringViewUtilities.createModelAndView(viewContext);
        } // try/catch

        response.sendError(HttpServletResponse.SC_NOT_FOUND);

        return null;
    } // handleRequestInternal()


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

} // MetaController
