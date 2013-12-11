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
package org.tangram.servlet;

import java.io.IOException;
import java.util.Locale;
import javax.inject.Named;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.view.AbstractInternalResourceTemplateResolver;


@Named
public class JspTemplateResolver extends AbstractInternalResourceTemplateResolver<String> {

    private static final Log log = LogFactory.getLog(JspTemplateResolver.class);

    private final static String NOT_FOUND_DUMMY = "JspNotFoundDummy";


    @Override
    protected String getNotFoundDummy() {
        return NOT_FOUND_DUMMY;
    } // getNotFoundDummy()


    @Override
    protected String resolveView(String path, Locale locale) throws Exception {
        return path;
    } // resolveView()


    @Override
    protected String checkResourceExists(String url) {
        if (log.isInfoEnabled()) {
            log.info("checkResourceExists() url="+url);
        } // if
        return checkJspExists(getPrefix()+url+getSuffix());
    } // checkResourceExists()


    @Override
    protected String lookupView(String viewName, Locale locale, Object content, String key) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("lookupView() "+viewName+"#"+content);
        } // if
        return super.lookupView(viewName, locale, content, key);
    } // lookupView()


    @Override
    protected String checkView(String view, String packageName, String simpleName, String key, Locale locale) {
        if (log.isInfoEnabled()) {
            log.info("checkView() "+view+"#"+packageName+"_"+simpleName);
        } // if // if
        return super.checkView(view, packageName, simpleName, key, locale);
    } // checkView()

} // JspTemplateResolver
