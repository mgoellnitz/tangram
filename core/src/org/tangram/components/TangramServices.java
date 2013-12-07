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
package org.tangram.components;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Named;
import org.tangram.content.BeanFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TemplateResolver;
import org.tangram.view.ViewContextFactory;


/**
 * bean providing access to common services by means of static accessors.
 *
 * Meant for parts of the code which don't have other reasonable access to the configured beans to
 * have all of these accesses in one place.
 */
@Named
public class TangramServices {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static BeanFactory beanFactory = null;

    private static LinkFactoryAggregator linkFactoryAggregator = null;

    private static ViewContextFactory viewContextFactory = null;

    private static PropertyConverter propertyConverter = null;

    @SuppressWarnings("rawtypes")
    private static Set<TemplateResolver> resolvers = new HashSet<TemplateResolver>();

    private static DateFormat httpHeaderDateFormat = null;

    private static Map<String, Object> viewSettings = null;


    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }


    @Inject
    public void setBeanFactory(BeanFactory beanFactory) {
        TangramServices.beanFactory = beanFactory;
    }


    public static LinkFactoryAggregator getLinkFactoryAggregator() {
        return linkFactoryAggregator;
    }


    @Inject
    public void setLinkFactoryAggregator(LinkFactoryAggregator linkFactoryAggregator) {
        TangramServices.linkFactoryAggregator = linkFactoryAggregator;
    }


    public static ViewContextFactory getViewContextFactory() {
        return viewContextFactory;
    }


    @Inject
    public void setViewContextFactory(ViewContextFactory viewContextFactory) {
        TangramServices.viewContextFactory = viewContextFactory;
    }


    public static PropertyConverter getPropertyConverter() {
        return propertyConverter;
    }


    @Inject
    public void setPropertyConverter(PropertyConverter propertyConverter) {
        TangramServices.propertyConverter = propertyConverter;
    }


    /**
     * This is "rawtypes" because of google guice's weak injection mechanism.
     */
    @SuppressWarnings("rawtypes")
    public static Set<TemplateResolver> getResolvers() {
        return resolvers;
    }


    @Inject
    @SuppressWarnings("rawtypes")
    public void setResolvers(Set<TemplateResolver> resolvers) {
        TangramServices.resolvers = resolvers;
    }


    public static DateFormat getHttpHeaderDateFormat() {
        return httpHeaderDateFormat;
    }


    @Inject
    public void setHttpHeaderDateFormat(@Named("httpHeaderDateFormat") DateFormat httpHeaderDateFormat) {
        httpHeaderDateFormat.setTimeZone(GMT);
        TangramServices.httpHeaderDateFormat = httpHeaderDateFormat;
    }


    /**
     * This is "rawtypes" because of google guice's weak injection mechanism.
     */
    public static Map<String, Object> getViewSettings() {
        return viewSettings;
    }


    @Inject
    public void setViewSettings(@Named("viewSettings") Map<String, Object> viewSettings) {
        if (viewSettings.containsKey("viewSettings")) {
            viewSettings = (Map<String, Object>)(viewSettings.get("viewSettings"));
        } // if
        TangramServices.viewSettings = viewSettings;
    }

} // TangramServices
