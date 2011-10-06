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
package org.tangram.jdo;

import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public interface JdoBeanFactory extends BeanFactory {

    String postprocessPlainId(Object id);


    Collection<Class<? extends Content>> getClasses();


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
