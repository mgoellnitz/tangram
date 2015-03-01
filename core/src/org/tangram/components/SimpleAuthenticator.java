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
package org.tangram.components;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.pac4j.http.credentials.UsernamePasswordAuthenticator;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Quite too simple username/password authenticator implementation.
 * 
 * Using a string/string map to map usernames to their respective passwords.
 */
@Named("usernamePasswordAuthenticator")
@Singleton
public class SimpleAuthenticator implements UsernamePasswordAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleAuthenticator.class);

    @Inject
    @Named("usernamePasswordMapping")
    private Map<String, String> usernamePasswordMapping;


    @Override
    public void validate(UsernamePasswordCredentials upc) {
        LOG.info("validate() {} in {}", upc.getUsername(), usernamePasswordMapping);
        if (!usernamePasswordMapping.get(upc.getUsername()).equals(upc.getPassword())) {
            throw new RuntimeException("wrong credentials");
        } // if
    } // validate()

} // SimpleAuthenticator
