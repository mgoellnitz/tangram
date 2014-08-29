/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
import java.net.URL;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generic superclass for all implementations using internal resources - namely JSP file - as templates.
 *
 * @param <T> Type for the view technology in use e.g. Strings for filenames
 */
public abstract class AbstractInternalResourceTemplateResolver<T extends Object> extends AbstractTemplateResolver<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInternalResourceTemplateResolver.class);

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


    @Inject
    public void setServletContext(ServletContext servletContext) {
        filePathPrefix = servletContext.getRealPath("");
    } // setServletContext()


    protected String checkJspExists(String url) {
        if (LOG.isInfoEnabled()) {
            LOG.info("checkJspExists("+url+")");
        } // if
        String resourcePrefix = "/META-INF/resources";
        final URL resource = getClass().getResource(resourcePrefix+url);
        if (resource==null) {
            File f = new File(filePathPrefix+url);
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkJspExists() f="+f.getAbsolutePath());
            } // if
            if (!(f.exists())) {
                url = null;
            } // if
        } // if
        return url;
    } // checkJspExists()


    @PostConstruct
    public void afterPropertiesSet() {
        if (StringUtils.isBlank(filePathPrefix)) {
            LOG.error("afterPropertiesSet() path to lookup templates may not be null");
        } // if
    } // afterPropertiesSet()


    @Override
    public int compareTo(TemplateResolver<T> o) {
        return 1;
    } // compareTo()

} // AbstractInternalResourceTemplateResolver
