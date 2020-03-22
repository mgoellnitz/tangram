/*
 *
 * Copyright 2013-2020 Martin Goellnitz
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
  String servlet_api = 'javax.servlet:javax.servlet-api:4.0.1'
  String jsp_api = 'javax.servlet.jsp:jsp-api:2.2.1-b03'
  String groovy = '2.5.8'
  String velocity = '1.7'
  String slf4j = '1.7.30'
  String logback = '1.2.3'
  String yui = '2.4.8'
  String mockito = '3.3.3'
  String xstream = '1.4.11.1'
  String pac4j = '3.8.3'
  String mockftpserver = '2.7.1'
  String tomcat = '8.0.53'
  String codemirror = '5.48.4'
  String ckeditor = '4.12.1'
  
  // Not used internally but recommended
  String lombok = '1.18.12'
  String org_json = '20190722'
  String javax_json = '1.1.4'
  String gson = '2.8.6'

  // For testing purposes
  String testng = '7.1.0' // Gradle still has 6.3.1 - tests using e.g. testng listeners fail with other versions
  String hsqldb = '1.8.0.10' // org.hsqldb group id - newer version fail during test
  String h2db = '1.4.199'

  // The dependency injection options
  String dinistiq = '0.8'

  String guice = '4.2.3'

  String springframework = '4.3.26.RELEASE'
  // String springframework = '5.2.1.RELEASE'

  String weld = '3.1.0.Final'
  String openwebbeans = '2.0.11'

  // Storage options
  String jdo_api = 'org.datanucleus:javax.jdo:3.2.0-m13'
  // There is no official source for a JPA API jar so we decide to use one of them
  // String persistence_api = 'org.eclipse.persistence:javax.persistence:2.1.1'
  String persistence_api = 'org.datanucleus:javax.persistence:2.2.2'

  String openjpa = '3.1.1'
  String eclipselink = '2.7.6'
  String hibernate = '5.4.12.Final'
  String hibernate_ogm = '5.4.1.Final'
  String byte_buddy = '1.9.14'

  String datanucleus = '[5.2,5.3)'

  String ebean = '11.41.1'
  String ebean_datasource = '4.5'
  String ebean_agent = '11.39.1'

  String mongodb = '3.10.1'
  String morphia = '1.3.2'
  
  String objectify = '6.0.3'
  
  String solr = '7.2.0'

} // TangramVersions
