/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Constants {

    private static final Log log = LogFactory.getLog(Constants.class);

    public static final String THIS = "self";

    public static final String DEFAULT_VIEW = "NULL";

    public static final String PARAMETER_VIEW = "v";

    public static final String PARAMETER_PROTECTION_LOGIN = "protection.login.button";

    public static final String PARAMETER_PROTECTION_KEY = "protection.key";

    public static final String ATTRIBUTE_LOGOUT_URL = "logoutUrl";

    public static final String ATTRIBUTE_LOGIN_URL = "loginUrl";

    public static final String ATTRIBUTE_LOGIN_RESULT = "loginResult";

    public static final String ATTRIBUTE_PROTECTION = "protection";

    public static final String ATTRIBUTE_LIVE_SYSTEM = "tangramLiveSystem";

    public static final String ATTRIBUTE_USER = "tangramUser";

    public static final String ATTRIBUTE_ADMIN_USER = "tangramAdminUser";

    public static final String ATTRIBUTE_CONTEXT = "org.springframework.web.servlet.DispatcherServlet.CONTEXT";

    public static final String PROPERTY_VERSION_BUILD = "version.build";

    public static final int RIP_CORD_COUNT = 10;

    public static final String VERSION_MAJOR = "0";

    public static final String VERSION_MINOR = "6";

    public static final Map<String, String> VERSIONS = new HashMap<String, String>();

    private static final int SUFFIX_LENGTH = "-build.properties".length();


    private static String[] getResourceListing(URL dirURL, String path) throws URISyntaxException, IOException {
        if (dirURL!=null&&dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        } // if

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip out only the JAR
                                                                                           // file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
            Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { // filter according to the path
                    result.add(name);
                } // if
            } // while
            return result.toArray(new String[result.size()]);
        } // if

        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    } // getResourceListing()

    static {
        try {
            Enumeration<URL> en = Constants.class.getClassLoader().getResources("tangram");
            while (en.hasMoreElements()) {
                URL metaInf = en.nextElement();
                String[] filenames = getResourceListing(metaInf, "tangram");
                for (String s : filenames) {
                    if (s.endsWith("build.properties")) {
                        Properties p = new Properties();
                        p.load(Constants.class.getClassLoader().getResourceAsStream(s));
                        VERSIONS.put(s.substring(8, s.length()-SUFFIX_LENGTH),
                                p.getProperty(Constants.PROPERTY_VERSION_BUILD));
                    } // if
                } // for
            } // while
        } catch (Exception e) {
            log.error("{} error while reading all modules building properties", e);
        } // try/catch
        StringBuilder versionBuilder = new StringBuilder();
        versionBuilder.append(VERSION_MAJOR);
        versionBuilder.append(".");
        versionBuilder.append(VERSION_MINOR);
        for (String key : VERSIONS.keySet()) {
            versionBuilder.append(".");
            versionBuilder.append(key);
            versionBuilder.append(VERSIONS.get(key));
        } // for
        VERSION = versionBuilder.toString();
    } // static

    public static final String VERSION;

} // Constants
