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
package org.tangram.content;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * Cache for code resources taken from the repository represented by the bean factory.
 *
 * The cache is filled with transient implementations of the code resource interfaces resembling contents taken
 * from the bean factory by query. The persistent restart cache is used to support much faster restart since
 * the values in this cache are not supposed to change between restarts.
 *
 */
public interface CodeResourceCache extends BeanListener {

    /**
     * return a pseudo file modification time.
     *
     * It takes the time of last code resource scan to be used by e.g. the velocity parser again for caching purposes
     *
     * @return last time of scanning for code resources
     */
    public long getLastUpdate();


    /**
     * Obtain the code resource for a given mimetype and annotation.
     *
     * @param mimeType mimetype for the cache part to search in
     * @param annotation annotation of the code resource to be returned
     * @return a code resource if found or null otherwise
     */
    CodeResource get(String mimeType, String annotation);


    /**
     * Get a code resource for an id from the code resource cache.
     *
     * Quite similar to a getBean() in the bean factory
     *
     * @param id content id of the code resource instance to obtain
     * @return code resource for the id if found or null otherwise
     */
    CodeResource get(String id);


    /**
     * Return a list of available mimetype in the cache.
     *
     * @return set of mimetypes - may be empty but not null
     */
    Set<String> getTypes();


    /**
     * Return all codes in the cache. unordered.
     *
     * @return collection of code resources - may be empty but not null
     */
    Collection<CodeResource> getCodes();


    /**
     * Get all available annotations for a given mimetype.
     *
     * Since thois anotations are used like filenames this is something like a listing for the given mimetype
     *
     * @param mimeType mimetype of the code resource instances to collect annotation string from
     * @return list of annotations for the given mimetype - maybe empty but nut null
     */
    Collection<String> getAnnotations(String mimeType);


    /**
     * Return the cache portion for a given mimetype.
     *
     * @param mimeType mime type of the code resources to include into result mapping
     * @return annotation to code resource map for the given mimetype - maybe empty but not null
     */
    Map<String, CodeResource> getTypeCache(String mimeType);


    /**
     * Add listener for cache changes.
     *
     * @param listener listener instance to be notified about cache changes.
     */
    void addListener(BeanListener listener);

} // CodeResourceCache
