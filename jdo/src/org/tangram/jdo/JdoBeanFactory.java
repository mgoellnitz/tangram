/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.jdo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public interface JdoBeanFactory extends BeanFactory {

    /**
     * Get all classes related with models - also the abstract ones
     * 
     * @return
     */
    Collection<Class<? extends Content>> getAllClasses();


    /**
     * Get non abstract model classes
     * 
     * @return
     */
    Collection<Class<? extends Content>> getClasses();


    Map<Class<? extends Content>, List<Class<? extends Content>>> getImplementingClassesMap();


    /**
     * clears cache only for entries given type. Never dare to issue changes for abstract classes or interfaces!
     * 
     * only relevant for the attached listeners
     * 
     * @param cls
     * @throws Exception
     */
    void clearCacheFor(Class<? extends Content> cls);


    PersistenceManager getManager();

} // JdoBeanFactory
