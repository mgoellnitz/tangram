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
package org.tangram.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.tangram.content.BeanFactory;
import org.tangram.view.link.LinkFactory;


public final class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    private static ApplicationContext applicationContext;

    private static BeanFactory beanFactory = null;

    private static LinkFactory linkFactory = null;

    private static ModelAndViewFactory modelAndViewFactory = null;

    private static ViewHandler viewHandler = null;

    private static PropertyConverter propertyConverter = null;

    private static String uriPrefix = null;

    private static ConversionService conversionService;


    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }



    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls) {
        T result = null;
        ApplicationContext appContext = getApplicationContext();
        if (appContext!=null) {
            result = appContext.getBean(cls);
        } // if
        if (result==null) {
            throw new RuntimeException("getBeanFromContext() no item of type "+cls.getName()+" available.");
        } // if
        return result;
    } // getBeanFromContext()


    public static BeanFactory getBeanFactory() {
        if (beanFactory==null) {
            beanFactory = getBeanFromContext(BeanFactory.class);
        } // if
        return beanFactory;
    } // getBeanFactory()


    public static LinkFactory getLinkFactory() {
        if (linkFactory==null) {
            linkFactory = getBeanFromContext(LinkFactory.class);
        } // if
        return linkFactory;
    } // getLinkFactory()


    public static ModelAndViewFactory getModelAndViewFactory() {
        if (modelAndViewFactory==null) {
            modelAndViewFactory = getBeanFromContext(ModelAndViewFactory.class);
        } // if
        return modelAndViewFactory;
    } // getModelAndViewFactory()


    public static ViewHandler getViewHandler() {
        if (viewHandler==null) {
            viewHandler = getBeanFromContext(ViewHandler.class);
        } // if
        return viewHandler;
    } // getViewHandler()


    public static PropertyConverter getPropertyConverter() {
        if (propertyConverter==null) {
            propertyConverter = getBeanFromContext(PropertyConverter.class);
        } // if
        return propertyConverter;
    } // getPropertyConverter()


    public static ConversionService getConversionsService() {
        if (conversionService==null) {
            conversionService = Utils.getBeanFromContext(ConversionService.class);
        } // if
        return conversionService;
    } // getConversionsService()



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
        result = result.replace("ß", "ss");
        char[] specials = {',', ' ', ':', ';', '"', '?', '!', '*'};
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
     * create a bean wrapper instance from a bean object and prepare it with a conversion service if available.
     *
     * @param bean
     * @param conversionService
     * @return
     */
    public static BeanWrapper createWrapper(Object bean) {
        BeanWrapper wrapper;
        wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        try {
            ConversionService conversionService = getConversionsService();
            if (conversionService!=null) {
                wrapper.setConversionService(conversionService);
            } // if
            if (log.isInfoEnabled()) {
                log.info("createWrapper() conversion service "+wrapper.getConversionService());
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
