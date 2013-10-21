/**
 * 
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.coma;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public class ComaContent implements Content, Map<String, Object> {

    private String id;

    private String documentType;

    private Map<String, Object> properties = new HashMap<String, Object>();


    public ComaContent(String id, String type, Map<String, Object> properties) {
        this.id = id;
        this.documentType = type;
        this.properties = properties;
    } // ComeContent()


    @Override
    public String getId() {
        return id;
    }


    public String getDocumentType() {
        return documentType;
    }


    @Override
    public void setBeanFactory(BeanFactory factory) {

    }


    public Object get(String name) {
        return properties.get(name);
    } // getProperty()


    public void set(String name, Object value) {
        properties.put(name, value);
    } // set()


    @Override
    public boolean persist() {
        // this is a read only implementation
        return false;
    } // persist()


    /** Map **/

    @Override
    public void clear() {
        properties.clear();
    }


    @Override
    public Set<Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }


    @Override
    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }


    @Override
    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }


    @Override
    public Object put(String key, Object value) {
        return properties.put(key, value);
    }


    @Override
    public Object get(Object key) {
        return properties.get(key);
    }


    @Override
    public int size() {
        return properties.size();
    }


    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }


    @Override
    public Set<String> keySet() {
        return properties.keySet();
    }


    @Override
    public Object remove(Object key) {
        return properties.remove(key);
    }


    @Override
    public Collection<Object> values() {
        return properties.values();
    }


    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        properties.putAll(m);
    }


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()

} // ComaContent
