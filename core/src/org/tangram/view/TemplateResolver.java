/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;


/**
 * Instances resolve templates according to a given view name and the type of one item from the model.
 */
public interface TemplateResolver<T extends Object> extends Comparable<TemplateResolver<T>> {

    /**
     *
     * @param viewName name of the view ("view method" - since this is an object oriented lookup)
     * @param model map to take one item from and use its type for the object oriented view method lookup
     * @param locale current local - ignored by all known implementations
     * @return view represented in a suitable type of the underlying implementation
     * @throws IOException IO related problems may occur on resolution of templates regardless of storage type for the templates
     */
    T resolveTemplate(String viewName, Map<String, Object> model, Locale locale) throws IOException;

} // TemplateResolver
