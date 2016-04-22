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
package org.tangram.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class marker indicating that the given class contains @LinkAction methods.
 * If implementing classes don't need any additional functions from the LinkHandler interface but just
 * contain @LinkAction methods with a path specified, they can be marked with this annotation to get
 * automatically recognized by the MetaController or MetaServlet.
 * Static implementations not taken from the code resource cache must manually register themselves
 * with a link handler registry instance.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LinkHandler {

} // LinkHandler
