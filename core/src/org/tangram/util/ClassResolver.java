/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * Resolve classes or classnames from a given set of packages.
 *
 * One design goal in finding the classes was to hit the JAR files just once.
 *
 */
public class ClassResolver {

    private static final Log log = LogFactory.getLog(ClassResolver.class);

    private Set<String> packageNames;


    public ClassResolver() {
        packageNames = new HashSet<String>();
    } // ClassResolver()


    public ClassResolver(Set<String> packageNames) {
        this.packageNames = packageNames;
    } // ClassResolver()


    public void addPackageName(String packageName) {
        packageNames.add(packageName);
    } // addPackageName()


    private void addUrlsForPackage(Set<URL> urls, String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(packagePath);
            while (urlEnumeration.hasMoreElements()) {
                URL u = urlEnumeration.nextElement();
                String url = u.toString();
                // log.info("addUrlsForPackage() "+url);
                int idx = url.indexOf('!');
                if (idx>0) {
                    url = url.substring(0, idx);
                } // if
                if (url.startsWith("jar:")) {
                    url = url.substring(4);
                } // if
                u = new URL(url);
                if (log.isInfoEnabled()) {
                    log.info("addUrlsForPackage() "+url);
                } // if

                if (url.endsWith(".jar")) {
                    urls.add(u);
                } // if
            } // while
        } catch (IOException e) {
            log.error("addUrlsForPackage()", e);
        } // try/catch
    } // addUrlsForPackage()


    private Set<String> getClassNames() {
        Set<String> classNames = new HashSet<String>();
        Set<URL> urls = new HashSet<URL>();
        for (String packageName : packageNames) {
            addUrlsForPackage(urls, packageName);
        } // if
        for (URL u : urls) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("getClassNames() u="+u);
                } // if
                JarInputStream is = new JarInputStream(u.openStream());
                for (JarEntry entry = is.getNextJarEntry(); entry!=null;entry = is.getNextJarEntry()) {
                    final String name = entry.getName().replace('/', '.');
                    if (name.endsWith(".class")&&(name.indexOf('$')<0)) {
                        String className = name.substring(0, name.length()-6);
                        boolean add = false;
                        for (String packageName : packageNames) {
                            add = add||(className.startsWith(packageName));
                        } // if
                        if (add) {
                            classNames.add(className);
                        } // if
                    } // if
                } // for
            } catch (IOException e) {
                log.error("getClassNames()", e);
            } // try/catch
        } // for
        return classNames;
    } // getClassNames()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass which are no interfaces.
     */
    public <T extends Object> Set<Class<T>> getSubclasses(Class<T> c) {
        Set<Class<T>> result = new HashSet<Class<T>>();
        Set<String> classNames = getClassNames();
        for (String className : classNames) {
            try {
                @SuppressWarnings("unchecked")
                Class<T> cls = (Class<T>) Class.forName(className);
                if ((!cls.isInterface())&&c.isAssignableFrom(cls)&&((cls.getModifiers()&Modifier.ABSTRACT)==0)) {
                    result.add(cls);
                } // if
            } catch (ClassNotFoundException e) {
                log.error("getSubclasses()", e);
            } // try/catch
        } // if
        return result;
    } // getSubclasses()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass.
     * Interfaces are excluded.
     */
    public <T extends Object> Set<Class<T>> getAnnotatedSubclasses(Class<T> c, Class<? extends Annotation> annotation) {
        Set<Class<T>> result = new HashSet<Class<T>>();
        Set<String> classNames = getClassNames();
        for (String className : classNames) {
            try {
                @SuppressWarnings("unchecked")
                Class<T> cls = (Class<T>) Class.forName(className);
                if ((cls.getAnnotation(annotation)!=null)&&c.isAssignableFrom(cls)&&(!cls.isInterface())) {
                    result.add(cls);
                } // if
            } catch (ClassNotFoundException e) {
                log.error("getAnnotatedSubclasses()", e);
            } // try/catch
        } // if
        return result;
    } // getAnnotatedSubclasses()

} // ClassResolver
