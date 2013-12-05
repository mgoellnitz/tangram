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
package org.tangram.spring.view;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;
import org.tangram.view.AbstractTemplateResolver;


public abstract class AbstractModelAwareViewResolver extends AbstractTemplateResolver<View> implements ModelAwareViewResolver {

    private static Log log = LogFactory.getLog(AbstractModelAwareViewResolver.class);

    private final static View NOT_FOUND_DUMMY = new View() {

        @Override
        public String getContentType() {
            return null;
        }


        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        }

    };

    private int order = Integer.MAX_VALUE;


    @Override
    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order = order;
    }


    protected View getNotFoundDummy() {
        return NOT_FOUND_DUMMY;
    } // getNotFoundDummy()


    @Override
    public View resolveView(String viewName, Map<String, Object> model, Locale locale) throws IOException {
        return resolveTemplate(viewName, model, locale);
    } // resolveView()


    protected AbstractModelAwareViewResolver(boolean suppressBrackets, String packageSeparator) {
        super(suppressBrackets, packageSeparator);
    } // AbstractModelAwareViewResolver()

} // AbstractModelAwareViewResolver
