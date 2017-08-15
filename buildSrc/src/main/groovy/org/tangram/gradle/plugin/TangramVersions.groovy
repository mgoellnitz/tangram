/*
 *
 * Copyright 2013-2017 Martin Goellnitz
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
package org.tangram.gradle.plugin;

/**
 *  Version constants for the components used in tangram and (optionally but reommended)
 *  the applications using it.
 *
 *  By the use of the constants from within build files a consistent library set is ensured.
 */
public class TangramVersions {

  // The basics
  String lombok = '1.16.18'
  String servlet_api = 'javax.servlet:javax.servlet-api:3.1.0'
  String jsp_api = 'javax.servlet.jsp:jsp-api:2.2'
  String org_json = '20160810' // not used in tangram internally
  String javax_json = '1.0'
  String gson = '2.7'
  String groovy = '2.4.12'
  String asm = '5.2'
  String velocity = '1.7'
  String slf4j = '1.7.25'
  String log4j = '1.2.17'
  String logback = '1.2.3'
  String yui = '2.4.7'
  String mockito = '2.8.47'
  String xstream = '1.4.10'
  String pac4j = '2.1.0'
  String mockftpserver = '2.7.1'
  String tomcat = '8.0.43'
  String codemirror = '5.27.4'
  String ckeditor = '4.7.0'

  // For testing purposes
  String junit = '4.12'
  String testng = '6.11' // Gradle 4.0 has 6.3.1 - tests using e.g. testng listeners fail with other versions
  String hsqldb = '1.8.1.1'
  String h2db = '1.4.196'

  // The dependency injection options
  String dinistiq = '0.6'

  String guice = '4.1.0'

  String springframework = '4.3.10.RELEASE'

  String weld = '2.4.4.Final'
  String openwebbeans = '2.0.0'

  // Storage options
  String jdo_api = 'org.datanucleus:javax.jdo:3.2.0-m7'
  // There is no official source for a JPA API jar so we decide to use one of them
  // String persistence_api = 'org.eclipse.persistence:javax.persistence:2.1.1'
  String persistence_api = 'org.datanucleus:javax.persistence:2.2.0-release'

  String openjpa = '2.4.2'
  String eclipselink = '2.6.4'
  String hibernate = '5.2.10.Final'
  String hibernate_ogm = '5.1.0.Final'
  String byte_buddy = '[1.6,1.7)'

  String datanucleus = '[5.1,5.2)'

  String ebean = '10.4.2'
  String ebean_datasource = '1.1'
  String ebean_agent = '10.4.1'

  String mongodb = '3.0.2'
  String morphia = '1.3.2'
  
  String objectify = '5.1.15'
  
  String solr = '6.4.1'  

} // TangramVersions
