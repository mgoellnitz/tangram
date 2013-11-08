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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.jpa;

import java.util.Collection;
import javax.persistence.EntityManager;
import org.tangram.content.Content;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableContent;

public interface JpaBeanFactory extends MutableBeanFactory {

    /**
     * Get all classes related with models - also the abstract ones
     *
     * @return
     */
    Collection<Class<? extends MutableContent>> getAllClasses();


    /**
     * clears caches only for entries of the given type.
     *
     * Never dare to issue changes for abstract classes or interfaces!
     * only relevant for the attached listeners
     *
     * @param cls
     */
    void clearCacheFor(Class<? extends Content> cls);


    EntityManager getManager();


    /**
     * set a list of classes to be used as model classes in addition to the statically scanned ones.
     *
     * @param classes list of available classes
     */
    void setAdditionalClasses(Collection<Class<? extends MutableContent>> classes);

} // JpaBeanFactory
