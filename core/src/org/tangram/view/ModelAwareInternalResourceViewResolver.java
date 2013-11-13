/**
 *
 * Copyright 2011 Martin Goellnitz
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

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.tangram.Constants;


public class ModelAwareInternalResourceViewResolver extends AbstractModelAwareViewResolver implements
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


    @Override
    public void setServletContext(ServletContext context) {
        filePathPrefix = context.getRealPath("");
    } // setServletContext()


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
        String url = ((AbstractUrlBasedView) result).getUrl();
        int idx = url.indexOf('/');
        if (idx!=0) {
            // TODO: this code is unused for now - we'd like to have VTL or
            // stuff like that on the classpath which comes with Servlet 3
            idx++;
            if (log.isInfoEnabled()) {
                log.info("checkResourceExists("+url+")");
            } // if
            InputStream is = getClass().getResourceAsStream(url);
            if (log.isInfoEnabled()) {
                log.info("checkResourceExists("+url+") is="+is);
            } // if
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
            if (idx==0) {
                // TODO: This only wirks with exploded deployments
                File f = new File(filePathPrefix+url);
                if (log.isInfoEnabled()) {
                    log.info("checkResourceExists() f="+f.getAbsolutePath());
                } // if
                if (!(f.exists())) {
                    result = null;
                } // if
            } else {
                // meaning idx < 0 - no slash in url
                log.warn("checkResourceExists() strange resource name "+url);
                result = null;
            } // if
        } // if
        return result;
    } // checkResourceExists()


    @Override
    protected String getFullViewName(String view, String packageName, String simpleName) {
        if (simpleName.endsWith("[]")) {
            simpleName = simpleName.replace("[]", "_array");
        } // if
        String viewPrefix = StringUtils.hasText(packageName) ? packageName+"/" : "";
        return viewPrefix+simpleName+(Constants.DEFAULT_VIEW.equals(view) ? "" : "."+view);
    } // getFullViewName()


    @Override
    protected View resolveView(String path, Locale locale) throws Exception {
        return delegate.resolveViewName(path, locale);
    } // resolverViewName()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegate, "delegate is null");
        Assert.notNull(filePathPrefix, "path to lookup templates may not be null");
        delegate.setPrefix(prefix);
        delegate.setSuffix(suffix);
    } // afterPropertiesSet()

} // ModelAwareInternalResourceViewResolver
