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
package org.tangram.components.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.tangram.spring.view.ModelAwareViewResolver;
import org.tangram.spring.view.ViewHandler;


@Named("viewHandler")
public class TangramViewHandler implements ViewHandler, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(TangramViewHandler.class);

    private ApplicationContext applicationContext;

    /** List of ViewResolvers used by this servlet */
    private List<ModelAwareViewResolver> modelAwareViewResolvers;

    private boolean detectAllModelAwareViewResolvers = true;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    public boolean isDetectAllModelAwareViewResolvers() {
        return detectAllModelAwareViewResolvers;
    }


    public void setDetectAllModelAwareViewResolvers(boolean detectAllModelAwareViewResolvers) {
        this.detectAllModelAwareViewResolvers = detectAllModelAwareViewResolvers;
    }


    /**
     * Initialize the ViewResolvers used by this class.
     * <p>
     * If no ViewResolver beans are defined in the BeanFactory for this namespace, we default to
     * InternalResourceViewResolver.
     */
    private void initViewResolvers(ApplicationContext context) {
        this.modelAwareViewResolvers = null;

        if (this.detectAllModelAwareViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ModelAwareViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
                    ModelAwareViewResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.modelAwareViewResolvers = new ArrayList<ModelAwareViewResolver>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                OrderComparator.sort(this.modelAwareViewResolvers);
            } // if
        } else {
            try {
                ModelAwareViewResolver vr = context.getBean(DispatcherServlet.VIEW_RESOLVER_BEAN_NAME, ModelAwareViewResolver.class);
                this.modelAwareViewResolvers = Collections.singletonList(vr);
            } catch (NoSuchBeanDefinitionException e) {
                // Ignore, we'll add a default ViewResolver later.
                LOG.warn("initViewResolvers()", e);
            } // try/catch
        } // if
    } // initViewResolvers()


    @Override
    public View resolveView(String viewName, Map<String, Object> model, Locale locale, ServletRequest request) throws IOException {
        View result = null;

        for (ModelAwareViewResolver viewResolver : this.modelAwareViewResolvers) {
            View view = viewResolver.resolveView(viewName, model, locale);
            if (view!=null) {
                result = view;
                break;
            } // if
        } // for

        return result;
    } // resolveViewName()


    @PostConstruct
    public void afterPropertiesSet() {
        initViewResolvers(applicationContext);
    } // afterPropertiesSet()

} // TangramViewHandler
