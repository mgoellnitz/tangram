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
package org.tangram.view;

import java.util.Map;
import org.tangram.Constants;


public class SimpleViewContext implements ViewContext {

    private final String viewName;

    private final Map<String, Object> model;


    public SimpleViewContext(String viewName, Map<String, Object> model) {
        this.viewName = viewName==null ? Constants.DEFAULT_VIEW : viewName;
        this.model = model;
    } // SimpleViewContext()


    @Override
    public String getViewName() {
        return viewName;
    } // getViewName()


    @Override
    public Map<String, Object> getModel() {
        return model;
    } // getModel()


    @Override
    public String toString() {
        return viewName+" on "+model;
    } // toString()

} // SimpleViewContext
