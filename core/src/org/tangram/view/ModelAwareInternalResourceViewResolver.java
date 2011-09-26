package org.tangram.view;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.tangram.Constants;

public class ModelAwareInternalResourceViewResolver extends AbstractModelAwareViewResolver implements InitializingBean,
        ServletContextAware {

    private UrlBasedViewResolver delegate;

    private String filePathPrefix;

    private String prefix;

    private String suffix;

    private static Log log = LogFactory.getLog(ModelAwareInternalResourceViewResolver.class);


    public UrlBasedViewResolver getDelegate() {
        return delegate;
    }


    public void setDelegate(UrlBasedViewResolver delegate) {
        this.delegate = delegate;
    }


    public void setServletContext(ServletContext context) {
        filePathPrefix = context.getRealPath("");
    } //  setServletContext()


    public String getPrefix() {
        return prefix;
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public String getSuffix() {
        return suffix;
    }


    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


    @Override
    protected View checkResourceExists(View result) {
        String url = ((AbstractUrlBasedView)result).getUrl();
        int idx = url.indexOf('/');
        if (idx!=0) {
            // TODO: this code is unused for now - we'd like to have VTL or stuff like that on the classpath
            idx++ ;
            if (log.isInfoEnabled()) {
                log.info("checkResourceExists("+url+")");
            } // if
            InputStream is = getClass().getResourceAsStream(url);
            if (log.isInfoEnabled()) {
                log.info("checkResourceExists("+url+") is="+is);
            } // if
            /*
             * what was this good for? it seems to be duplicated... URL u = getClass().getResource(url); if
             * (log.isInfoEnabled()) { log.info("checkResourceExists("+url+") u="+u); } // if
             */
            if (is!=null) {
                if (log.isInfoEnabled()) {
                    log.info("checkResourceExists("+url+") exists!");
                } // if
                try {
                    is.close();
                } catch (Exception e) {
                    log.error("checkResourceExists() ", e);
                } // try/catch
            } else {
                result = null;
            } // if
        } else {
            if (url.startsWith("/")) {
                File f = new File(filePathPrefix+url);
                if (log.isInfoEnabled()) {
                    log.info("checkResourceExists() f="+f.getAbsolutePath());
                } // if
                if ( !(f.exists())) {
                    result = null;
                } // if
            } else {
                log.warn("checkResourceExists() strange resource name "+url);
                result = null;
            } // if
        } // if
        return result;
    } // checkResourceExists()


    @Override
    protected String getFullViewName(String view, String packageName, String simpleName) {
        return packageName+"/"+simpleName+(Constants.DEFAULT_VIEW.equals(view) ? "" : "."+view);
    } // getFullViewName()


    @Override
    protected View resolveViewName(String path, Locale locale) throws Exception {
        return delegate.resolveViewName(path, locale);
    } // resolverViewName()


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegate, "delegate is null");
        Assert.notNull(filePathPrefix, "path to lookup templates may not be null");
        delegate.setPrefix(prefix);
        delegate.setSuffix(suffix);
    } // afterPropertiesSet()

} // ModelAwareInternalResourceViewResolver
