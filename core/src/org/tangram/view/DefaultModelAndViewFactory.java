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

import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;

/**
 * In the very heart this is just a collection of helper classes, but together with the groovy-implementation
 * it gets a powerful tool for populating contexts of velocity templates with useful stuff and methods.
 *
 */
public class DefaultModelAndViewFactory implements ModelAndViewFactory {

    @Override
    public Map<String, Object> createModel(Object bean, ServletRequest request, ServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(Constants.THIS, bean);
        model.put("request", request);
        model.put("response", response);
        return model;
    } // createModelAndView()


    @Override
    public ModelAndView createModelAndView(Map<String, Object> model, String view) {
        return model==null ? null : (new ModelAndView(view==null ? Constants.DEFAULT_VIEW : view, model));
    } // createModelAndView()


    @Override
    public ModelAndView createModelAndView(Object bean, String view, ServletRequest request, ServletResponse response) {
        Map<String, Object> model = createModel(bean, request, response);
        return createModelAndView(model, view);
    } // createModelAndView()


    @Override
    public ModelAndView createModelAndView(Object bean, ServletRequest request, ServletResponse response) {
        return createModelAndView(bean, null, request, response);
    } // createModelAndView()

} // DefaultModelAndViewFactory
