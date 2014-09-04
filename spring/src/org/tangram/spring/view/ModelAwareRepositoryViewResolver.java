/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
import javax.inject.Inject;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.velocity.VelocityView;
import org.tangram.Constants;
import org.tangram.components.spring.SpringViewUtilities;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.view.AbstractRepositoryTemplateResolver;
import org.tangram.view.ViewUtilities;


public class ModelAwareRepositoryViewResolver extends AbstractRepositoryTemplateResolver<View> implements BeanListener, ModelAwareViewResolver {

    @Inject
    private ViewUtilities viewUtilities;

    private int order = Integer.MAX_VALUE;

    private ViewResolver delegate;


    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order = order;
    }


    public ViewResolver getDelegate() {
        return delegate;
    }


    public void setDelegate(ViewResolver delegate) {
        this.delegate = delegate;
    }


    protected View getNotFoundDummy() {
        return SpringViewUtilities.NOT_FOUND_DUMMY;
    } // getNotFoundDummy()


    @Override
    protected View resolveView(String path, Locale locale) throws Exception {
        View result = null;
        CodeResource template = resolveTemplate(path, locale);
        if (template!=null) {
            result = delegate.resolveViewName(template.getId(), locale);
            // Set default encoding to UTF-8 for any view
            if ((result!=null)&&(result instanceof VelocityView)) {
                VelocityView v = (VelocityView) result;
                v.addStaticAttribute(Constants.ATTRIBUTE_VIEW_UTILITIES, viewUtilities);
                v.setContentType(template.getMimeType()+";charset=UTF-8");
                v.setEncoding("UTF-8");
            } // if
        } // if
        return result;
    } // resolveView()


    @Override
    public View resolveView(String viewName, Map<String, Object> model, Locale locale) throws IOException {
        return resolveTemplate(viewName, model, locale);
    } // resolveView()

} // ModelAwareRepositoryViewResolver
