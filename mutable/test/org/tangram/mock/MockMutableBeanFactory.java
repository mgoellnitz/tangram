/*
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
package org.tangram.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.mock.content.MockBeanFactory;
import org.tangram.mock.content.MockContent;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableCode;


/**
 * Mock utility class for mutable bean factory instances needed in tests.
 */
public class MockMutableBeanFactory extends MockBeanFactory implements MutableBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MockMutableBeanFactory.class);

    private final Object manager = new MockOrmManager();

    private final Collection<Class<? extends Content>> clearedClasses = new HashSet<>();


    @Override
    public Class<? extends Content> getBaseClass() {
        return MockContent.class;
    }


    @Override
    public void beginTransaction() {
        // empty mock
    }


    @Override
    public void commitTransaction() {
        // empty mock
    }


    @Override
    public void rollbackTransaction() {
        // empty mock
    }


    @Override
    public <T extends Content> T createBean(Class<T> cls) throws Exception {
        return cls.newInstance();
    }


    @Override
    public <T extends Content> boolean persistUncommitted(T bean) {
        return true;
    }


    @Override
    public <T extends Content> boolean persist(T bean) {
        return true;
    }


    @Override
    public <T extends Content> boolean delete(T bean) {
        // TODO: Really delete this
        return true;
    }


    @Override
    public Collection<Class<? extends Content>> getAllClasses() {
        return allClasses;
    }


    protected List<Class<? extends Content>> getImplementingClassesForModelClass(Class<? extends Content> baseClass) {
        List<Class<? extends Content>> result = new ArrayList<>();

        for (Class<? extends Content> c : getClasses()) {
            if (baseClass.isAssignableFrom(c)) {
                result.add(c);
            } // if
        } // for

        return result;
    } // getImplementingClassesForModelClass()


    /**
     * just to support JSP's weak calling of methods. It does not allow any parameters.
     *
     * @return map to map abstract classes to all non-abstract classes implementing/extending them
     */
    @Override
    public Map<Class<? extends Content>, List<Class<? extends Content>>> getImplementingClassesMap() {
        Map<Class<? extends Content>, List<Class<? extends Content>>> implementingClassesMap = null;
        synchronized (this) {
            implementingClassesMap = new HashMap<>();

            // Add the very basic root classes directly here - they won't get auto detected otherwise
            implementingClassesMap.put(getBaseClass(), getImplementingClassesForModelClass(getBaseClass()));
            for (Class<? extends Content> c : getAllClasses()) {
                implementingClassesMap.put(c, getImplementingClassesForModelClass(c));
            } // for
            if (!implementingClassesMap.containsKey(CodeResource.class)) {
                implementingClassesMap.put(CodeResource.class, getImplementingClassesForModelClass(CodeResource.class));
            } // if
            if (!implementingClassesMap.containsKey(MutableCode.class)) {
                implementingClassesMap.put(MutableCode.class, getImplementingClassesForModelClass(MutableCode.class));
            } // if
            // implementingClassesMap.put(Content.class, getImplementingClassesForModelClass(Content.class));
            LOG.info("getImplementingClassesMap() {}", implementingClassesMap);
        } // synchronized
        return implementingClassesMap;
    } // getImplementingClassMap()


    @Override
    public <T extends Content> List<Class<T>> getImplementingClasses(Class<T> baseClass) {
        List<Class<T>> result = new ArrayList<>();
        for (Class<? extends Content> c : getImplementingClassesMap().get(baseClass)) {
            result.add((Class<T>) c);
        } // for
        return result;
    } // getImplementingClasses()


    @Override
    public String getFilterQuery(Class<?> cls, String filterProperty, String filterValues) {
        return "<mock mutable bean factory does not do query expressions>";
    }


    @Override
    public void clearCacheFor(Class<? extends Content> cls) {
        clearedClasses.add(cls);
    } // clearCacheFor()


    public Collection<Class<? extends Content>> getClearedCacheClasses() {
        Collection<Class<? extends Content>> result = clearedClasses;
        clearedClasses.clear();
        return result;
    } // getClearedCacheClasses()


    @Override
    public Object getManager() {
        return manager;
    } // getManager()

} // MockMutableBeanFactory
