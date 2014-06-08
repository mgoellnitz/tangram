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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.annotate.ActionForm;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkPart;
import org.tangram.controller.ControllerHook;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.util.JavaBean;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


@Named
@Singleton
public class ControllerServices {

    private static final Log log = LogFactory.getLog(ControllerServices.class);

    @Inject
    private ViewUtilities viewUtilities;

    @Inject
    private ViewContextFactory viewContextFactory;

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    @Inject
    private Collection<ControllerHook> controllerHooks = new HashSet<ControllerHook>();

    @Inject
    private PropertyConverter propertyConverter;


    /**
     * creates a model and also calls any registered controller hooks.
     *
     * @param request
     * @param response
     * @param bean
     * @return
     * @throws Exception
     */
    public Map<String, Object> createModel(TargetDescriptor descriptor, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = viewContextFactory.createModel(descriptor.bean, request, response);
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
            return viewContextFactory.createModel(e, request, response);
        } // try/catch
        return model;
    } // createModel()


    public TargetDescriptor callAction(HttpServletRequest request, HttpServletResponse response, Matcher matcher, Method method, TargetDescriptor descriptor,
                                       Object target) throws Throwable, IllegalAccessException {
        TargetDescriptor result = null;
        if (log.isDebugEnabled()) {
            log.debug("callAction() "+method+"@"+target);
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
                        if (log.isDebugEnabled()) {
                            log.debug("callAction() parameter #"+typeIndex+"='"+valueString+"' should be of type "+type.getName());
                        } // if
                        parameters.add(propertyConverter.getStorableObject(valueString, type, request));
                    } // if
                    if (annotation instanceof ActionParameter) {
                        String parameterName = ((ActionParameter) annotation).value();
                        if ("--empty--".equals(parameterName)) {
                            parameterName = type.getSimpleName().toLowerCase();
                        } // if
                        if (parameterMap==null) {
                            parameterMap = viewUtilities.createParameterAccess(request).getParameterMap();
                        } // if
                        if (log.isDebugEnabled()) {
                            log.debug("callAction() parameter "+parameterName+" should be of type "+type.getName());
                        } // if
                        Object value = propertyConverter.getStorableObject(request.getParameter(parameterName), type, request);
                        parameters.add(value);
                    } // if
                    if (annotation instanceof ActionForm) {
                        try {
                            Object form = type.newInstance();
                            JavaBean wrapper = new JavaBean(form);
                            for (String propertyName : wrapper.propertyNames()) {
                                String valueString = request.getParameter(propertyName);
                                Object value = propertyConverter.getStorableObject(valueString, wrapper.getType(propertyName), request);
                                wrapper.set(propertyName, value);
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


    public ViewContext handleResultDescriptor(TargetDescriptor resultDescriptor, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ViewContext result = null;
        if (resultDescriptor==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            if (log.isInfoEnabled()) {
                log.info("handleResultDescriptor() received link "+resultDescriptor);
            } // if
            if (resultDescriptor!=TargetDescriptor.DONE) {
                if (resultDescriptor.action!=null) {
                    try {
                        Link link = linkFactoryAggregator.createLink(request, response, resultDescriptor.bean, resultDescriptor.action, resultDescriptor.view);
                        response.sendRedirect(link.getUrl());
                    } catch (Exception e) {
                        log.error("handleResultDescriptor()", e);
                        result = viewContextFactory.createViewContext(resultDescriptor.bean, resultDescriptor.view, request, response);
                    } // try/catch
                } else {
                    result = viewContextFactory.createViewContext(resultDescriptor.bean, resultDescriptor.view, request, response);
                } // if
            } // if
        } // if
        return result;
    } // handleResultDescriptor()

} // ControllerServices
