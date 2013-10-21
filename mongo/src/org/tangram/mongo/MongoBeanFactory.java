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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.mongo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.nucleus.NucleusBeanFactory;

public class MongoBeanFactory extends NucleusBeanFactory {

    private static final Log log = LogFactory.getLog(MongoBeanFactory.class);

    String host;

    String port;

    String database;


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public String getPort() {
        return port;
    }


    public void setPort(String port) {
        this.port = port;
    }


    public String getDatabase() {
        return database;
    }


    public void setDatabase(String database) {
        this.database = database;
    }


    @Override
    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        Map<Object, Object> result = new HashMap<Object, Object>();
        if (StringUtils.isNotEmpty(getHost())) {
            log.info("getFactoryConfigOverrides() have host for mongoDB: "+getDatabase());
            if (StringUtils.isNotEmpty(getDatabase())) {
                log.info("getFactoryConfigOverrides() have databasefor mongoDB: "+getDatabase());
                if (StringUtils.isNotEmpty(getPort())) {
                    log.info("getFactoryConfigOverrides() have databasefor mongoDB: "+getPort());
                    result.put("datanucleus.ConnectionURL", "mongodb:"+getHost()+":"+getPort()+"/"+getDatabase());
                } // if
            } // if
        } // if
        return result;
    } // getFactoryConfigOverrides()

} // MongoBeanFactory
