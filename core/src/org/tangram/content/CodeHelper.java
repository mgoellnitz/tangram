/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.content;

import java.util.HashSet;
import java.util.Set;


/**
 * Constant string to be used as keys for the session store of an ftp session.
 */
public class CodeHelper {

    private static final Set<String> EXTENSION_TO_CUT = new HashSet<String>();


    /**
     * a set of ignorable extensions. Tweaked for use with NetBeans FTP uploader
     */
    static {
        EXTENSION_TO_CUT.add(".new");
        EXTENSION_TO_CUT.add(".old");
    }


    /**
     * Avoid ambiguities with mimetypes for javascript and xml.
     * Just for internal use.
     */
    public static String getNormalizedMimeType(String mimeType) {
        if ("application/x-groovy".equals(mimeType)) {
            mimeType = "text/groovy";
        } // if
        if ("application/xml".equals(mimeType)) {
            mimeType = "text/xml";
        } // if
        if ("application/javascript".equals(mimeType)) {
            mimeType = "text/javascript";
        } // if
        return mimeType;
    } // getNormalizedMimeType()


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
        if ("text/plain".equals(mimeType)) {
            mimeType = "";
        } // if
        if (mimeType.startsWith("text/")) {
            mimeType = "."+mimeType.substring(5);
        } // if
        return mimeType;
    } // getExtension()


    public static String getFolder(String mimeType) {
        if ("application/x-groovy".equals(mimeType)) {
            mimeType = "text/groovy";
        } // if
        if ("text/html".equals(mimeType)) {
            mimeType = "text/velocity";
        } // if
        if ("text/xml".equals(mimeType)) {
            mimeType = "text/velocity-xml";
        } // if
        if ("text/javascript".equals(mimeType)) {
            mimeType = "text/js";
        } // if
        if (mimeType.startsWith("text/")) {
            mimeType = mimeType.substring(5);
        } // if
        return mimeType;
    } // getFolder()


    public static String getMimetype(String directoryName) {
        String result = "text/plain";

        if ("velocity".equals(directoryName)) {
            result = "text/html";
        } // if
        if ("velocity-xml".equals(directoryName)) {
            result = "text/xml";
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
    public static String getAnnotation(String filename) {
        int idx = filename.lastIndexOf('.');
        String extension = filename.substring(idx);
        if (EXTENSION_TO_CUT.contains(extension)) {
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

} // CodeHelper
