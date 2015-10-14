/*
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
package org.tangram.content;

import java.util.HashSet;
import java.util.Set;
import org.tangram.Constants;


/**
 * Constant string to be used as keys for the session store of an ftp session.
 */
public final class CodeHelper {

    /**
     * a set of ignorable extensions. Tweaked for use with NetBeans FTP uploader
     */
    private static final Set<String> EXTENSION_TO_CUT = new HashSet<>();

    /**
     * a set of mime type used for codes within tangram.
     */
    private static final Set<String> MIME_TYPES = new HashSet<>();


    private CodeHelper() {
    }


    static {
        EXTENSION_TO_CUT.add(".new");
        EXTENSION_TO_CUT.add(".old");

        MIME_TYPES.add(Constants.MIME_TYPE_XML);
        MIME_TYPES.add(Constants.MIME_TYPE_HTML);
        MIME_TYPES.add(Constants.MIME_TYPE_CSS);
        MIME_TYPES.add(Constants.MIME_TYPE_JS);
        MIME_TYPES.add(Constants.MIME_TYPE_GROOVY);
    }


    public static Set<String> getCodeMimeTypes() {
        return MIME_TYPES;
    } // getCodeMimeTypes()


    public static String getExtension(String mimeType) {
        if (Constants.MIME_TYPE_GROOVY.equals(mimeType)) {
            mimeType = "text/groovy";
        } // if
        if (Constants.MIME_TYPE_HTML.equals(mimeType)) {
            mimeType = "text/vtl";
        } // if
        if (Constants.MIME_TYPE_XML.equals(mimeType)) {
            mimeType = "text/vtl";
        } // if
        if (Constants.MIME_TYPE_JS.equals(mimeType)) {
            mimeType = "text/js";
        } // if
        if (Constants.MIME_TYPE_PLAIN.equals(mimeType)) {
            mimeType = "";
        } // if
        if (mimeType.startsWith("text/")) {
            mimeType = "."+mimeType.substring(5);
        } // if
        return mimeType;
    } // getExtension()


    public static String getFolder(String mimeType) {
        if (Constants.MIME_TYPE_GROOVY.equals(mimeType)) {
            mimeType = "text/groovy";
        } // if
        if (Constants.MIME_TYPE_HTML.equals(mimeType)) {
            mimeType = "text/velocity";
        } // if
        if (Constants.MIME_TYPE_XML.equals(mimeType)) {
            mimeType = "text/velocity-xml";
        } // if
        if (Constants.MIME_TYPE_JS.equals(mimeType)) {
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
            result = Constants.MIME_TYPE_HTML;
        } // if
        if ("velocity-xml".equals(directoryName)) {
            result = Constants.MIME_TYPE_XML;
        } // if
        if ("css".equals(directoryName)) {
            result = Constants.MIME_TYPE_CSS;
        } // if
        if ("js".equals(directoryName)) {
            result = Constants.MIME_TYPE_JS;
        } // if
        if ("groovy".equals(directoryName)) {
            result = Constants.MIME_TYPE_GROOVY;
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
