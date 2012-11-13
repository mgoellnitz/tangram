SUMMARY

Tangram is a framework for the object oriented web rendering of java beans. 
It comes with limited CMS functionality by using JDO as a source and presenting 
a more or less generic editor for that situation. JDO can be used in conjunction 
with Google App Engine or stand alone with relational database systems.

Examples are presented for JDO with RDBMS SQLite and another backend system.

HOW-TO

To make all this work you need

- Java 6 (Update 26 maximum - 32 or 64 bit)
- Gradle 1.2

simply type

gradle clean upload

and then you can build and use your projects using tangram or the examples

And Optionally Google App Engine but just if you want to use it for your target systems
(Yes, you will want to install java AND python version)

Eclipse preparation:

  gradle eclipse
  
(output folders are set to .../build/classes/main not to .../bin)

Add standard-1.1.2.jar to the classpath of jdo, rdbmd, gae, and coma projects
