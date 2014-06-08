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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.tangram.Constants;

/**
 * Factory implementation to create instances describing a view and its context.
 * The context in this case is describes as a multi value model taken from a hash map.
 *
 * Despite it's name it's not really the default for tangram anymore.
 *
 * In its very heart this is just a collection of helper methods, but together with the groovy-implementation
 * it gets a powerful tool for populating contexts of velocity templates with useful stuff and methods.
 *
 */
public class DefaultViewContextFactory implements ViewContextFactory {

    @Override
    public Map<String, Object> createModel(Object bean, ServletRequest request, ServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(Constants.THIS, bean);
        model.put("request", request);
        model.put("response", response);
        return model;
    } // createModelAndView()


    @Override
    public ViewContext createViewContext(Map<String, Object> model, String view) {
        return model==null ? null : (new SimpleViewContext(view==null ? Constants.DEFAULT_VIEW : view, model));
    } // createModelAndView()


    @Override
    public ViewContext createViewContext(Object bean, String view, ServletRequest request, ServletResponse response) {
        return createViewContext(createModel(bean, request, response), view);
    } // createModelAndView()


    @Override
    public ViewContext createViewContext(Object bean, ServletRequest request, ServletResponse response) {
        return createViewContext(bean, null, request, response);
    } // createModelAndView()

} // DefaultViewContextFactory
