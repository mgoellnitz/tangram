/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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
package org.tangram.content;

import java.util.Comparator;
import java.util.List;


public interface BeanFactory<Q extends Object> {

    /**
     * Get a bean of a given type with a given id.
     *
     * The resulting bean must adhere to both conditions: id and type.
     *
     * @param cls class of the bean to obtain
     * @param <T> type constraint for the above class
     * @param id id of the bean to obtain
     * @return bean with the given ID and type or null otherwise
     */
    <T extends Content> T getBean(Class<T> cls, String id);


    /**
     * Get bean with a given id.
     *
     * @param id id of the bean to return
     * @return bean with the given ID or null otherwise
     */
    Content getBean(String id);


    /**
     * Create a underlying storage system specific query object.
     *
     * @param cls class of the elements the query should return
     * @param expression optional expression which might be ignored by he underlying system
     * @return new query instance
     */
    Q createQuery(Class<? extends Content> cls, String expression);


    /**
     * List beans from the repository.
     *
     * @param cls beans class to query for
     * @param <T> type constraint for the above class
     * @param optionalQuery query string specific to the underlying storage system - may be null
     * @param orderProperty name of a attribute of the bean to be used for ascending ordering
     * @param ascending sort ascending or not (descending)
     * @return List of beans adhering the conditions - maybe empty but not null
     */
    <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery, String orderProperty, Boolean ascending);


    /**
     * List beans from the repository.
     *
     * @param cls class of the elements of the result list
     * @param <T> Type constraint for the elements of the result list
     * @param optionalQuery query string specific to the underlying storage system - may be null
     * @return List of beans adhering the conditions - maybe empty but not null
     */
    <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery);


    /**
     * List beans from the repository.
     *
     * @param cls class of the elements of the result list
     * @param <T> Type constraint for the elements of the result list
     * @param optionalQuery query string specific to the underlying storage system - may be null
     * @param order comparator to be used for the sorting of the result list
     * @return List of beans adhering the conditions - maybe empty but not null
     */
    <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery, Comparator<T> order);


    /**
     * List beans from the repository.
     *
     * @param cls class of the elements of the result list
     * @param <T> Type constraint for the elements of the result list
     * @return List of all beans of the given class - maybe empty but not null
     */
    <T extends Content> List<T> listBeans(Class<T> cls);


    /**
     * List beans from the repository by means of the underlying storage solution.
     *
     * @param query implementation specific query instance
     * @param <T> Type constraint for the elements of the result list
     * @return List of all beans of the given class - maybe empty but not null
     */
    <T extends Content> List<T> listBeans(Q query);


    /**
     * List beans from the repository of an exact type. Does not take sublcasses into account.
     *
     * @param cls Type requirement to be met.
     * @param <T> Type constraint for the elements of the result list
     * @param optionalQuery query according to the underlying storage layer - may be null
     * @param orderProperty the resulting list may be ordered according to this property
     * @param ascending sort following orderProperty ascending if true or descending if false - may be null
     * @return list of content elements of exactly the given type T.
     */
    <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String optionalQuery, String orderProperty, Boolean ascending);


    /**
     * List beans from the repository of an exact type. Does not take sublcasses into account.
     *
     * @param cls Type requirement to be met.
     * @param <T> Type constraint for the elements of the result list
     * @return list of content elements of exactly the given type T.
     */
    <T extends Content> List<T> listBeansOfExactClass(Class<T> cls);


    /**
     * Add a listener to be triggered if changes to beans of given type occur.
     *
     * @param cls type to check modified resources against
     * @param listener listener instance to be called
     */
    void addListener(Class<? extends Content> cls, BeanListener listener);

} // BeanFactory
