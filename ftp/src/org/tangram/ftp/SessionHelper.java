/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
package org.tangram.ftp;

import org.mockftpserver.core.session.Session;


/**
 *
 * Constants to be used as keys for the session store of an ftp session and some static helper methods.
 *
 */
public final class SessionHelper {

    public static final String CURRENT_DIR = "tangram.working.dir";

    public static final String RENAME_ID = "tangram.id.to.rename";

    public static final String USER = "tangram.user.name";

    private SessionHelper() {
    }


    /**
     * Get current working directory for ftp service.
     *
     * @param session server session for a client to discover current working directory for
     * @return working director of the given session - avoids null results
     */
    public static String getCwd(Session session) {
        Object attributeValue = session.getAttribute(CURRENT_DIR);
        return attributeValue==null ? "/" : ""+attributeValue;
    } // getCwd()


    public static String getDirectoy(Session session) {
        String dir = getCwd(session);
        int idx = dir.indexOf('/', 1);
        return (idx > 0) ? dir.substring(1, idx) : dir.substring(1);
    } // getDirectoy()

} // SessionHelper
