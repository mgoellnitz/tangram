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
package org.tangram.jdo;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TangramJDO {

	private static Properties PROPERTIES = new Properties();

	/**
	 * TODO: May be it is a good idea to move this to EditingController
	 * 
	 * writable properties which should not be altered by the upper layers or
	 * persisted
	 */
	public static Set<String> SYSTEM_PROPERTIES;

	static {
		// SYSTEM_PROPERTIES:
		SYSTEM_PROPERTIES = new HashSet<String>();
		SYSTEM_PROPERTIES.add("manager");
		SYSTEM_PROPERTIES.add("beanFactory");
		// PROPERTIES:
		try {
			PROPERTIES.load(TangramJDO.class.getClassLoader()
					.getResourceAsStream("tangram/jdo-build.properties"));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} // try/catch
	} // static

	private static final String PROPERTY_VERSION_BUILD = "version.build";

	private static final String VERSION_MAJOR = "0";

	private static final String VERSION_MINOR = "5";

	public static String getVersion() {
		return VERSION_MAJOR + "." + VERSION_MINOR + "."
				+ PROPERTIES.getProperty(PROPERTY_VERSION_BUILD);
	}

} // Constants
