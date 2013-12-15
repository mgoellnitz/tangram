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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.monitor.Statistics;


/**
 * Common stuff for all bean factories dealing with mutable content.
 */
public abstract class AbstractMutableBeanFactory extends AbstractBeanFactory implements MutableBeanFactory {

    protected static final String QUERY_CACHE_KEY = "tangram.query.cache";

    private static final Log log = LogFactory.getLog(AbstractMutableBeanFactory.class);

    @Inject
    protected Statistics statistics;

    @Inject
    protected PersistentRestartCache startupCache;

    private Map<Class<? extends Content>, List<BeanListener>> attachedListeners = new HashMap<Class<? extends Content>, List<BeanListener>>();


    protected Map<Class<? extends Content>, List<BeanListener>> getListeners() {
        return attachedListeners;
    } // getListeners() {


    @Override
    public <T extends MutableContent> boolean persist(T bean) {
        final boolean result = persistUncommitted(bean);
        if (result) {
            commitTransaction();
        } // if
        return result;
    } // persistUncommitted()


    /**
     * Cache key for the persistent cache to store all class names.
     * The stored values are taken from the class path package scan and
     * asumed to be persistent over re-starts of the applicaion.
     *
     * @return String to be used as a cache key
     */
    protected String getClassNamesCacheKey() {
        return "tangram-class-names";
    } // getClassNamesCacheKey()


    /**
     * Gets the class for a given type name.
     * Be aware of different class loaders - like with groovy based classes.
     *
     * @param className fully qualified name of the class
     */
    protected Class<? extends MutableContent> getClassForName(String className) {
        Class<? extends MutableContent> result = null;
        for (Class<? extends MutableContent> c : getClasses()) {
            if (c.getName().equals(className)) {
                result = c;
            } // if
        } // for
        if (result==null) {
            try {
                result = (Class<? extends MutableContent>) Class.forName(className);
            } catch (ClassNotFoundException cnfe) {
                //
            } // try/catch
        } // if
        return result;
    } // getClassForName()


    /**
     * Get class name from query cache key.
     * Keys are supposed to be in the form of <classname>:<query>
     * @param <T>
     * @param key
     * @return Class for the given key or null if the key does not map to any class
     */
    @SuppressWarnings("unchecked")
    protected <T extends Content> Class<T> getKeyClass(String key) {
        String className = key.split(":")[0];
        if (log.isDebugEnabled()) {
            log.debug("getKeyClass() "+className);
        } // if
        return (Class<T>) getClassForName(className);
    } // getKeyClass()


    /**
     * attach a listener for any changes dealing with classes of the given type.
     *
     * @param cls
     * @param listener
     */
    @Override
    public void addListener(Class<? extends Content> cls, BeanListener listener) {
        synchronized (attachedListeners) {
            List<BeanListener> listeners = attachedListeners.get(cls);
            if (listeners==null) {
                listeners = new ArrayList<BeanListener>();
                attachedListeners.put(cls, listeners);
            } // if
            listeners.add(listener);
        } // synchronized
        if (log.isInfoEnabled()) {
            log.info("addListener() "+cls.getSimpleName()+": "+attachedListeners.get(cls).size());
        } // if
    } // addListener()


    /**
     * Get the classes implementing a given baseClass.
     *
     * @param <T>
     * @param baseClass
     * @return list of non-abstract classes that can be assigned to the given base class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> List<Class<T>> getImplementingClasses(Class<T> baseClass) {
        List<Class<T>> result = new ArrayList<Class<T>>();

        for (Class<? extends MutableContent> c : getClasses()) {
            if (baseClass.isAssignableFrom(c)) {
                result.add((Class<T>) c);
            } // if
        } // for

        return result;
    } // getImplementingClasses()


    protected List<Class<? extends MutableContent>> getImplementingClassesForModelClass(Class<? extends Content> baseClass) {
        List<Class<? extends MutableContent>> result = new ArrayList<Class<? extends MutableContent>>();

        for (Class<? extends MutableContent> c : getClasses()) {
            if (baseClass.isAssignableFrom(c)) {
                result.add(c);
            } // if
        } // for

        return result;
    } // getImplementingClassesForModelClass()

} // AbstractMutableBeanFactory
