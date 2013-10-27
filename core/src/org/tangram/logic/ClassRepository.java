/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.logic;

import java.util.Map;
import java.util.Set;
import org.tangram.content.BeanListener;

public interface ClassRepository {

    /**
     * Get a set of names of classes available in this repository.
     * @return class names
     */
    public Set<String> get();


    /**
     * Get classes from this repository being subclasses of a given class.
     * @param <T>
     * @param cls base class for the result
     * @return map code resource annotation to class instance
     */
    public <T extends Object> Map<String, Class<T>> get(Class<? extends T> cls);


    /**
     * Get a class from this repository.
     * @param className fully qualified class name
     * @return class instance
     */
    public Class<? extends Object> get(String className);


    /**
     * Get a class from this repository.
     * @param className fully qualified class name
     * @return class instance
     */
    public byte[] getBytes(String className);


    /**
     * Get all compilation errors from last update of this repository.
     * Annotations are mapped to their respective textual compilation error.
     * @return map annotation to error text
     */
    public Map<String, String> getCompilationErrors();


    /**
     * Attach a consuming listener to this repository being notified of updates.
     * @param listener
     */
    public void addListener(BeanListener listener);

} // ClassRepository
