/*
 * 
 * Copyright 2013-2014 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.gradle.plugin;

/**
 *  Version constants for the components used in tangram and (optionally but reommended) 
 *  the applications using it. 
 *  
 *  By the use of the constants from within build files a consistent library set is ensured. 
 */
public class TangramVersions {

  String servlet = '2.5'
  String jsp = '2.0'
  String groovy = '2.3.0'
  String velocity = '1.7'
  String slf4j = '1.7.7'
  String log4j = '1.2.17'
  String yui = '2.4.7'
  String junit = '4.11'
  String xstream = '1.4.7'
  
  String dinistiq = '0.2-SNAPSHOT'
  String shiro = '1.2.3'

  String springframework = '4.0.5.RELEASE'
  String springsecurity = '3.2.4.RELEASE'
  
  String jdo_api = 'javax.jdo:jdo-api:3.0.1'
  String persistence_api = 'org.eclipse.persistence:javax.persistence:2.1.0'
  
  String openjpa = '2.3.0'
  String eclipselink = '2.5.2'
  
  // Datanucleus Version limited to 3.1.x by Google App Engine plugin for now
  String datanucleus = '3.1.3'
  // The byte code enhancer is not included in every version for some reason
  String datanucleus_enhancer = '3.1.1'
  // Latest version presented by google is 2.1.2 - this version her is provided
  // by datanucleus for plattform version 3.3
  // String datanucleus_appengine = '3.0.0-20140128'
  String datanucleus_appengine = '2.1.2'
  
  String ebean = '4.1.1'
  String ebean_agent = '4.1.4'

  String appengine = '1.9.7'
  
} // TangramVersions
