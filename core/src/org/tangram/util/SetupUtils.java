/**
 *
 * Copyright 2014-2015 Martin Goellnitz
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.StringUtils;


/**
 * Generic String utility functions not found elsewhere in the libraries we are using.
 */
public final class SetupUtils {

    private SetupUtils() {
    }


    /**
     * Split a String - say servlet init parameter - at each ',' and trim the result.
     *
     * Actually very generic utility fuction. Trimming is used so that line breaks and spaces can be used
     * to format the input in config files.
     *
     * @param parameter
     * @return set of string taken from the input parameter
     */
    public static Set<String> stringSetFromParameterString(String parameter) {
        Set<String> result = new HashSet<>();
        if (StringUtils.isNotBlank(parameter)) {
            String[] parts = parameter.split(",");
            for (String part : parts) {
                result.add(part.trim());
            } // for
        } // if
        return result;
    } // stringSetFromParameterString()


    private static Set<String> getResourceListing(URL pathUrl, String prefix, String suffix) throws URISyntaxException, IOException {
        Set<String> result = new HashSet<>();
        if ("file".equals(pathUrl.getProtocol())) {
            for (String name : new File(pathUrl.toURI()).list()) {
                if (name.endsWith(suffix)) {
                    result.add(prefix+"/"+name);
                } // if
            } // for
        } // if

        if ("jar".equals(pathUrl.getProtocol())) {
            String jarPath = pathUrl.getPath().substring(5, pathUrl.getPath().indexOf("!")); // only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
                String name = entries.nextElement().getName();
                if (name.startsWith(prefix)&&name.endsWith(suffix)) {
                    result.add(name);
                } // if
            } // for
            jar.close();
        } // if

        return result;
    } // getResourceListing()


    /**
     * Read a set of files names from resources or files in the class path.
     *
     * @param prefix prefix of the names in the result set including path and name prefix
     * @param suffix suffix of the names in the result set including e.g. and extension or naming convention
     * @return set of file names to be used for getResource()
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Set<String> getResourceListing(String prefix, String suffix) throws URISyntaxException, IOException {
        Set<String> result = new HashSet<>();
        Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(prefix);
        while (en.hasMoreElements()) {
            URL metaInf = en.nextElement();
            result.addAll(getResourceListing(metaInf, prefix, suffix));
        } // while
        return result;
    } // getResourceListing()

} // SetupUtils