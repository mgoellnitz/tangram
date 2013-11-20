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
import org.datanucleus.enhancer.DataNucleusEnhancer
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.bundling.War
import com.avaje.ebean.enhance.agent.Transformer
import com.avaje.ebean.enhance.ant.OfflineFileTransform


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
    
  
  /*
   *  extract base webarchive which must be the first webapp dependency of this project.
   *  Then copy JavaScript and CSS Codes and try to minify them.
   *  
   */
  public overlayWebapp(War w) {
    Project p = w.project
        
    // Strange way of overwriting things - it must be the first webapp dependency
    Object iter = p.configurations.webapp.dependencies.iterator()
    int i = 0;
    while (iter.hasNext()) {
      Object webappDependency = iter.next()
      // println "$project.name: dependency: $webappDependency"
      if (webappDependency instanceof org.gradle.api.artifacts.ProjectDependency) {
        String archiveFileName = webappDependency.dependencyProject.war.outputs.files.singleFile.absolutePath
        println "$project.name: project: $webappDependency.dependencyProject.war.outputs.files.singleFile.name"
        p.ant.unzip(src: archiveFileName, dest: "$p.buildDir/target")  
      } else {
        // println "checking for path based extraction"
        if (p.configurations.webapp.dependencies.size() > 0) {
          // println "path is $p.configurations.webapp.asPath"
          String[] archiveFileNames = p.configurations.webapp.asPath.split(File.pathSeparator)
          String archiveFileName = archiveFileNames[i];
          println "$project.name: path: $archiveFileName"
          int idx = archiveFileName.indexOf(';')
          if (idx >= 0) {
            archiveFileName = archiveFileName.substring(0, idx)
          } // if
          p.ant.unzip(src: archiveFileName, dest: "$p.buildDir/target")  
        } else {
          println "$project.name: ** WARNING: MISSING WAR TO ADD LOCAL FILES TO! **"
        } // if
      } // if 
      i++;
    } // while
        
    // The next three are for the tangram system where codes reside in webapp
    p.copy {
      from 'webapp'
      into "$p.buildDir/target"
      include '**/**'
      exclude '**/*.css'
    }
    p.copy {
      from 'webapp'
      into "$p.buildDir/target"
      include '**/*.js'
      exclude 'editor/ckeditor/**'
      filter(JavaScriptMinify)
    }
    p.copy {
      from 'webapp'
      into "$p.buildDir/target"
      include '**/*.css'
      filter(CSSMinify)
    }
    // for standard layout applications use these subdirectories
    p.copy {
      from 'src/main/webapp'
      into "$p.buildDir/target"
      include '**/**'
      exclude '**/*.css'
    }
    p.copy {
      from 'src/main/webapp'
      into "$p.buildDir/target"
      include '**/*.js'
      filter(JavaScriptMinify)
    }
    p.copy {
      from 'src/main/webapp'
      into "$p.buildDir/target"
      include '**/*.css'
      filter(CSSMinify)
    }
    // and now move it to the web archive
    w.into ('') {
      from "$p.buildDir/target"
      exclude 'WEB-INF/lib/**'
    }
  } // overlayWebapp()

  
  /**
   * customize war dependencies for non-clean build intended for system build
   */
  public customizeWar(War w) {
    Project p = w.project
    // For some reason webapp files are not included in the inputs collection
    FileTree tree = p.fileTree(dir: 'webapp')
    w.inputs.files tree
    /* And also the web-archive of the upstream project has to be added
     * so that no classes subdirectory is present and the lib directory 
     * is nearly empty.
     */
    Object iter = p.configurations.webapp.dependencies.iterator()
    while (iter.hasNext()) {
      Object webappDependency = iter.next()
      // println "$project.name: dependency: $webappDependency"
      if (webappDependency instanceof org.gradle.api.artifacts.ProjectDependency) {
        String archiveFile = webappDependency.dependencyProject.war.outputs.files.singleFile
        println "$project.name: adding project dependency: $archiveFile.name"
        w.inputs.file archiveFile
      } else {
        println "$project.name: ** WARNING: MISSING WAR TO ADD LOCAL FILES TO! **"
      } // if 
    } // while
    w.classpath = p.jar.outputs.files
  } // customizeWar()

  
  /**
   *  Do JDO enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set.
   */
  public nucleusJdoEnhance() {
    nucleusEnhance("JDO")
  } // nucleusJdoEnhance()
  
  
  /**
   *  Do JPA enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set.
   */
  public nucleusJpaEnhance() {
    nucleusEnhance("JPA")
  } // nucleusJpaEnhance()
  
  
  /**
   *  Do enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set.
   */
  private nucleusEnhance(String api) {
    try {
      // Collect output paths and files
      List<String> fileList = new ArrayList<String>()
      List<URL> urlList = new ArrayList<URL>()
      project.sourceSets['main'].output.files.each {
        String urlstring = it.toURI().toURL()
        urlList.add(new URL(urlstring))
        project.fileTree(dir: it).each {
          if (it.name.endsWith(".class")) {
            fileList.add(it.canonicalPath)
          } 
        }
      }
      // Add compile classpath
      project.configurations.compile.files.each {
        String urlstring = it.toURI().toURL()
        urlList.add(new URL(urlstring))
      }
      String[] filenames = fileList.toArray()
      URL[] urls = urlList.toArray();      
      // Build classloader from path element URLs
      URLClassLoader cl = new URLClassLoader(urls, this.class.classLoader)
      
      // Instanciate enhancer and pass filename list
      DataNucleusEnhancer enhancer = new DataNucleusEnhancer(api)
      enhancer.setVerbose(true)
      enhancer.setSystemOut(true)
      enhancer.addFiles(filenames)
      enhancer.setClassLoader(cl)
      // println "enhancing $filenames"
      int numClasses = enhancer.enhance();
      println "$numClasses classes enhanced."
    } catch(Exception e) {
      println ''
      e.printStackTrace(System.out);
      throw new GradleException('An error occurred enhancing persistence capable classes.', e)
    } // try/catch
  } // nucleusEnhance()
  
  
  /**
   * Call the OpenJPA Enhancer as an ant task. OpenJPA must be available
   * from the callers runtime classpath.
   */
  public openjpaEnhance() {
    project.ant.taskdef(
      name : 'enhance',
      classpath : project.runtimeClasspath.asPath,
      classname : 'org.apache.openjpa.ant.PCEnhancerTask'
    )
    project.ant.enhance(classpath : project.runtimeClasspath.asPath) {
      fileset(dir: project.sourceSets['main'].output.classesDir.canonicalPath)
    }
  } // openjpaEnhance()


  /**
   * Call the EclipseLink Weaver for the callers jars as an ant tasks. 
   * EclipseLink must be available from the callers runtime classpath.
   */
  public eclipselinkWeave() {
    try {
      String jarPath = project.jar.outputs.files.asPath
      String tempPath = jarPath.replace('.jar', '-unwoven.jar')
      File o = new File(jarPath)
      o.renameTo(new File(tempPath))
      project.ant.taskdef(name: 'weave', 
        classpath: project.configurations.compile.asPath, 
        classname: 'org.eclipse.persistence.tools.weaving.jpa.StaticWeaveAntTask')
      project.ant.weave(source: tempPath, target: jarPath) {
        classpath {
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
  } // eclipselinkWeave()

  
  /**
   * Call the EclipseLink Weaver for the callers classes before packaging a jar.
   * This is quite similar to the JDO case except that a specific persistence.xml
   * is used which must reside in the weave/ subdirectory of the caller project.
   */
  public internalJpaWeave() {
    try {
      String persistenceXml = "${project.projectDir}/weave"
      println "persistence.xml: $persistenceXml"
      project.ant.taskdef(name: 'weave', 
        classpath: project.configurations.compile.asPath, 
        classname: 'org.eclipse.persistence.tools.weaving.jpa.StaticWeaveAntTask')
      project.ant.weave(source: project.sourceSets['main'].output.classesDir.canonicalPath, 
        target: project.sourceSets['main'].output.classesDir.canonicalPath,
        persistenceinfo: persistenceXml) {
        classpath {
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
  } // internalJpaWeave()
    
  
  /**
   * Call the OpenJPA Enhancer as an ant task. OpenJPA must be available
   * from the callers runtime classpath.
   */
  public ebeanEnhance() {
    // collect compile output paths as URLs
    List<URL> urlList = new ArrayList<URL>()
    String classSource = null
    project.sourceSets['main'].output.files.each {
      String urlstring = it.toURI().toURL()
      if (classSource == null) {
        classSource = it.absolutePath
      } // if
      println "url: $urlstring"
      urlList.add(new URL(urlstring))
    }
    // Add compile class path elements as urls
    project.configurations.compile.files.each {
      String urlstring = it.toURI().toURL()
      urlList.add(new URL(urlstring))
    }
    URL[] urls = urlList.toArray()
    URLClassLoader cl = new URLClassLoader(urls, this.class.classLoader)
    
    String transformArgs = "debug=1"      
    Transformer t = new Transformer(project.configurations.compile.asPath, transformArgs)
    // Class destination has no function
    // String classDestination = "$project.buildDir"
    // println classDestination
    // OfflineFileTransform ft = new OfflineFileTransform(t, cl, classSource, classDestination)
    OfflineFileTransform ft = new OfflineFileTransform(t, cl, classSource, null)
    ft.process(null);
  } // ebeanEnhance()

} // TangramUtilities
