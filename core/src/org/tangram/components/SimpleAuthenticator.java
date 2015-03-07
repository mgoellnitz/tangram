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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
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
    @Resource(name="usernamePasswordMapping")
    private Map<String, String> usernamePasswordMapping;


    /**
     * generate a string readable digest value with a given message digest instance.
     *
     * SHA256 values can be manually generated via
     * http://www.xorbin.com/tools/sha256-hash-calculator
     *
     * @param md digest to use
     * @param message message as byte array
     * @return readable digest
     */
    private static String getHash(MessageDigest md, byte[] message) {
        byte[] hash = md.digest(message);
        StringBuilder hexString = new StringBuilder(32);
        for (int i = 0; i<hash.length; i++) {
            int element = 0xff&hash[i];
            if (element<0x10) {
                hexString.append('0');
            } // if
            hexString.append(Integer.toHexString(element));
        } // for
        return hexString.toString();
    } // getHash()


    @Override
    public void validate(UsernamePasswordCredentials upc) {
        LOG.info("validate() {} in {}", upc.getUsername(), usernamePasswordMapping);
        try {
            MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
            String hash = getHash(md, upc.getPassword().getBytes("UTF-8"));
            Object storedHash = usernamePasswordMapping.get(upc.getUsername());
            if ((storedHash==null)||!storedHash.equals(hash)) {
                throw new RuntimeException("wrong credentials");
            } // if
        } catch (NoSuchAlgorithmException|UnsupportedEncodingException e) {
            LOG.error("validate()", e);
            throw new RuntimeException("internal error: "+e.getMessage(), e);
        } // try/catch
    } // validate()

} // SimpleAuthenticator
