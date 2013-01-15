/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.view.link;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.tangram.content.BeanFactory;
import org.tangram.view.TargetDescriptor;

/**
 * 
 * In the class the link hanlder gets extended by the URL parsing elements
 * 
 * Implementing classes might need the bean factory and the default controller (to register which views are handled by
 * this implementation)
 * 
 */
public interface LinkScheme extends LinkHandler {

    /**
     * underlying implementations might want to use this
     * 
     * @param beanFactory
     */
    void setBeanFactory(BeanFactory beanFactory);


    /**
     * return all view's names the underlying implementation wants to handle itself
     * 
     * @return collection of view names
     */
    Collection<String> getCustomViews();


    /**
     * return the id of the object to be show, null otherwise
     * 
     * @param url
     * @param response
     *            for error handling
     * @return
     */
    TargetDescriptor parseLink(String url, HttpServletResponse response);

} // LinkScheme
