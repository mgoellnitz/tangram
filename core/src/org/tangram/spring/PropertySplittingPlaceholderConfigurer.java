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
package org.tangram.spring;

import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;


/**
 * Springframwork placeholder configurer to split any URL form property into parts.
 *
 * So anything in the form of
 *
 * # Example
 * url=mongodb://ruth:guessme@mongo.host:8111/db
 *
 * gets exploded as if it would read
 *
 * url.username=ruth
 * url.password=guessme
 * url.host=mongo.host
 * url.port=8111
 * url.uri=db
 * url.protocol=mongo
 *
 * For JDBC connection URLs the protocol will be something in the form of jdbc:<db> not just <db>.
 *
 */
public class PropertySplittingPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private static final Log log = LogFactory.getLog(PropertySplittingPlaceholderConfigurer.class);


    @Override
    protected void convertProperties(Properties props) {
        super.convertProperties(props);
        Enumeration<?> propertyNames = props.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = props.getProperty(propertyName);
            // split
            int idx = propertyValue.indexOf("://");
            if ((idx>0)&&(propertyValue.length()>idx+5)) {
                // Might be a URL
                try {
                    if (log.isDebugEnabled()) {
                        log.info("convertProperties() splitting "+propertyValue+"("+propertyName+")");
                    } // if
                    String protocol = propertyValue.substring(0, idx);
                    if (StringUtils.isNotBlank(protocol)) {
                        props.setProperty(propertyName+".protocol", protocol);
                    } // if
                    idx += 3;
                    String hostname = propertyValue.substring(idx);
                    if (log.isDebugEnabled()) {
                        log.debug("hostname I: "+hostname);
                    } // if
                    String uri = "";
                    idx = hostname.indexOf('/');
                    if (idx>0) {
                        uri = hostname.substring(idx+1);
                        hostname = hostname.substring(0, idx);
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("hostname II: "+hostname);
                        log.debug("uri: "+uri);
                    } // if
                    if (StringUtils.isNotBlank(uri)) {
                        props.setProperty(propertyName+".uri", uri);
                    } // if
                    String username = "";
                    idx = hostname.indexOf('@');
                    if (idx>0) {
                        username = hostname.substring(0, idx);
                        hostname = hostname.substring(idx+1);
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("hostname III: "+hostname);
                        log.debug("username: "+username);
                    } // if
                    idx = username.indexOf(':');
                    if (idx>0) {
                        String[] userinfos = username.split(":");
                        if (userinfos.length>1) {
                            username = userinfos[0];
                            props.setProperty(propertyName+".password", userinfos[1]);
                        } // if
                    } // if
                    if (StringUtils.isNotBlank(username)) {
                        props.setProperty(propertyName+".username", username);
                    } // if

                    String port = "";
                    idx = hostname.indexOf(':');
                    if (idx>0) {
                        port = hostname.substring(idx+1);
                        hostname = hostname.substring(0, idx);
                    } // if
                    if (StringUtils.isNotBlank(hostname)) {
                        props.setProperty(propertyName+".host", hostname);
                    } // if
                    if (StringUtils.isNotBlank(port)) {
                        props.setProperty(propertyName+".port", port);
                    } // if
                } catch (Exception e) {
                    log.error("convertProperties() error reading "+propertyValue+" as a url", e);
                } // try/catch
            } // if
        } // while
    } // convertProperties()

} // PropertySplittingPlaceholderConfigurer
