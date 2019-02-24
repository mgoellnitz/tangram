/**
 *
 * Copyright 2013-2019 Martin Goellnitz
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
package org.tangram.gradle.plugin

import org.hibernate.bytecode.enhance.spi.DefaultEnhancementContext;
import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.bytecode.enhance.spi.Enhancer;
import org.hibernate.bytecode.enhance.spi.UnloadedClass;
import org.hibernate.bytecode.enhance.spi.UnloadedField;
import org.hibernate.bytecode.spi.BytecodeProvider
import org.hibernate.cfg.Environment
import org.gradle.api.Project
import org.datanucleus.enhancer.DataNucleusEnhancer
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import io.ebean.enhance.Transformer
import io.ebean.enhance.ant.OfflineFileTransform
import org.eclipse.persistence.tools.weaving.jpa.StaticWeaveProcessor


/**
 * Utility class building the foundation of the tangram plugin.
 * The main utility functions for the hanlding of the needed oberlays and
 * the ORM frameworks are placed here.
 */
class TangramUtilities {

  private Project project

  public TangramUtilities(Project p) {
    project = p
  }


  /**
   * Helper methods to be used for conditional filtering of input files.
   * These filters only work with files - not e.g. with zip entries.
   */
  public isFilterable(input) {
    return (input.displayName.startsWith('file') &&
      (input.name.endsWith('.css') || input.name.endsWith('.js') ||
        input.name.endsWith('.html') || input.name.endsWith('.txt') || input.name.endsWith('.jsp')))
  }

  public isCss(input) {
    return (input.displayName.startsWith('file') && input.name.endsWith('.css'))
  }

  public isJavaScript(input) {
    return (input.displayName.startsWith('file') && input.name.endsWith('.js'))
  }

  /**
   *  Extract all webarchives we depend on and leave out files we'd like to
   *  overwrite with local versions.
   */
  public overlayWebapp(War w) {
    Project p = w.project

    // calculate all files in the local webapp dir for the exclusion from wars
    def exclusion = []
    def base=p.webAppDir.path.length()+1
    FileTree tree = p.fileTree(p.webAppDir)
    tree.each {
      exclusion.add(it.path.substring(base))
    }

    // extract wars from the dependencies as project dependencies or file paths
    // and leave out the exclusions while adding them
    Object iter = p.configurations.webapp.dependencies.iterator()
    String[] archiveFileNames = p.configurations.webapp.asPath.split(File.pathSeparator)
    for (int i = 0; iter.hasNext(); i++) {
      Object webappDependency = iter.next()
      if (webappDependency instanceof org.gradle.api.artifacts.ProjectDependency) {
        String archiveFileName = webappDependency.dependencyProject.war.outputs.files.singleFile.absolutePath
        println "$project.name: project: $webappDependency.dependencyProject.war.outputs.files.singleFile.name"
        w.from (p.zipTree("$archiveFileName")) {
          exclude exclusion
        }
      } else {
        if (p.configurations.webapp.dependencies.size() > 0) {
          String archiveFileName = archiveFileNames[i]
          println "$project.name: path: $archiveFileName"
          int idx = archiveFileName.indexOf(';')
          if (idx >= 0) {
            archiveFileName = archiveFileName.substring(0, idx)
          } // if
          w.from (p.zipTree("$archiveFileName")) {
            exclude exclusion
          }
        } else {
          println "$project.name: ** WARNING: MISSING WAR TO ADD LOCAL FILES TO! **"
        } // if
      } // if
    } // while
  } // overlayWebapp()



  /**
   * Tries to minidy any JavaScript and CSS file in the given archive.
   */
  public minifyArchive(Jar j) {
    j.eachFile {
      if(isCss(it)) {
        it.filter(org.tangram.gradle.plugin.CSSMinify)
      }
      if(isJavaScript(it)) {
        it.filter(org.tangram.gradle.plugin.JavaScriptMinify)
      }
    }
  } // minifyArchive()


  /**
   * Create a classloader from the projects compile dependencies and output path
   */
  private ClassLoader getClassLoader() {
    // collect compile output paths as URLs
    List<URL> urlList = new ArrayList<>()
    project.sourceSets['main'].output.files.each {
      String urlstring = it.toURI().toURL()
      // println "url: $urlstring"
      urlList.add(new URL(urlstring))
    }
    project.sourceSets['test'].output.files.each {
      String urlstring = it.toURI().toURL()
      // println "url: $urlstring"
      urlList.add(new URL(urlstring))
    }
    // Add compile class path elements as urls
    project.configurations.compile.files.each {
      // println "dependency: $it"
      String urlstring = it.toURI().toURL()
      urlList.add(new URL(urlstring))
    }
    // Add test class path elements as urls
    project.configurations.testCompile.files.each {
      String urlstring = it.toURI().toURL()
      urlList.add(new URL(urlstring))
    }
    URL[] urls = urlList.toArray()
    return new URLClassLoader(urls, this.class.classLoader)
  } // getClassLoader()


