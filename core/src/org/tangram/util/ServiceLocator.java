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
package org.tangram.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;


/**
 * Utility class to obtain instances of types service by standard Java means.
 */
public class ServiceLocator {

    /**
     * Get one instance of given type from the Java ServiceLoader.
     *
     * @param <T>
     * @param type
     * @return one instance assignable to type
     */
    public static <T extends Object> T get(Class<T> type) {
        Iterator<T> iterator = ServiceLoader.load(type).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    } // get()


    /**
     * Get all instances of a give type from the Java ServiceLoader.
     *
     * @param <T>
     * @param type
     * @return all instances assignable to type
     */
    public static <T extends Object> Collection<T> getAll(Class<T> type) {
        Collection<T> result = new HashSet<T>();
        for (T t : ServiceLoader.load(type)) {
            result.add(t);
        } // for
        return result;
    } // getAll()

} // ServiceLocator
