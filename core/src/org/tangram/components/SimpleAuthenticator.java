/*
 *
 * Copyright 2015-2016 Martin Goellnitz
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
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.util.SystemUtils;


/**
 * Very simple username/password authenticator implementation.
 *
 * Using a string/string map to map usernames to their respective password SHA256 hashes.
 */
@Named("usernamePasswordAuthenticator")
@Singleton
public class SimpleAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleAuthenticator.class);

    @Inject
    @Named("usernamePasswordMapping")
    @Resource(name = "usernamePasswordMapping")
    private Map<String, String> usernamePasswordMapping;

    @Inject
    private CodeResourceCache codeResourceCache;


    @Override
    public void validate(UsernamePasswordCredentials upc, WebContext wc) throws HttpAction {
        LOG.info("validate() {} in {}", upc.getUsername(), usernamePasswordMapping);
        try {
            LOG.debug("validate() {}", codeResourceCache.getTypeCache("text/plain"));
            CodeResource code = codeResourceCache.getTypeCache("text/plain").get("users.properties");
            Properties p = new Properties();
            try {
                LOG.debug("validate() {}", code);
                if (code!=null) {
                    p.load(code.getStream());
                } // ifF
            } catch (Exception e) {
                LOG.error("validate() error while reading user database", e);
            } // try/catch
            p.putAll(usernamePasswordMapping);
            String hash = SystemUtils.getSha256Hash(upc.getPassword());
            Object storedHash = p.get(upc.getUsername());
            LOG.debug("validate() {} in {} ({})", upc.getUsername(), p, storedHash);
            if ((storedHash==null)||!storedHash.equals(hash)) {
                throw new RuntimeException("wrong credentials");
            } // if
            CommonProfile profile = new CommonProfile();
            profile.setId(upc.getUsername());
            profile.addAttribute(Pac4jConstants.USERNAME, upc.getUsername());
            upc.setUserProfile(profile);
        } catch (NoSuchAlgorithmException|UnsupportedEncodingException e) {
            LOG.error("validate()", e);
            throw new RuntimeException("internal error: "+e.getMessage(), e);
        } // try/catch
    } // validate()

} // SimpleAuthenticator
