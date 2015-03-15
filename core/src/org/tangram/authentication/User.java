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


/**
 * Generic tangram user.
 *
 * Instance may be web users of the application or users of the tangram backends like the editor.
 */
public interface User {

    /**
     * Users might be resolved by different providers.
     *
     * @return provider ID
     */
    String getProvider();


    /**
     * Provide a unique ID for this user regardless of backends and providers.
     *
     * @return system wide unique ID
     */
    String getId();


    /**
     * User might have a provider specific set of properties which can be accessed here.
     * 
     * @param name name of the property to request
     * @return value of the named property or null
     */
    Object getProperty(String name);

} // User()
