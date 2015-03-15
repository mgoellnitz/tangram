/*
 * 
 * Copyright 2015 Martin Goellnitz
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
package org.tangram.authentication;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;


/**
 * User implementation using a username and a provider.
 * the username must be unique accross the providers so that
 * this implementation can guarantee unique user ids throughout the system.
 */
public class GenericUser implements User, Serializable {

    private static final long serialVersionUID = 324847968531348468L;

    private final String provider;

    private final String id;

    private final Map<String, Object> properties;


    public GenericUser(String provider, String username, Map<String, Object> properties) {
        this.provider = provider;
        this.id = provider+":"+username;
        this.properties = properties;
    } // ()


    @Override
    public String getProvider() {
        return provider;
    }


    @Override
    public String getId() {
        return id;
    } // getId()


    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    } // getProperty()


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97*hash+Objects.hashCode(this.id);
        return hash;
    } // hashCode()


    @Override
    public boolean equals(Object obj) {
        if (obj==null) {
            return false;
        }
        if (getClass()!=obj.getClass()) {
            return false;
        }
        final GenericUser other = (GenericUser) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    } // equals()


    @Override
    public String toString() {
        return getId();
    } // toString()

} // GenericUser
