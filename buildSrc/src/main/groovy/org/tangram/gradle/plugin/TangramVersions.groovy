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

  // The basics
  String lombok = '1.16.6'
  String servlet_api = 'javax.servlet:javax.servlet-api:3.1.0'
  String jsp_api = 'javax.servlet.jsp:jsp-api:2.2'
  String groovy = '2.4.5'
  String asm = '4.0'
  String velocity = '1.7'
  String slf4j = '1.7.12'
  String log4j = '1.2.17'
  String logback = '1.1.3'
  String yui = '2.4.7'
  String mockito = '1.10.19'
  String xstream = '1.4.8'
  String pac4j = '1.8.0'
  String tomcat = '7.0.63'
  String codemirror = '5.8'
  String ckeditor = '4.5.4'

  // For testing purposes
  String junit = '4.12'
  String testng = '6.3.1' // Gradle 2.7 has 6.3.1 - tests fail with other versions - latest is 6.9.8

  // The dependency injection options
  String dinistiq = '0.5'

  String guice = '4.0'
  String mycila_guice = '3.6.ga'

  String springframework = '4.2.2.RELEASE'

  String weld = '2.3.0.Final'
  String openwebbeans = '1.6.1'

  // Storage options
  String jdo_api = 'javax.jdo:jdo-api:3.1'
  String persistence_api = 'org.eclipse.persistence:javax.persistence:2.1.0'

  String openjpa = '2.4.0'
  String eclipselink = '2.6.1'
  String hibernate = '5.0.0.Final' // '5.0.2.Final'
  String hibernate_ogm = '4.2.0.Final'

  // The last DataNucleus Access Plattform version to include all necessary modules
  // DataNucleus does not provide all artifacts for every released version.
  String datanucleus = '4.1.0-release'

  String ebean = '6.2.2' // 6.9.1 once we resolve issue with example applications https://github.com/ebean-orm/avaje-ebeanorm/issues/435
  String ebean_agent = '4.7.1'

  // Not used by tangram itself
  String mongodb = '2.13.2' // '3.0.3' OpenShift right now has MongoDB 2.4 available

} // TangramVersions
