package org.tangram.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.tangram.Constants;
import org.tangram.content.BeanFactory;
import org.tangram.edit.PropertyConverter;
import org.tangram.view.link.LinkFactory;

public class Utils {

    private static BeanFactory beanFactory = null;

    private static LinkFactory linkFactory = null;

    private static ModelAndViewFactory modelAndViewFactory = null;

    private static ViewHandler viewHandler = null;

    private static PropertyConverter propertyConverter = null;

    private static String uriPrefix = null;


    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls, ServletRequest request) {
        T result = null;
        ApplicationContext appContext = (ApplicationContext)request.getAttribute(Constants.ATTRIBUTE_CONTEXT);
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
        String result = title.toLowerCase();
        result = result.replace(" - ", "-");
        result = result.replace("�", "ae");
        result = result.replace("�", "oe");
        result = result.replace("�", "ue");
        result = result.replace("�", "Ae");
        result = result.replace("�", "Oe");
        result = result.replace("�", "Ue");
        result = result.replace("�", "ss");
        char[] specials = { ',', ' ', ':', ';', '"', '?', '*' };
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

} // Utils
