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
package org.tangram.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.tangram.content.BeanFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TemplateResolver;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


/**
 * bean providing access to common services by means of static accessors.
 *
 * Meant for parts of the code which don't have other reasonable access to the configured beans to
 * have all of these accesses in one place.
 */
@Named
public class TangramServices {

    private static BeanFactory beanFactory = null;

    private static LinkFactoryAggregator linkFactoryAggregator = null;

    private static ViewContextFactory viewContextFactory = null;

    private static PropertyConverter propertyConverter = null;

    private static CodeResourceCache codeResourceCache = null;

    private static ViewUtilities viewUtilities = null;

    @SuppressWarnings("rawtypes")
    private static List<TemplateResolver> resolvers = new ArrayList<TemplateResolver>();

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


    public static CodeResourceCache getCodeResourceCache() {
        return codeResourceCache;
    }


    @Inject
    public void setCodeResourceCache(CodeResourceCache codeResourceCache) {
        TangramServices.codeResourceCache = codeResourceCache;
    }


    public static ViewUtilities getViewUtilities() {
        return viewUtilities;
    }


    @Inject
    public void setViewUtilities(ViewUtilities viewUtilities) {
        TangramServices.viewUtilities = viewUtilities;
    }


    /**
     * This is "rawtypes" because of google guice's weak injection mechanism.
     */
    @SuppressWarnings("rawtypes")
    public static List<TemplateResolver> getResolvers() {
        return resolvers;
    }


    @Inject
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setResolvers(Set<TemplateResolver> resolvers) {
        TangramServices.resolvers = new ArrayList<>(resolvers);
        Collections.sort(TangramServices.resolvers);
    } // setResolvers()


    /**
     * This is "rawtypes" because of google guice's weak injection mechanism.
     */
    public static Map<String, Object> getViewSettings() {
        return viewSettings;
    }


    @Inject
    @SuppressWarnings("unchecked")
    public void setViewSettings(@Named("viewSettings") Map<String, Object> viewSettings) {
        if (viewSettings.containsKey("viewSettings")) {
            viewSettings = (Map<String, Object>) (viewSettings.get("viewSettings"));
        } // if
        TangramServices.viewSettings = viewSettings;
    } // setViewSettings()

} // TangramServices
