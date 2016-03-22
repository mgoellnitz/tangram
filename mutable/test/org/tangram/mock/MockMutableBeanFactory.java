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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.tangram.content.Content;
import org.tangram.content.test.MockBeanFactory;
import org.tangram.mutable.MutableBeanFactory;


/**
 * Mock utility class for mutable bean factory instances needed in tests.
 */
public class MockMutableBeanFactory extends MockBeanFactory implements MutableBeanFactory {

    private Object manager = new MockOrmManager();


    @Override
    public Class<? extends Content> getBaseClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void beginTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void commitTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void rollbackTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public <T extends Content> T createBean(Class<T> cls) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public <T extends Content> boolean persistUncommitted(T bean) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public <T extends Content> boolean persist(T bean) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public <T extends Content> boolean delete(T bean) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Collection<Class<? extends Content>> getAllClasses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Map<Class<? extends Content>, List<Class<? extends Content>>> getImplementingClassesMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public <T extends Content> List<Class<T>> getImplementingClasses(Class<T> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public String getFilterQuery(Class<?> cls, String filterProperty, String filterValues) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void clearCacheFor(Class<? extends Content> cls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Object getManager() {
        return manager;
    } // getManager()

} // MockMutableBeanFactory
