/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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

import java.io.InputStream;


/**
 * Implementing classes are used to store codes in the repository represented by the bean factory.
 */
public interface CodeResource extends Content {

    /**
     * Annotation indicating the use of the code text.
     * Dependends on the mime type of the code.
     *
     * @return annotation value
     */
    String getAnnotation();


    /**
     * Mime type of the code itself or the result produced for template codes.
     *
     * @return mime type - excluding encoding
     */
    String getMimeType();


    /**
     * Get modification time of the code.
     *
     * @return modification time in milliseconds since the epoche
     */
    long getModificationTime();


    /**
     * Return the code text.
     *
     * @return code text string
     */
    String getCodeText();


    /**
     * Return the size of the code text.
     *
     * @return size of the code text in characters
     */
    long getSize();


    /**
     * Read the code as a stream.
     *
     * @return stream of the code text
     * @throws Exception mostly IO Exceptions
     */
    InputStream getStream() throws Exception;

} // CodeResource
