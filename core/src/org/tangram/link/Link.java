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
package org.tangram.link;

import java.util.HashMap;
import java.util.Map;

/**
 * Instances describe a link in more detail than just a url.
 *
 * Links are described by the URL and optionally a target window identifier for the client browser and a map
 * of named handlers as attributes for e.g. an a tag like onclick.
 *
 */
public class Link {

    private String url;

    private String target;

    private final Map<String, String> handlers = new HashMap<>();


    public Link() {
    }


    public Link(String url) {
        this.url = url;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public String getTarget() {
        return target;
    }


    public void setTarget(String target) {
        this.target = target;
    }


    public void addHandler(String handler, String value) {
        handlers.put(handler, value);
    }


    public void removeHandler(String handler) {
        handlers.remove(handler);
    }


    public Map<String, String> getHandlers() {
        return handlers;
    }


    @Override
    public String toString() {
        return url+"@"+target+": "+handlers;
    } // toString()

} // Link
