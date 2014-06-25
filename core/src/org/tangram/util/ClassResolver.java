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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * Resolve classes or classnames from a given set of packages.
 *
 * One design goal in finding the classes was to hit the JAR files just once.
 *
 */
public class ClassResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ClassResolver.class);

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


    private void addPathsForPackage(Set<String> urls, String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(packagePath);
            while (urlEnumeration.hasMoreElements()) {
                String url = urlEnumeration.nextElement().toString();
                int idx = url.indexOf('!');
                if (idx>0) {
                    url = url.substring(0, idx);
                } // if
                if (url.startsWith("jar:")) {
                    url = url.substring(4);
                } // if
                if (url.startsWith("file:")) {
                    url = url.substring(5);
                } // if
                if (!url.endsWith(".jar")) {
                    url = url.substring(0, url.length()-packagePath.length());
                } //
                if (LOG.isInfoEnabled()) {
                    LOG.info("addPathsForPackage() "+url);
                } // if
                urls.add(url);
            } // while
        } catch (IOException e) {
            LOG.error("addPathsForPackage()", e);
        } // try/catch
    } // addPathsForPackage()


    /**
     * Checks a set of class or properties names and adds them to the classNames collection.
     * Check is performed against package name and file extension.
     *
     * @param classNames
     * @param name
     */
    private final void checkClassAndAdd(Set<String> classNames, String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkClassAndAdd() name="+name);
        } // if
        if (name.endsWith(".class")&&(name.indexOf('$')<0)) {
            name = name.replace(File.separatorChar, '/').replace('/', '.');
            String className = name.substring(0, name.length()-6);
            boolean add = false;
            for (String packageName : packageNames) {
                add = add||(className.startsWith(packageName));
            } // if
            if (add) {
                classNames.add(className);
            } // if
        } // if
    } // checkClassAndAdd()


    private final void recurseSubDir(Set<String> classNames, File dir, int basePathLength) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("recurseSubDir() scanning "+dir.getAbsolutePath());
        } // if
        for (File f : (dir.isDirectory() ? dir.listFiles() : new File[0])) {
            String fileName = f.getAbsolutePath().substring(basePathLength);
            if (LOG.isDebugEnabled()) {
                LOG.debug("recurseSubDir() fileName="+fileName);
            } // if
            if ((fileName.endsWith(".class"))||(fileName.endsWith(".properties"))) {
                checkClassAndAdd(classNames, fileName);
            } else {
                recurseSubDir(classNames, f, basePathLength);
            } // if
        } // for
    } // recurseSubDir()


    private Set<String> getClassNames() {
        Set<String> classNames = new HashSet<>();
        Set<String> paths = new HashSet<>();
        for (String packageName : packageNames) {
            addPathsForPackage(paths, packageName);
        } // for
        for (String path : paths) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getClassNames() path="+path);
                } // if
                if (path.endsWith(".jar")) {
                    JarInputStream is = new JarInputStream(new FileInputStream(path));
                    for (JarEntry entry = is.getNextJarEntry(); entry!=null; entry = is.getNextJarEntry()) {
                        final String name = entry.getName().replace('/', '.');
                        checkClassAndAdd(classNames, name);
                    } // for
                    is.close();
                } else {
                    File dir = new File(path);
                    int basePathLength = dir.getAbsolutePath().length()+1;
                    recurseSubDir(classNames, dir, basePathLength);
                } // if
            } catch (IOException e) {
                LOG.error("getClassNames()", e);
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
                LOG.error("getSubclasses()", e);
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
                LOG.error("getAnnotatedSubclasses()", e);
            } // try/catch
        } // if
        return result;
    } // getAnnotatedSubclasses()

} // ClassResolver
