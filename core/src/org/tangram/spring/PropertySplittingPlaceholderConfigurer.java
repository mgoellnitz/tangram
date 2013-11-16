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

    final private Properties learnings = new Properties();


    /**
     * store URL parts of a property's values with suffixed names in a given set of properties
     *
     * @param propertyValue original value of the property
     * @param propertyName base name of the parts
     * @param props properties set to store split values in
     */
    private void storeUrlParts(String propertyValue, String propertyName, Properties props) {
        // split
        int idx = propertyValue.indexOf("://");
        if (log.isDebugEnabled()) {
            log.debug("storeUrlParts("+idx+") "+propertyValue);
        } // if
        if ((idx>0)&&(propertyValue.length()>idx+5)) {
            // Might be a URL
            try {
                if (log.isInfoEnabled()) {
                    log.info("storeUrlParts() splitting "+propertyValue+"("+propertyName+")");
                } // if
                String protocol = propertyValue.substring(0, idx);
                if (StringUtils.isNotBlank(protocol)) {
                    props.setProperty(propertyName+".protocol", protocol);
                } // if
                idx += 3;
                String host = propertyValue.substring(idx);
                if (log.isDebugEnabled()) {
                    log.debug("storeUrlParts() host I: "+host);
                } // if
                String uri = "";
                idx = host.indexOf('/');
                if (idx>0) {
                    uri = host.substring(idx+1);
                    host = host.substring(0, idx);
                } // if
                if (log.isDebugEnabled()) {
                    log.debug("storeUrlParts() host II: "+host);
                    log.debug("storeUrlParts() uri: "+uri);
                } // if
                if (StringUtils.isNotBlank(uri)) {
                    props.setProperty(propertyName+".uri", uri);
                } // if
                String username = "";
                idx = host.indexOf('@');
                if (idx>0) {
                    username = host.substring(0, idx);
                    host = host.substring(idx+1);
                } // if
                if (log.isDebugEnabled()) {
                    log.debug("storeUrlParts() host III: "+host);
                    log.debug("storeUrlParts() username: "+username);
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
                idx = host.indexOf(':');
                if (idx>0) {
                    port = host.substring(idx+1);
                    host = host.substring(0, idx);
                } // if
                if (StringUtils.isNotBlank(host)) {
                    props.setProperty(propertyName+".host", host);
                } // if
                if (StringUtils.isNotBlank(port)) {
                    props.setProperty(propertyName+".port", port);
                } // if
            } catch (Exception e) {
                log.error("convertProperties() error reading "+propertyValue+" as a url", e);
            } // try/catch
        } // if
    } // storeUrlParts()


    /**
     * convert all possible URLs in property files
     *
     * @param props
     */
    @Override
    protected void convertProperties(Properties props) {
        super.convertProperties(props);
        Enumeration<?> propertyNames = props.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = props.getProperty(propertyName);
            storeUrlParts(propertyValue, propertyName, props);
        } // while
    } // convertProperties()


    @Override
    protected String resolveSystemProperty(String key) {
        String result = super.resolveSystemProperty(key);
        if (result==null) {
            if (log.isDebugEnabled()) {
                log.debug("resolveSystemProperty() nothing found in system properties for "+key);
            } // if
            int idx = key.lastIndexOf('.');
            if (idx>0) {
                String baseKey = key.substring(0, idx);
                if (log.isDebugEnabled()) {
                    log.debug("resolveSystemProperty() lookup for baseKey "+baseKey);
                } // if
                String value = super.resolveSystemProperty(baseKey);
                storeUrlParts(value, baseKey, learnings);
                result = learnings.getProperty(key);
                if (log.isDebugEnabled()) {
                    log.debug("resolveSystemProperty() result "+baseKey+" is "+result);
                } // if
            } // if
        } // if
        return result;
    } // resolveSystemProperty()

} // PropertySplittingPlaceholderConfigurer