  /**
   *  Do enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set. An optional output directory can
   *  be given to let the enhancer put the enhanced classes there instead
   *  of overriding the original class files.
   */
  private nucleusEnhance(String api, String dir) {
    try {
      // Collect output paths and files
      List<String> fileList = new ArrayList<>()
      List<URL> urlList = new ArrayList<>()
      project.sourceSets['main'].output.files.each {
        String urlstring = it.toURI().toURL()
        urlList.add(new URL(urlstring))
        project.fileTree(dir: it).each {
          if (it.name.endsWith(".class")) {
            fileList.add(it.canonicalPath)
          }
        }
      }
      project.sourceSets['test'].output.files.each {
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
      project.configurations.testCompile.files.each {
        String urlstring = it.toURI().toURL()
        urlList.add(new URL(urlstring))
      }
      String[] filenames = fileList.toArray()
      URL[] urls = urlList.toArray()
      // Build classloader from path element URLs
      URLClassLoader cl = new URLClassLoader(urls, this.class.classLoader)

      // Instanciate enhancer and pass filename list
      DataNucleusEnhancer enhancer = new DataNucleusEnhancer(api, new Properties())
      if (dir != null) {
        enhancer.setOutputDirectory(dir)
      } // if
      enhancer.setVerbose(true)
      enhancer.setSystemOut(true)
      enhancer.addFiles(filenames)
      enhancer.setClassLoader(cl)
      // println "enhancing $filenames"
      int numClasses = enhancer.enhance()
      println "$numClasses classes enhanced."
    } catch(Exception e) {
      println ''
      e.printStackTrace(System.out)
      throw new GradleException('An error occurred enhancing persistence capable classes.', e)
    } // try/catch
  } // nucleusEnhance()


  /**
   *  Do JDO enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set.
   */
  public nucleusJdoEnhance() {
    nucleusEnhance("JDO", null)
  } // nucleusJdoEnhance()


  /**
   *  Do JPA enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set.
   */
  public nucleusJpaEnhance() {
    nucleusEnhance("JPA", null)
  } // nucleusJpaEnhance()


  /**
   *  Do JPA enhancement with datanucleus enhancer of classes from the
   *  callers javaCompile output set to the given target directory.
   */
  public nucleusJpaEnhance(String dir) {
    nucleusEnhance("JPA", dir)
  } // nucleusJpaEnhance()


  /**
   * Call the OpenJPA Enhancer as an ant task. OpenJPA must be available
   * from the callers runtime classpath, which is the case for any project
   * using OpenJPA.
   *
   * The target directory parameter dir is optional. If present the enhanced
   * classes will be placed in the given directory otherwise the original
   * classes get overridden.
   *
   * An optional persistence.xml can be placed in the subfolder META-INF folder
   * of an 'enhance/' folder in the project's root directory which then is only
   * used during the enhancement process. This helps when having multiple jars
   * with enhanced classes while only one of them may later contain the
   * persistence.xml in effect for the deployed system.
   */
  public openjpaEnhance(String dir) {
    String cp = project.sourceSets['main'].runtimeClasspath.asPath+File.pathSeparator+"${project.projectDir}/enhance"
    project.ant.taskdef(
      name : 'enhance',
      classpath : cp,
      classname : 'org.apache.openjpa.ant.PCEnhancerTask'
    )
    if (dir != null) {
      project.ant.enhance(classpath : cp, directory : dir) {
        fileset(dir: project.sourceSets['main'].java.outputDir.canonicalPath)
      }
    } else {
      project.ant.enhance(classpath : cp) {
        fileset(dir: project.sourceSets['main'].java.outputDir.canonicalPath)
      }
    } // if
  } // openjpaEnhance()


  /**
   * Call the EclipseLink Weaver for the callers classes before packaging a jar.
   * You may provide an output directory for the woven classes. If this parameter
   * is missing or null the original classes will be overridden.
   */
  public eclipselinkWeave(String dir) {
    try {
      URLClassLoader cl = getClassLoader()

      if (dir == null) {
        dir = project.sourceSets['main'].java.outputDir.canonicalPath
      } // if
      StaticWeaveProcessor p = new StaticWeaveProcessor(project.sourceSets['main'].java.outputDir.canonicalPath, dir)
      String persistenceXml = "${project.projectDir}/weave"
      // println "persistence.xml: $persistenceXml"
      File pxml = new File("$persistenceXml/META-INF/persistence.xml")
      if (pxml.exists()) {
        p.setPersistenceInfo(persistenceXml)
      } else {
        p.setPersistenceInfo(project.sourceSets['main'].output.resourcesDir.canonicalPath)
      } // if
      p.setClassLoader(cl)
      p.performWeaving()
    } catch(Exception e) {
      println ''
      e.printStackTrace(System.out)
      throw new GradleException('An error occurred weaving entity classes.', e)
    } // try/catch
  } // eclipselinkWeave()


  private void writeEnhancedClassFile(byte[] enhancedBytecode, File file) {
    try {
      if (file.delete()) {
        if (!file.createNewFile()) {
          println "Could not create class file "+file.name
        }
      } else {
        println "Could not delete class file "+file.name
      }
    } catch (IOException e) {
      println "Could not prepare file for enhanced class "+file.name
    }

    try {
      FileOutputStream outputStream = new FileOutputStream(file, false)
      try {
        outputStream.write(enhancedBytecode)
        outputStream.flush()
      } catch (IOException e) {
        throw new GradleException("Error writing enhanced class to file "+file.absolutePath, e)
      } finally {
        try {
          outputStream.close()
        } catch (IOException ignore) {
        }
      }
    } catch (FileNotFoundException e) {
      throw new GradleException("Error writing to class file "+file.absolutePath, e)
    }
  }


  private void hibernateEnhance(String dir) {
    if (dir == null) {
      dir = project.sourceSets['main'].java.outputDir.canonicalPath
    } // if
    ClassLoader classLoader = getClassLoader()
    EnhancementContext enhancementContext = new DefaultEnhancementContext() {
      @Override
      public ClassLoader getLoadingClassLoader() {
        return classLoader
      }
      @Override
      public boolean doBiDirectionalAssociationManagement(UnloadedField field) {
        return true
      }
      @Override
      public boolean doDirtyCheckingInline(UnloadedClass classDescriptor) {
        return true
      }
      @Override
      public boolean hasLazyLoadableAttributes(UnloadedClass classDescriptor) {
        return true
      }
      @Override
      public boolean isLazyLoadable(UnloadedField field) {
        return true
      }
      @Override
      public boolean doExtendedEnhancement(UnloadedClass classDescriptor) {
        return false;
      }
    };
    Properties p = new Properties();
    p.setProperty(Environment.BYTECODE_PROVIDER, Environment.BYTECODE_PROVIDER_NAME_BYTEBUDDY);
    BytecodeProvider bytecodeProvider = Environment.buildBytecodeProvider(p);
    Enhancer enhancer = bytecodeProvider.getEnhancer(enhancementContext);
    FileTree fileTree = project.fileTree(dir)
    def base = dir.length() + 1
    for (File file : fileTree) {
      if (file.name.endsWith(".class")) {
        def path = file.absolutePath
        String className = path.substring(base, path.length()-".class".length()).replace(File.separator, '.')
        // print "class: $className file: $file.name dir: $dir\n"
        ByteArrayOutputStream originalBytes = new ByteArrayOutputStream()
        FileInputStream fileInputStream = new FileInputStream(file)
        try {
          byte[] buffer = new byte[1024]
          int length
          while ((length = fileInputStream.read(buffer)) != -1) {
            originalBytes.write(buffer, 0, length)
          }
        }
        finally {
          fileInputStream.close()
        }
        byte[] enhancedBytecode = enhancer.enhance(className, originalBytes.toByteArray())
        if (enhancedBytecode != null) {
          writeEnhancedClassFile(enhancedBytecode, file)
        }
      }
    }
  }


  /**
   * Call the Ebean Enhancer as an ant task. Ebean must be available
   * from the callers runtime classpath.
   */
  public ebeanEnhance() {
    URLClassLoader cl = getClassLoader()

    String transformArgs = "debug=1"
    Transformer t = new Transformer(cl, transformArgs)
    // println "                  path: $project.configurations.compile.asPath"
    String classSource = null
    project.sourceSets['main'].output.files.each {
      if (classSource == null) {
        classSource = it.absolutePath
      } // if
    }
    OfflineFileTransform ft = new OfflineFileTransform(t, cl, classSource)
    ft.process(null)
  } // ebeanEnhance()


  /**
   * Call the Ebean Enhancer for the test classes of the caller as an ant task.
   * Ebean must be available from the callers runtime classpath.
   */
  public ebeanEnhanceTest() {
    URLClassLoader cl = getClassLoader()

    String transformArgs = "debug=1"
    Transformer t = new Transformer(cl, transformArgs)

    String classSource = null
    project.sourceSets['test'].output.files.each {
      if (classSource == null) {
        classSource = it.absolutePath
      } // if
    }
    OfflineFileTransform ft = new OfflineFileTransform(t, cl, classSource)
    ft.process(null)
  } // ebeanEnhanceTest()

} // TangramUtilities
