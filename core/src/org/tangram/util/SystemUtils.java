/**
 *
 * Copyright 2014-2020 Martin Goellnitz
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
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;
import org.tangram.content.Content;


/**
 * Generic String utility functions not found elsewhere in the libraries we are using.
 */
public final class SystemUtils {

    private SystemUtils() {
    }


    /**
     * Split a String - say servlet init parameter - at each ',' and trim the result.
     *
     * Actually very generic utility function. Trimming is used so that line breaks and spaces can be used
     * to format the input in config files.
     *
     * @param parameter comma separated list of string items
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
     * @throws URISyntaxException unlikely case that we might come across some invalid URLs during resource lookup
     * @throws IOException IO problems may occur during resource lookup
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


    /**
     * Generate a string readable hash value through a SHA256 message digest.
     *
     * SHA256 values can be manually generated via
     * http://www.xorbin.com/tools/sha256-hash-calculator or
     * http://hashgenerator.de/
     *
     * @param value text value to generate hash for
     * @return readable hash
     * @throws NoSuchAlgorithmException Should not happen but indicates that SHA256 is not available
     * @throws UnsupportedEncodingException Should not happen but indicates that UTF-8 encoding is not
     */
    public static String getSha256Hash(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
        byte[] hash = md.digest(value.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder(32);
        for (int i = 0; i<hash.length; i++) {
            int element = 0xff&hash[i];
            if (element<0x10) {
                hexString.append('0');
            } // if
            hexString.append(Integer.toHexString(element));
        } // for
        return hexString.toString();
    } // getSha256Hash()


    /**
     * Small helper method to keep areas with suppressed warnings small.
     *
     * @param <T> intended type of result
     * @param bean bean to be cast to the given type
     * @return converted result
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object bean) {
        return (T) bean;
    } // convert()


    /**
     * Small helper method to keep areas with suppressed warnings small.
     *
     * @param contents item to be cast to a list of contents
     * @return type casted input parameter
     */
    @SuppressWarnings("unchecked")
    public static List<Content> convertList(Object contents) {
        return (List<Content>) contents;
    } // convertList()

} // SystemUtils
