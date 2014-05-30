/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class Constants {

    private static final Log LOG = LogFactory.getLog(Constants.class);

    public static final String THIS = "self";

    /**
     * Default date format string for http headers and the like
     */
    public static final String DEFAULT_DATE_FORMAT = "kk:mm:ss dd.MM.yyyy zzz";

    /**
     * Pattern string to find IDs in Strings
     */
    public static final String ID_PATTERN = "([A-Z][a-zA-Z]+:[0-9]+)";


    /**
     * Pattern to find IDs in (rich/long) text
     */
    public final static Pattern TEXT_ID_PATTERN = Pattern.compile("http://[a-zA-Z0-9:]*\"");

    /**
     * name of the default view if value null cannot be used
     */
    public static final String DEFAULT_VIEW = "NULL";

    /**
     * name of the url parameter for the view to be selected in defaul controller.
     */
    public static final String PARAMETER_VIEW = "v";

    public static final String PARAMETER_PROTECTION_LOGIN = "protection.login.button";

    public static final String PARAMETER_PROTECTION_KEY = "protection.key";

    /**
     * name of the request attribute to take a currently valid logout url from
     */
    public static final String ATTRIBUTE_LOGOUT_URL = "logoutUrl";

    /*
     * name of the request attribute to take a currently valid login from
     */
    public static final String ATTRIBUTE_LOGIN_URL = "loginUrl";

    /**
     * name of the request attribute to take the result of a login attempt from
     */
    public static final String ATTRIBUTE_LOGIN_RESULT = "loginResult";

    /**
     * name of the request attribute to take the value of a protecton from, which
     * needs to be met right now for the content to be displayed. If the value
     * is not null, we should issue a login form to the user in this output
     */
    public static final String ATTRIBUTE_PROTECTION = "protection";

    /**
     *
     */
    public static final String ATTRIBUTE_LIVE_SYSTEM = "tangramLiveSystem";

    /**
     * name of the request attribute holding the currently logged in tangram user
     */
    public static final String ATTRIBUTE_USER = "tangramUser";

    /**
     * name of the request attribute to indicate if the currently logged in tangram user
     * is considered a tangram admin by system configuration
     */
    public static final String ATTRIBUTE_ADMIN_USER = "tangramAdminUser";

    public static final String PROPERTY_VERSION_BUILD = "version.build";

    public static final int RIP_CORD_COUNT = 8;

    public static final String VERSION_MAJOR = "0";

    public static final String VERSION_MINOR = "9";

    public static final Map<String, String> VERSIONS = new HashMap<String, String>();

    private static final String PREFIX = "tangram";

    private static final String SUFFIX = "-build.properties";

    private static final int SUFFIX_LENGTH = SUFFIX.length();


    private static Set<String> getResourceListing(URL pathUrl, String prefix, String suffix) throws URISyntaxException,
            IOException {
        Set<String> result = new HashSet<String>();
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


    static {
        try {
            Enumeration<URL> en = Constants.class.getClassLoader().getResources(PREFIX);
            while (en.hasMoreElements()) {
                URL metaInf = en.nextElement();
                for (String s : getResourceListing(metaInf, PREFIX, SUFFIX)) {
                    Properties p = new Properties();
                    p.load(Constants.class.getClassLoader().getResourceAsStream(s));
                    VERSIONS.put(s.substring(8, s.length()-SUFFIX_LENGTH),
                            p.getProperty(Constants.PROPERTY_VERSION_BUILD));
                } // for
            } // while
        } catch (Exception e) {
            LOG.error("{} error while reading all modules building properties", e);
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


    /**
     * just to protect this stuff from being instantiated
     */
    private Constants() {
    } // Constants()

} // Constants
