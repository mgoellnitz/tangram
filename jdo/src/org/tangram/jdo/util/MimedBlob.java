/**
 * 
 * Copyright 2012 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.jdo.util;

/**
 * 
 * Convenience interface to describe blobs with mimetypes to be output via HTTP in a common way and with a common
 * relative expiry date in the future configured via the project's property file as image.cache.time in milliseconds
 * which defaults to seven day (10080ms).
 * 
 */
public interface MimedBlob {

    /**
     * @return assiciated mime type of the underlaying data
     */
    String getMimeType();


    /**
     * @return return contents as bytes to be directly passed via http
     */
    byte[] getBytes();

} // MimedBlob
