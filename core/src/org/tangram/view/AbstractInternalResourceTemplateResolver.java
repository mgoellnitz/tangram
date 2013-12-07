/**
 *
 * Copyright 2013 Martin Goellnitz
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Generic superclass for all implementations using internal resources - namely JSP file - as templates.
 *
 * @param <>> Type for the view technology in use e.g. Strings for filenames
 */
public abstract class AbstractInternalResourceTemplateResolver<T extends Object> extends AbstractTemplateResolver<T> {

    private final static Log log = LogFactory.getLog(AbstractInternalResourceTemplateResolver.class);

    private String prefix;

    private String suffix;

    private String filePathPrefix;


    public AbstractInternalResourceTemplateResolver() {
        super(true, "/");
        prefix = "/WEB-INF/view/jsp/";
        suffix = ".jsp";
    } // AbstractInternalResourceTemplateResolver()


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


    public void setFilePathPrefix(String filePathPrefix) {
        this.filePathPrefix = filePathPrefix;
    }


    protected String checkJspExists(String result) {
        String url = result;
        int idx = url.indexOf('/');
        if (log.isDebugEnabled()) {
            log.debug("checkJspExists("+url+") idx="+idx);
        } // if
        if (idx!=0) {
            // TODO: this code is unused for now - we'd like to have VTL or
            // stuff like that on the classpath which comes with Servlet 3
            idx++;
            if (log.isDebugEnabled()) {
                log.debug("checkJspExists("+url+")");
            } // if
            InputStream is = getClass().getResourceAsStream(url);
            if (log.isDebugEnabled()) {
                log.debug("checkJspExists("+url+") is="+is);
            } // if
            if (is!=null) {
                if (log.isInfoEnabled()) {
                    log.info("checkJspExists("+url+") exists!");
                } // if
                try {
                    is.close();
                } catch (Exception e) {
                    log.error("checkJspExists() ", e);
                } // try/catch
            } else {
                result = null;
            } // if
        } else {
            if (idx==0) {
                // TODO: This only works with exploded deployments
                File f = new File(filePathPrefix+url);
                if (log.isDebugEnabled()) {
                    log.debug("checkJspExists() f="+f.getAbsolutePath());
                } // if
                if (!(f.exists())) {
                    result = null;
                } // if
            } else {
                // meaning idx < 0 - no slash in url
                if (log.isWarnEnabled()) {
                    log.warn("checkJspExists() strange resource name "+url);
                } // if
                result = null;
            } // if
        } // if
        return result;
    } // checkJspExists()


    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(filePathPrefix)) {
            throw new Exception("path to lookup templates may not be null");
        } // if
    } // afterPropertiesSet()

} // AbstractInternalResourceTemplateResolver
