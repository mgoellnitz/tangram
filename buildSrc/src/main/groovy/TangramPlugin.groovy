/**
 * 
 * Copyright 2013 Martin Goellnitz
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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException

class TangramPlugin implements Plugin<Project> {

    public void apply(Project project) {
        project.convention.plugins.utilities = new TangramUtilities(project)
        project.extensions.create('versions', TangramVersions)
    } // apply()
    
    public static void main(String[] args) {
    }

} // TangramPlugin


class TangramUtilities {

    private Project project
  
    public TangramUtilities(Project p) {
        project = p
    } 

    public jdoEnhance() {
        try {
            project.ant.taskdef(name: 'enhance', classpath: project.configurations.providedCompile.asPath, classname: 'org.datanucleus.enhancer.tools.EnhancerTask')
            project.ant.enhance(failonerror: true, verbose: true, checkonly: false, dir: project.sourceSets['main'].output.classesDir.canonicalPath) {
                classpath {
                    // for the log configuration...
                    pathelement(path: '.')
                    // The classes to be enhanced need to be on the class path
                    pathelement(path: project.sourceSets['main'].output.classesDir.canonicalPath)
                    // this is the real class path for the tool (s.a.)
                    pathelement(path: project.configurations.providedCompile.asPath)
                    // With mere jar libs this is still not complete and enough:
                    pathelement(path: project.sourceSets['main'].compileClasspath.asPath)
                }
                fileset(dir: project.sourceSets['main'].output.classesDir.canonicalPath)
            }
        } catch(Exception e) {
            println ''
            e.printStackTrace(System.out);
            throw new GradleException('An error occurred enhancing persistence capable classes.', e)
        } // try/catch
    } // jdoEnhance()


    public jpaWeave() {
        try {
            String jarPath = project.jar.outputs.files.asPath
            String tempPath = jarPath.replace('.jar', '-unweaved.jar')
            println "libs: ${jarPath}"
            File o = new File(jarPath)
            o.renameTo(new File(tempPath))
            /*
            String persistenceXml = project.configurations.providedCompile.asPath;
            project.sourceSets['main'].resources.each() {
                if (it.name.endsWith('persistence.xml')) persistenceXml = it.absolutePath
            }
            persistenceXml = persistenceXml.substring(0, persistenceXml.length()-'META-INT/persistence.xml'.length())
            println "persistence.xml: $persistenceXml"
            */
            project.ant.taskdef(name: 'weave', 
                                classpath: project.configurations.compile.asPath, 
                                classname: 'org.eclipse.persistence.tools.weaving.jpa.StaticWeaveAntTask')
            // project.ant.weave(source: project.sourceSets['main'].output.classesDir.canonicalPath, 
            //                  target: project.sourceSets['main'].output.classesDir.canonicalPath,
            //                  persistenceinfo: persistenceXml) {
            project.ant.weave(source: tempPath, target: jarPath) {
                classpath {
                    // The classes to be enhanced need to be on the class path
                    // pathelement(path: project.sourceSets['main'].output.classesDir.canonicalPath)
                    // this is the real class path for the tool (s.a.)
                    pathelement(path: project.configurations.compile.asPath)
                    // With mere jar libs this is still not complete and enough:
                    pathelement(path: project.sourceSets['main'].compileClasspath.asPath)
                }
            }            
        } catch(Exception e) {
            println ''
            e.printStackTrace(System.out);
            throw new GradleException('An error occurred weaving entity classes.', e)
        } // try/catch
    } // jpaWeave()

} // TangramUtilities
