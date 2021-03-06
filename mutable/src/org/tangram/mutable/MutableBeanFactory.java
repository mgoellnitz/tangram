/**
 *
 * Copyright 2013-2017 Martin Goellnitz
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
import org.tangram.content.ChangeListener;
import org.tangram.content.Content;


public interface MutableBeanFactory<M extends Object, Q extends Object> extends BeanFactory<Q> {

    /**
     * Returns the root class of all content classes handled by the implementing instance - may be null;
     *
     * @return base class for this bean factory implementing the content interface
     */
    Class<? extends Content> getBaseClass();


    /**
     * Starts a transaction which must subsequently be committed or rolled back.
     */
    void beginTransaction();


    /**
     * Commits a transaction previously opened with beginTransaction()
     */
    void commitTransaction();


    /**
     * Undo the changes initiated by all the commands issued after the last beginTransaction call.
     */
    void rollbackTransaction();


    /**
     * Create a new bean of a given type.
     *
     * The beans hast to be persisted in a subsequent step! The call of persist() is mandatory after using this call.
     *
     * @param cls class to create a persistable instance for
     * @param <T> Type constraint for the above class
     * @return newly created instance
     * @throws Exception mostly IO and DB storage related exceptions
     */
    <T extends Content> T createBean(Class<T> cls) throws Exception;


    /**
     * Persist a given bean and don't close the open transaction.
     *
     * @param <T> Type constraint for the given bean
     * @param bean bean to persist without committing the running transaction
     * @return true if persisting could be completed successfully
     */
    <T extends Content> boolean persistUncommitted(T bean);


    /**
     * Persist a given bean.
     *
     * @param <T> Type constraint for the given bean
     * @param bean bean to persist
     * @return true if persisting could be completed successfully
     */
    <T extends Content> boolean persist(T bean);


    /**
     * Delete a given bean from persistence storage.
     * This method uses a transaction and closes it or does a roll back.
     *
     * @param <T> Type constraint for the given bean
     * @param bean bean to delete from persistent storage
     * @return true if deleting could be completed successfully
     */
    <T extends Content> boolean delete(T bean);


    /**
     * return a collection of all content classes available for mutable contents.
     * No abstract classes will be in the returned collection.
     *
     * @return collection of content classes
     */
    Collection<Class<? extends Content>> getClasses();


    /**
     * return a collection of all classes available related with content for mutable bean instances.
     * Also abstract classes will be in the returned collection.
     *
     * @return collection of content classes
     */
    Collection<Class<? extends Content>> getAllClasses();


    /**
     * Return a map mapping abstract classes to inheriting non-abstract classes.
     *
     * This map is used in the editor to map abstract classes to inheriting non-abstract classes when it
     * comes to ask the user which non-abstract class to instanciate.
     *
     * @return mappping from class specifier to non-abstract implementing classes available
     */
    Map<Class<? extends Content>, List<Class<? extends Content>>> getImplementingClassesMap();


    /**
     * Return a list of assignable non-abstract classes for a given type.
     *
     * @param <T> abstract class or interfaces
     * @param c class instance for this type
     * @return list of non abstract classes assignable to the given type
     */
    <T extends Content> List<Class<T>> getImplementingClasses(Class<T> c);


    /**
     * Create a suitable query clause for filter expressions.
     * Form a query string from a property name a a space separated list of possible values
     * which should all be met as a substring of the property.
     *
     * @param cls class this resulting query is about
     * @param <F> content type constraint
     * @param filterProperty of the property to filter for
     * @param filterValues space separated list of values which should all be met
     * @return implementation specific query expression
     */
    <F extends Content> String getFilterQuery(Class<F> cls, String filterProperty, String filterValues);


    /**
     * clear caches for instances depending on the given type.
     *
     * Never dare to issue changes for abstract classes or interfaces!
     * (You cannot have instances of those types anyway)
     * The call is only relevant for the attached listeners.
     *
     * @param cls class to trigger listeners for
     */
    void clearCacheFor(Class<? extends Content> cls);


    /**
     * Attach a listener to be notified for each change.
     *
     * Don't use this if you are not interested in the sequence of changes but typed changes.
     *
     * @param listener listener to be notified about changes
     */
    void addListener(ChangeListener listener);


    /**
     * Get managing instance of the underlying implementation.
     *
     * Unspecified type instance of the implementing backend.
     * This is e.g. a JDO PersistenceManager instance or a JPA
     * EntitiyManager instance.
     *
     * @return implementation specific manager instance
     */
    M getManager();

} // MutableBeanFactory
