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
package org.tangram.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.servlet.DispatcherServlet;
import org.tangram.content.BeanFactory;
import org.tangram.edit.PropertyConverter;
import org.tangram.view.link.LinkFactory;

public final class Utils {

    private static BeanFactory beanFactory = null;

    private static LinkFactory linkFactory = null;

    private static ModelAndViewFactory modelAndViewFactory = null;

    private static ViewHandler viewHandler = null;

    private static PropertyConverter propertyConverter = null;

    private static String uriPrefix = null;


    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls, ServletRequest request) {
        T result = null;
        ApplicationContext appContext = (ApplicationContext)request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (appContext!=null) {
            result = appContext.getBean(cls);
        } // if
        if (result==null) {
            throw new RuntimeException("getBeanFromContext() no item of type "+cls.getName()+" available.");
        } // if
        return result;
    } // getBeanFromContext()


    public static BeanFactory getBeanFactory(ServletRequest request) {
        if (beanFactory==null) {
            beanFactory = getBeanFromContext(BeanFactory.class, request);
        } // if
        return beanFactory;
    } // getBeanFactory()


    public static LinkFactory getLinkFactory(ServletRequest request) {
        if (linkFactory==null) {
            linkFactory = getBeanFromContext(LinkFactory.class, request);
        } // if
        return linkFactory;
    } // getLinkFactory()


    public static ModelAndViewFactory getModelAndViewFactory(ServletRequest request) {
        if (modelAndViewFactory==null) {
            modelAndViewFactory = getBeanFromContext(ModelAndViewFactory.class, request);
        } // if
        return modelAndViewFactory;
    } // getModelAndViewFactory()


    public static ViewHandler getViewHandler(ServletRequest request) {
        if (viewHandler==null) {
            viewHandler = getBeanFromContext(ViewHandler.class, request);
        } // if
        return viewHandler;
    } // getViewHandler()


    public static PropertyConverter getPropertyConverter(ServletRequest request) {
        if (propertyConverter==null) {
            propertyConverter = getBeanFromContext(PropertyConverter.class, request);
        } // if
        return propertyConverter;
    } // getPropertyConverter()


    /**
     * transform a title into a URL conform UTF-8 encoded string
     * 
     * @param title
     * @return the URL form of the title
     * @throws UnsupportedEncodingException
     */
    public static String urlize(String title) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(title)) {
            return "-";
        } // if
        String result = title.toLowerCase();
        result = result.replace(" - ", "-");
        result = result.replace("ä", "ae");
        result = result.replace("ö", "oe");
        result = result.replace("ü", "ue");
        // TODO: These three should be useless
        result = result.replace("Ä", "Ae");
        result = result.replace("Ö", "Oe");
        result = result.replace("Ü", "Ue");
        result = result.replace("ß", "ss");
        char[] specials = { ',', ' ', ':', ';', '"', '?', '!', '*' };
        for (char c : specials) {
            result = result.replace(c, '-');
        } // for
        return URLEncoder.encode(result, "UTF-8");
    } // urlize()


    public static String getUriPrefix(HttpServletRequest request) {
        if (uriPrefix==null) {
            String contextPath = request.getContextPath();
            if (contextPath.length()==1) {
                contextPath = "";
            } // if
            uriPrefix = contextPath;
        } // if
        return uriPrefix;
    } // getUriPrefix()


    /**
     * TODO: this is not really a view utility - place it somewhere else or replace it
     * 
     * create a bean wrapper instance from a bean object and prepares it with a conversion service if available
     * 
     * @param bean
     * @param conversionService
     * @return
     */
    public static BeanWrapper createWrapper(Object bean, HttpServletRequest request) {
        BeanWrapper wrapper;
        wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        try {
            // TODO: Do this exactly once!
            ConversionService conversionService = getBeanFromContext(ConversionService.class, request);
            if (conversionService!=null) {
                wrapper.setConversionService(conversionService);
            } // if
        } catch (Exception e) {
            // conversion services are still optional for some time
        } // try/catch
        return wrapper;
    } // createWrapper()


    /**
     * just to protect this stuff from being instantiated
     */
    private Utils() {
    } // Utils()

} // Utils
