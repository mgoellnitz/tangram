/**
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
package org.tangram.feature.protection;


/**
 * Interface for password protection implementation to have common templates.
 *
 * A simple password protection combines a single user name with a password to protect some content items.
 */
public interface SimplePasswordProtection extends Protection {

    String PARAM_LOGIN = "login";

    String PARAM_PASSWORD = "password";

    String ERROR_CODE = "Falscher Benutzername und/oder falsches Paßwort eingegeben!";


    String getLogin();


    String getPassword();

} // SimplePasswordProtection
