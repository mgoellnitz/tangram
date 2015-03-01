/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.util.SetupUtils;


public final class Constants {

    private static final Logger LOG = LoggerFactory.getLogger(Constants.class);

    /**
     * Overall version descriptor of the system calculated below.
     */
    public static final String VERSION;

    public static final String THIS = "self";

    /**
     * Default date format string for http headers and the like.
     */
    public static final String DEFAULT_DATE_FORMAT = "kk:mm:ss dd.MM.yyyy zzz";

    /**
     * Pattern string to find IDs in Strings
     */
    public static final String ID_PATTERN = "([A-Z][a-zA-Z]+:[0-9]+)";

    /**
     * Pattern to find IDs in (rich/long) text.
     */
    public final static Pattern TEXT_ID_PATTERN = Pattern.compile("http://[a-zA-Z0-9:]*\"");

    /**
     * name of the default view if value null cannot be used.
     */
    public static final String DEFAULT_VIEW = "NULL";

    /**
     * name of the url parameter for the view to be selected in defaul controller.
     */
    public static final String PARAMETER_VIEW = "v";

    public static final String PARAMETER_PROTECTION_LOGIN = "protection.login.button";

    public static final String PARAMETER_PROTECTION_KEY = "protection.key";

    /**
     * Name of the request attribute to hold the view to be used for link generation within text properties.
     */
    public static final String ATTRIBUTE_EMBEDDED_VIEW = "embedded.link.view";

    /**
     * Name of the request attribute to hold the action to be used for link generation within text properties.
     */
    public static final String ATTRIBUTE_EMBEDDED_ACTION = "embedded.link.action";

    /**
     * name of the attribute holding the handled request.
     */
    public static final String ATTRIBUTE_REQUEST = "request";

    /**
     * name of the attribute holding response to be issued.
     */
    public static final String ATTRIBUTE_RESPONSE = "response";

    /**
     * name of the application attribute to hold the view settings hash map.
     */
    public static final String ATTRIBUTE_VIEW_SETTINGS = "viewSettings";

    /**
     * name of the application attribute to hold the view utilities instance.
     */
    public static final String ATTRIBUTE_STATISTICS = "statistics";

    /**
     * name of the application attribute to hold the bean factory instance.
     */
    public static final String ATTRIBUTE_BEAN_FACTORY = "beanFactory";

    /**
     * name of the application attribute to hold the view utilities instance.
     */
    public static final String ATTRIBUTE_VIEW_UTILITIES = "viewUtilities";

    /**
     * name of the application attribute to hold the link factory aggregator instance.
     */
    public static final String ATTRIBUTE_LINK_FACTORY_AGGREGATOR = "linkFactoryAggregator";

    /**
     * name of the request attribute to take the result of a login attempt from.
     */
    public static final String ATTRIBUTE_LOGIN_RESULT = "loginResult";

    /**
     * name of the request attribute to take the value of a protecton from, which
     * needs to be met right now for the content to be displayed. If the value
     * is not null, we should issue a login form to the user in this output
     */
    public static final String ATTRIBUTE_PROTECTION = "protection";

    /**
     * Name of the session attribute holding the last return url for logins etc.
     */
    public static final String ATTRIBUTE_RETURN_URL = "tangram.return.url";

    /**
     * Name of the session attribute holding the currently logged in user.
     */
    public static final String ATTRIBUTE_USERS = "tangram.users";

    public static final String PROPERTY_VERSION_BUILD = "version.build";

    public static final int RIP_CORD_COUNT = 8;

    public static final String VERSION_MAJOR = "1";

    public static final String VERSION_MINOR = "0";

    public static final Map<String, String> VERSIONS = new HashMap<>();

    private static final String PREFIX = "tangram";

    private static final String SUFFIX = "-build.properties";

    private static final int SUFFIX_LENGTH = SUFFIX.length();


    static {
        try {
            for (String s : SetupUtils.getResourceListing(PREFIX, SUFFIX)) {
                Properties p = new Properties();
                p.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(s));
                VERSIONS.put(s.substring(8, s.length()-SUFFIX_LENGTH), p.getProperty(Constants.PROPERTY_VERSION_BUILD));
            } // for
        } catch (Exception e) {
            LOG.error("{} error while reading all modules building properties", e);
        } // try/catch
        StringBuilder versionBuilder = new StringBuilder(128);
        versionBuilder.append(VERSION_MAJOR);
        versionBuilder.append('.');
        versionBuilder.append(VERSION_MINOR);
        for (String key : VERSIONS.keySet()) {
            versionBuilder.append('.');
            versionBuilder.append(key);
            versionBuilder.append(VERSIONS.get(key));
        } // for
        VERSION = versionBuilder.toString();
    } // static


    /**
     * just to protect this stuff from being instantiated.
     */
    private Constants() {
    } // Constants()

} // Constants
