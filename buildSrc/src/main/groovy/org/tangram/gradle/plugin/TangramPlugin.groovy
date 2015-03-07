/**
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.gradle.plugin;

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin base module.
 * 
 * A plugin especially usefull for tangram projects but also for other
 * projects using JPA, JDO, or EBean
 */
class TangramPlugin implements Plugin<Project> {

  public void apply(Project project) {
    def utilities = new TangramUtilities(project)
    project.convention.plugins.utilities = utilities
    project.extensions.create('versions', TangramVersions)
    
    project.getConfigurations().create('webapp').setVisible(false).setDescription("Wars to be added.");
    
    def cjIterator = project.getTasksByName('compileJava', true).iterator()
    if(cjIterator.hasNext()) {
      def compileJava = cjIterator.next();
      compileJava.doLast {
        def jarPath = project.getConfigurations().getByName('runtime').asPath
        def persistenceAPI = ''
        def jpaBackend = ''
        if (jarPath.indexOf('jdo-api') > 0) { persistenceAPI = 'jdo' }
        if (jarPath.indexOf('javax.persistence') > 0) { persistenceAPI = 'jpa' }
        if (jarPath.indexOf('ebean') > 0) { persistenceAPI = 'ebean' }
        if (jarPath.indexOf('datanucleus') > 0) { jpaBackend = 'datanucleus' }
        // println "API: $persistenceAPI"
        // println "JPA: $jpaBackend"
        if (persistenceAPI == 'jpa') {
          if (jpaBackend == 'datanucleus') {
            println "Performing DataNucleus JPA byte code transformation."
            utilities.nucleusJpaEnhance()
          }
        }
        if (persistenceAPI == 'jdo') {
          println "Performing DataNucleus JDO byte code transformation."
          utilities.nucleusJdoEnhance()
        }
        if (persistenceAPI == 'ebean') {
          println "Performing EBean byte code transformation."
          utilities.ebeanEnhance()
        }
      }
      
      def jar = project.getTasksByName('jar', true).iterator().next();
      // jar.enabled = true
      jar.doFirst { 
        def jarPath = project.getConfigurations().getByName('runtime').asPath
        def persistenceAPI = ''
        def jpaBackend = ''
        if (jarPath.indexOf('javax.persistence') > 0) { persistenceAPI = 'jpa' }
        if (jarPath.indexOf('org.eclipse.persistence.core') > 0) { jpaBackend = 'eclipselink' }
        if (jarPath.indexOf('openjpa') > 0) { jpaBackend = 'openjpa' }
        def byteCodeTransform = (persistenceAPI != 'jpa')
        if (jarPath.indexOf('-eclipselink.jar') > 0) { byteCodeTransform = true }
        if (jarPath.indexOf('-openjpa.jar') > 0) { byteCodeTransform = true }  
        // println "API: $persistenceAPI"
        // println "JPA: $jpaBackend"
        // println "enhance: $byteCodeTransform"
        if (persistenceAPI == 'jpa') {
          if (byteCodeTransform) {
            if (jpaBackend == 'openjpa') {
              println "Performing OpenJPA byte code transformation."
              utilities.openjpaEnhance()
            }
            if (jpaBackend == 'eclipselink') {
              println "Performing EclipseLink byte code transformation."
              utilities.eclipselinkWeave()
            }
          }
        }
      }
    }
    
    def warIterator = project.getTasksByName('war', true).iterator()
    if (warIterator.hasNext()) {
      def war = warIterator.next();
      war.doFirst() {
        utilities.overlayWebapp(war)
      }
    }
  } // apply()
    
} // TangramPlugin
