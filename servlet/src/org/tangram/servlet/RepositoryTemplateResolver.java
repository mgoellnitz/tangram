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

import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.view.AbstractRepositoryTemplateResolver;


public class RepositoryTemplateResolver extends AbstractRepositoryTemplateResolver<String> implements BeanListener {

    private final static Log log = LogFactory.getLog(JspTemplateResolver.class);

    private final static String NOT_FOUND_DUMMY = "ResourceNotFoundDummy";


    @Override
    protected String getNotFoundDummy() {
        return NOT_FOUND_DUMMY;
    } // getNotFoundDummy()


    @Override
    protected String resolveView(String path, Locale locale) throws Exception {
        String result = null;
        CodeResource template = resolveTemplate(path, locale);
        if (template!=null) {
            result = template.getId();
        } // if
        return result;
    } // resolveViewName()

} // RepositoryTemplateResolver
