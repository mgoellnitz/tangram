/*
 *
 * Copyright 2013-2016 Martin Goellnitz
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
  String lombok = '1.16.8'
  String servlet_api = 'javax.servlet:javax.servlet-api:3.1.0'
  String jsp_api = 'javax.servlet.jsp:jsp-api:2.2'
  String groovy = '2.4.7'
  String asm = '5.1'
  String velocity = '1.7'
  String slf4j = '1.7.21'
  String log4j = '1.2.17'
  String logback = '1.1.7'
  String yui = '2.4.7'
  String mockito = '1.10.19'
  String xstream = '1.4.9'
  String pac4j = '1.8.8'
  String mockftpserver = '2.6'
  String tomcat = '8.0.36'
  String codemirror = '5.16.0'
  String ckeditor = '4.5.9'

  // For testing purposes
  String junit = '4.12'
  String testng = '6.9.12' // Gradle 2.13 has 6.3.1 - tests using e.g. testng listeners fail with other versions
  String hsqldb = '1.8.1.1'
  String h2db = '1.4.192'

  // The dependency injection options
  String dinistiq = '0.5'

  String guice = '4.0'
  String mycila_guice = '3.6.ga'

  String springframework = '4.3.1.RELEASE'

  String weld = '2.3.4.Final'
  String openwebbeans = '1.6.2'

  // Storage options
  String jdo_api = 'javax.jdo:jdo-api:3.1'
  // There is no official source for a JPA API jar so we decide to use one of them
  // String persistence_api = 'org.eclipse.persistence:javax.persistence:2.1.1'
  String persistence_api = 'org.datanucleus:javax.persistence:2.1.2'

  String openjpa = '2.4.1'
  String eclipselink = '2.6.3'
  String hibernate = '5.1.0.Final'
  String hibernate_ogm = '5.0.0.Final'

  // The last DataNucleus Access Plattform version to include all necessary modules
  // DataNucleus does not provide all artifacts for every released version.
  // String datanucleus = '4.1.0-release'
  String datanucleus = '[4.1,4.2)'

  String ebean = '7.12.2'
  String ebean_datasource = '1.1'
  String ebean_agent = '4.11.1'

  String mongodb = '3.0.2'
  String morphia = '1.1.1'

} // TangramVersions
