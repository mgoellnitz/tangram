Tangram Dynamic Extendable Web Applications
==============================

Tangram is a framework for the object oriented web rendering of java beans. 
It comes with limited CMS functionality by using JDO as a source and presenting 
a more or less generic editor for that situation. JDO can be used in conjunction 
with Google App Engine or stand alone with relational database systems. 
Additionally the use of CoreMedia CMS repositories as data source is possible.

The emphasis of the latest work and releases is towards dynamic templating, 
continous enhancement of web applications. As a result Object Oriented Templating 
can not only be used with static JSP Files but also with Apache Velocity code placed 
in the repository. Additionally the base bean classes can be dynamically extended 
by Groovy codes in the repository.

Tangram not only allows for the convenient presentation of content buts also the 
implementation of actions to be called via URLs like e.g. for AJAX or RESTful services 
by use of custom annotations in Java or again Groovy extensions.

Two german short articles about that can be found here:

http://qiqiaoban.blogspot.de/2012/12/on-fly-url-formate-dynamisch-anpassen.html

http://qiqiaoban.blogspot.de/2012/12/nie-wieder-keinen-shim-mer.html

Examples are presented for JDO with RDBMS (SQLite is used for the example contens), 
JDO on Google App Engine, and CoreMedia CMS's most simple example application.

HOW-TO

To make all this work you need

- Java 6 or 7
- Gradle 1.6, 1.7, or 1.8

To make things easier for applications using this framework we provide a gradle plugin now. This means that you now have to prepare that first:

```bash
cd gradle-plugin
gradle
```

For all the rest simply type

```bash
gradle
```

which again automatically expands to

```bash
gradle clean publishToMavenLocal
```

and then you can build and use your projects using tangram or the examples

Optionally you might need Google App Engine but just if you want to use it for your target systems
(Yes, you will want to install java AND python version)

Eclipse preparation:

```bash
gradle eclipse
```
  
(output folders are set to .../build/classes/main not to .../bin)

Changes 0.9

0.9 is a code cleanup and refactoring version to get a starting point for other backend and platform solutions. 

The named jdo persistence manager factory renamed from unintuitive "transaction-optional" to "tangram". You will need to change your jdoconfig.xml unless you are using Google App Engine.

Users of relational database systems will have to get rid of their tangram-rdbms dependencies for library and web archives. Everything that's needed moved to the mere library dependecy nucleus for all datanucleus based scenarios, all other parts moved to core or jdo layer respectively. So an rdbms project will need the tanram-nucleus library compile dependency and the tangram-jdo war dependency instead of tangram-rdbms in both cases in the past.

The security aspects have now been renamed from 'solution' to 'feature' which might result in a inheritance change and template renaming. 

The MimedBlob stuff now also is called a 'feature' and moved to the core package.

The code level now is lifted to Java 7 since the problems with the datanucleus enhancer seem to have vanished
