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
package org.tangram.gradle.plugin;

/**
 *  Version constants for the components used in tangram and (optionally but reommended)
 *  the applications using it.
 *
 *  By the use of the constants from within build files a consistent library set is ensured.
 */
public class TangramVersions {

  String lombok = '1.16.6'
  String servlet_api = 'javax.servlet:servlet-api:2.5'
  String jsp_api = 'javax.servlet:jsp-api:2.0'
  String groovy = '2.4.4'
  String velocity = '1.7'
  String slf4j = '1.7.12'
  String log4j = '1.2.17'
  String yui = '2.4.7'
  String mockito = '1.10.19'
  String xstream = '1.4.8'
  String pac4j = '1.7.1'
  String tomcat = '7.0.63'
  String codemirror = '5.5'
  String ckeditor = '4.5.2'

  // For testing purposes
  String junit = '4.12'
  String testng = '6.3.1' // Gradle 2.6 has 6.3.1 - tests fail with other versions - latest is 6.9.6
  String testspring = '2.5' // Google app engine is quite outdated with Servlet APIs

  String dinistiq = '0.4'

  String springframework = '4.1.7.RELEASE'

  String weld = '2.2.15.Final'
  String openwebbeans = '1.6.1'

  String guice = '4.0'
  String mycila_guice = '3.6.ga'

  String jdo_api = 'javax.jdo:jdo-api:3.1-rc1'
  String persistence_api = 'org.eclipse.persistence:javax.persistence:2.1.0'

  String openjpa = '2.4.0'
  String eclipselink = '2.6.0'
  String mongodb = '2.13.2' // '3.0.3' OpenShift right now has MongoDB 2.4 available
  String hibernate = '5.0.0.Final'
  String hibernate_ogm = '4.2.0.Final'

  // Datanucleus Version limited to 3.1.x by Google App Engine plugin for now
  String datanucleus = '3.1.3'
  // The byte code enhancer is not included in every version for some reason
  String datanucleus_enhancer = '3.1.1'
  // Latest version presented by google is 2.1.2
  String datanucleus_appengine = '2.1.2'

  String ebean = '6.5.1'
  String ebean_agent = '4.5.4'

  String appengine = '1.9.25'

} // TangramVersions
