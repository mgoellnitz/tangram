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
package org.tangram.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JavaBean {

    private static Log log = LogFactory.getLog(JavaBean.class);

    private Object delegate;

    private Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();


    public JavaBean(Object delegate) throws IntrospectionException {
        this.delegate = delegate;
        BeanInfo info = Introspector.getBeanInfo(delegate.getClass());
        final PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            descriptors.put(descriptor.getName(), descriptor);
        } // for
    } // JavaBean()


    /**
     * Return a collection of all property names of the underying bean.
     *
     * @return
     */
    public Collection<String> propertyNames() {
        return descriptors.keySet();
    } // propertyNames


    /**
     * return the value of a property of the underlying bean.
     *
     * @param name name of the property to get value for
     * @return property's value
     */
    public Object get(String name) {
        Object result = null;
        final Method readMethod = descriptors.get(name).getReadMethod();
        if (readMethod!=null) {
            try {
                result = readMethod.invoke(delegate);
            } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                log.error("get()", ex);
            } // try/catch
        } // if
        return result;
    } // get()


    /**
     * Tell if a property of the underlying bean is readable
     *
     * @param name name of the property to check
     * @return true of the given property has a read method
     */
    public boolean isReadable(String name) {
        return descriptors.get(name).getReadMethod()!=null;
    } // isReadable()


    /**
     * set the value for a property of the underlying bean.
     *
     * @param name name of the property to set value for
     * @param value new value for the given property
     */
    public void set(String name, Object value) {
        final Method writeMethod = descriptors.get(name).getWriteMethod();
        if (writeMethod!=null) {
            try {
                writeMethod.invoke(delegate, value);
            } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                log.error("set()", ex);
            } // try/catch
        } // if
    } // set()


    /**
     * Tell if a property of the underlying bean is writable
     *
     * @param name name of the property to check
     * @return true if the given property has a write method
     */
    public boolean isWritable(String name) {
        return descriptors.get(name).getWriteMethod()!=null;
    } // isWritable()


    /**
     * return the type of the property
     *
     * @param name name of the property
     * @return class for the given property
     */
    public Class<? extends Object> getType(String name) {
        return descriptors.get(name).getPropertyType();
    } // getType()


    /**
     * Return the type of collection elements. The given property must be of any collectio type!
     *
     * @param name name of the property holding a collection
     * @return type of the elements of the collection
     */
    public Class<? extends Object> getCollectionType(String name) {
        ParameterizedType returnType = (ParameterizedType) (descriptors.get(name).getReadMethod().getGenericReturnType());
        return (Class<? extends Object>)returnType.getActualTypeArguments()[0];
    } // getCollectionType()

} // JavaBean
