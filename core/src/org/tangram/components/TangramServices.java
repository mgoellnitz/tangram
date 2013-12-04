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
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.tangram.content.BeanFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.PropertyConverter;
import org.tangram.view.ViewContextFactory;

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

} // TangramServices
