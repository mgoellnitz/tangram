/**
 *
 * Copyright 2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.ftp;

import org.mockftpserver.core.session.Session;


/**
 * Constant string to be used as keys for the session store of an ftp session.
 */
public class SessionHelper {

    public static final String CURRENT_DIR = "tangram.working.dir";

    public static final String RENAME_ID = "tangram.id.to.rename";

    public static final String USER = "tangram.user.name";


    /**
     * get current working directory for ftp serivce.
     *
     * avoids null results
     */
    public static String getCwd(Session session) {
        Object attributeValue = session.getAttribute(CURRENT_DIR);
        return attributeValue==null ? "/" : ""+attributeValue;
    } // getCwd()


    public static String getDirectoy(Session session) {
        String dir = getCwd(session);
        dir = dir.substring(1);
        int idx = dir.indexOf('/');
        if (idx>0) {
            dir = dir.substring(0, idx);
        } // if
        return dir;
    } // getDirectoy()


    public static String getExtension(String mimeType) {
        if ("application/x-groovy".equals(mimeType)) {
            mimeType = "text/groovy";
        } // if
        if ("text/html".equals(mimeType)) {
            mimeType = "text/vtl";
        } // if
        if ("text/xml".equals(mimeType)) {
            mimeType = "text/vtl";
        } // if
        if ("text/javascript".equals(mimeType)) {
            mimeType = "text/js";
        } // if
        if (mimeType.startsWith("text/")) {
            mimeType = mimeType.substring(5);
        } // if
        return mimeType;
    } // getExtension()


    public static String getMimetype(String directoryName) {
        String result = "text/plain";

        // TODO: how to distinguish XML templates
        if ("vtl".equals(directoryName)) {
            result = "text/html";
        } // if
        if ("css".equals(directoryName)) {
            result = "text/css";
        } // if
        if ("js".equals(directoryName)) {
            result = "text/javascript";
        } // if
        if ("groovy".equals(directoryName)) {
            result = "application/x-groovy";
        } // if

        return result;
    } // getMimetype()


    /**
     * derives annotation from pseudo filename.
     */
    // TODO: same usage for codes.zip
    public static String getAnnotation(String filename) {
        int idx = filename.lastIndexOf('.');
        // TODO: the use of these two should be switchable
        if (filename.endsWith(".new")) {
            filename = filename.substring(0, idx);
        } // if
        if (filename.endsWith(".old")) {
            filename = filename.substring(0, idx);
        } // if
        idx = filename.lastIndexOf('.');
        if (filename.endsWith(".vtl")) {
            filename = filename.substring(0, idx);
        } // if
        if (filename.endsWith(".css")) {
            filename = filename.substring(0, idx);
        } // if
        if (filename.endsWith(".js")) {
            filename = filename.substring(0, idx);
        } // if
        if (filename.endsWith(".groovy")) {
            filename = filename.substring(0, idx);
        } // if
        return filename;
    } // getAnnotation()

} // SessionHelper
