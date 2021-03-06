/**
 *
 * Copyright 2012-2013 Martin Goellnitz
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
 * Method annotation to indicate that a method is callable via HTTP Requests.
 *
 * The URL can be specified as a regular expressions where the matching groups are passed over to the called
 * method as parameters. If the value is left empty the name of the method is taken.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LinkAction {

    /**
     * URI part after the context name and the servlet part of the URL to trigger the annotated method.
     *
     * @return regular expression pattern describing the URL format and groups to be used as parameters
     */
    String value() default "";

} // LinkAction
