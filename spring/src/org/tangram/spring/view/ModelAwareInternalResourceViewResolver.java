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
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.tangram.components.spring.SpringViewUtilities;
import org.tangram.view.AbstractInternalResourceTemplateResolver;


/**
 * This class represents the necessary adapter between generic template lookup and the spring view layer
 * for internal resources - namely JSPs - used as templates.
 *
 * The spring order mechanism is used for the priority of ViewResolvers and a view resolver delegate later
 * does the real work.
 */
public class ModelAwareInternalResourceViewResolver extends AbstractInternalResourceTemplateResolver<View> implements ServletContextAware, ModelAwareViewResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ModelAwareInternalResourceViewResolver.class);

    private int order = Integer.MAX_VALUE;

    private UrlBasedViewResolver delegate;


    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order = order;
    }


    public UrlBasedViewResolver getDelegate() {
        return delegate;
    }


    public void setDelegate(UrlBasedViewResolver delegate) {
        this.delegate = delegate;
    }


    @Override
    protected View getNotFoundDummy() {
        return SpringViewUtilities.NOT_FOUND_DUMMY;
    } // getNotFoundDummy()


    @Override
    protected View checkResourceExists(View result) {
        String url = ((AbstractUrlBasedView) result).getUrl();
        return (checkJspExists(url)!=null) ? result : null;
    } // checkResourceExists()


    @Override
    protected View resolveView(String path, Locale locale) throws Exception {
        return delegate.resolveViewName(path, locale);
    } // resolverViewName()


    @Override
    public View resolveView(String viewName, Map<String, Object> model, Locale locale) throws IOException {
        return resolveTemplate(viewName, model, locale);
    } // resolveView()


    @PostConstruct
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(delegate, "delegate is null");
        delegate.setPrefix(getPrefix());
        delegate.setSuffix(getSuffix());
    } // afterPropertiesSet()

} // ModelAwareInternalResourceViewResolver
