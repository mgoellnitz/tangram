/**
 *
 * Copyright 2014 Martin Goellnitz
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
package org.tangram.util;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *  Generic String utility functions not found elsewhere in the libraries we are using.
 */
public class StringUtil {

    /**
     * Split a String - say servlet init parameter - at each ',' and trim the result.
     *
     * Actually very generic utility fuction. Trimming is used so that line breaks and spaces can be used
     * to format the input in config files.
     *
     * @param parameter
     * @return set of string taken from the input parameter
     */
    public static Set<String> stringSetFromParameterString(String parameter) {
        Set<String> result = new HashSet<String>();
        if (StringUtils.isNotBlank(parameter)) {
            String[] parts = parameter.split(",");
            for (String part : parts) {
                result.add(part.trim());
            } // for
        } // if
        return result;
    } // stringSetFromParameterString()

} // StringUtil
