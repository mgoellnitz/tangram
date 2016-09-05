/**
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.coma.annotation;

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
public @interface ContentBean {

    /**
     * Document type name the annotated bean should handle.
     *
     * @return Type name as shown in the bean factory configuration
     */
    String value() default "";


    /**
     * Activation flag if the annotated implementation should be considered as active an usable.
     *
     * @return true if the implementation should be used at runtime
     */
    boolean active() default true;

} // ContentBean
