/**
 * 
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.rdbms;

import java.util.Properties;

public class TangramRDBMS {

    private static Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(TangramRDBMS.class.getClassLoader().getResourceAsStream("tangram/rdbms-build.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } // try/catch
    } // static

    private static final String PROPERTY_VERSION_BUILD = "version.build";

    private static final String VERSION_MAJOR = "0";

    private  static final String VERSION_MINOR = "5";
    
    public static String getVersion() {
        return VERSION_MAJOR+"."+VERSION_MINOR+"."+PROPERTIES.getProperty(PROPERTY_VERSION_BUILD);
    }

} // Constants
