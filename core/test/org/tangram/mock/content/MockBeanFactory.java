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
package org.tangram.mock.content;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.FileNotFoundException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.Abstract;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.content.TransientCode;
import org.tangram.util.ClassResolver;
import org.tangram.util.SystemUtils;


/**
 * Mock a bean factory returning some instances to provides means for other components's tests.
 */
public class MockBeanFactory extends AbstractBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MockBeanFactory.class);

    private final Map<Class<? extends Content>, List<BeanListener>> attachedListeners = new HashMap<>();

    private final Map<Class<? extends Content>, List<Content>> contents = new HashMap<>();


    public void init() throws FileNotFoundException {
        XStream xstream = new XStream(new StaxDriver());
        Set<String> basePackages = new HashSet<>();
        basePackages.add("org.tangram.mock.content");
        basePackages.add("org.tangram.content");
        allClasses = new ArrayList<>();
        ClassResolver resolver = new ClassResolver(basePackages);
        Set<Class<? extends Content>> resolvedClasses = new HashSet<>();
        resolvedClasses.add(TransientCode.class);
        resolvedClasses.addAll(resolver.getSubclasses(MockContent.class));
        for (Class<? extends Content> cls : resolvedClasses) {
            LOG.info("getAllClasses() * {}", cls.getName());
            if (!allClasses.contains(cls)) {
                allClasses.add(cls);
                xstream.alias(cls.getSimpleName(), cls);
            } // if
        } // for
        Object mockContents = xstream.fromXML(this.getClass().getResource("/mock-content.xml"));
        LOG.debug("() {}", mockContents);
        if (mockContents instanceof List) {
            List<? extends Content> list = SystemUtils.convertList(mockContents);
            for (Content o : list) {
                LOG.info("() {}", o);
                List<Content> l = contents.get(o.getClass());
                if (l==null) {
                    l = new ArrayList<>();
                    contents.put(o.getClass(), l);
                } // if
                l.add(o);
            } // for
        } // if
    } // ()


    public Map<Class<? extends Content>, List<Content>> getContents() {
        return contents;
    }


    @Override
    public <T extends Content> T getBean(Class<T> cls, String id) {
        T result = null;
        for (Class<? extends Content> c : contents.keySet()) {
            if (cls.isAssignableFrom(c)) {
                for (Content content : contents.get(c)) {
                    if (content.getId().equals(id)) {
                        result = (T) content;
                    } // if
                } // for
            } // if
        } // for
        return result;
    } // getBean()


    @Override
    public Content getBean(String id) {
        Content result = null;
        for (List<Content> l : contents.values()) {
            for (Content c : l) {
                if (c.getId().equals(id)) {
                    result = c;
                } // if
            } // for
        } // for
        return result;
    } // getBean()

    protected List<Class<? extends Content>> allClasses = null;


    public Collection<Class<? extends Content>> getClasses() {
        Collection<Class<? extends Content>> modelClasses = new ArrayList<>();
        for (Class<? extends Content> cls : allClasses) {
            if ((cls.getAnnotation(Abstract.class)==null)&&(!cls.isInterface())&&((cls.getModifiers()&Modifier.ABSTRACT)==0)) {
                modelClasses.add(cls);
            } // if
        } // for
        Comparator<Class<?>> comp = new Comparator<Class<?>>() {

            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getName().compareTo(o2.getName());
            } // compareTo()

        };
        // Collections.sort(modelClasses, comp);
        return modelClasses;
    } // getClasses()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery, String orderProperty, Boolean ascending) {
        LOG.debug("listBeans() :{} {} {} {}", cls.getSimpleName(), optionalQuery, orderProperty, ascending);
        List<T> result = new ArrayList<>();
        for (Class<? extends Content> cx : getClasses()) {
            if (cls.isAssignableFrom(cx)) {
                Class<? extends T> c = SystemUtils.convert(cx);
                List<? extends T> beans = listBeansOfExactClass(c, optionalQuery, orderProperty, ascending);
                result.addAll(beans);
            } // if
        } // for
        return result;
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String optionalQuery, String orderProperty, Boolean ascending) {
        List<T> result = (List<T>) contents.get(cls);
        if (result==null) {
            result = Collections.EMPTY_LIST;
        } // if
        return result;
    } // listBeansOfExactClass()


    /**
     * Attach a listener for any changes dealing with classes of the given type.
     *
     * @param cls class to be notified when instances of that class have been changed
     * @param listener listener to be notified about changes
     */
    @Override
    public void addListener(Class<? extends Content> cls, BeanListener listener) {
        synchronized (attachedListeners) {
            List<BeanListener> listeners = attachedListeners.get(cls);
            if (listeners==null) {
                listeners = new ArrayList<>();
                attachedListeners.put(cls, listeners);
            } // if
            listeners.add(listener);
        } // synchronized
        listener.reset();
        LOG.info("addListener() {}: {}", cls.getSimpleName(), attachedListeners.get(cls).size());
    } // addListener()

} // MockBeanFactory
