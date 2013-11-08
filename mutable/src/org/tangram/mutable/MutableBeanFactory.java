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
package org.tangram.mutable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;


public interface MutableBeanFactory extends BeanFactory {

    /**
     *
     * Get a bean of a given type with a given id. You MUST call persist() after using this method! The resulting bean
     * must adhere to both conditions: id and type.
     *
     * @param <T>
     * @param cls
     * @param id
     * @return
     */
    <T extends MutableContent> T getBeanForUpdate(Class<T> cls, String id);


    /**
     *
     * Get bean with a given id. You MUST call persist() after using this method!
     *
     * @param id
     * @return
     */
    MutableContent getBeanForUpdate(String id);


    /**
     * Create a new bean of a given type. The beans hast to be persisted in a subsequent step! The call of persist() ist
     * mandatory after using this call.
     *
     * @param cls class to reate a persistable instance for
     * @return newly created instance
     */
    <T extends MutableContent> T createBean(Class<T> cls) throws Exception;


    /**
     * Persist a given bean.
     *
     * @param <T>
     * @param bean
     * @return true if persisting could be completed successfully
     */
    <T extends MutableContent> boolean persist(T bean);


    /**
     * return a collection of content classes available for mutable contents.
     * No abstract classes will be in the list.
     *
     * @return list of classes
     */
    public Collection<Class<? extends MutableContent>> getClasses();


    /**
     * Return a map mapping abstract classes or interfaces to non-abstract classes implementing this interface.
     */
    Map<Class<? extends Content>, List<Class<? extends MutableContent>>> getImplementingClassesMap();

} // MutableBeanFactory
