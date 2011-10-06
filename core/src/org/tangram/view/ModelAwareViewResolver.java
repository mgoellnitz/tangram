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
package org.tangram.view;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;

public interface ModelAwareViewResolver extends Ordered {

    View resolveViewName(String viewName, Map<String, Object> model, Locale locale) throws IOException;

} // ModelAwareViewResolver
