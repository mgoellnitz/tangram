Tangram Dynamic Extendable Web Applications
===========================================

[![Build Status](https://api.travis-ci.org/mgoellnitz/tangram.svg?branch=master)](https://travis-ci.org/mgoellnitz/tangram)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/3981/badge.svg)](https://scan.coverity.com/projects/3981)

Tangram is a framework for the object oriented web rendering of java beans. 
It comes with limited CMS functionality by using Java Data Object (JDO) or 
Java Persistence API (JPA) implementations as data sources and presenting 
a more or less generic editor for that situation. JDO can be used in conjunction 
with Google App Engine while JDO and JPA are tested with relational database 
systems and MongoDB stand alone or on the OpenShift plattform.
Additionally the use of CoreMedia CMS repositories as data source is possible.

The emphasis of the latest work and releases is towards dynamic templating and 
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

The tangram components can be glued together with several dependency injection frameworks. 
Springframework, dinistiq, and Google Guice work out of the box.

Examples are presented for JDO with RDBMS (hsqldb is used for the example contents), 
JDO on Google App Engine, JPA, EBean (with h2), and CoreMedia CMS's most simple example 
application.

Maven Repositories
------------------

Snapshots and Releases:

https://raw.githubusercontent.com/mgoellnitz/artifacts/master

HOW-TO
======

To make all this work you need

- Java 7 (Java 8 is untested)
- Gradle 2.0 or later (Including 2.2)

Gradle 1.x can only be used, if you recompile dinistiq and tangram with that very gradle
version due to some incompatibilities with the groovy version used.

To make things easier for applications using this framework we provide a gradle plugin now.

To use this plugin seperately - which very much makes sense for applications using JPA, JDO, 
or EBean - you have to build and publish the tangram buildSrc folder.

```bash
cd buildSrc
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

and then you can build and use your projects using tangram or the examples.

Optionally you might need Google App Engine but just if you want to use it 
for your target systems (Yes, you will want to install ths Java AND python version)

Eclipse preparation:

```bash
gradle eclipse
```
(output folders are set to .../build/classes/main not to .../bin)

(We don't recommend using eclipse anymore)
  
Changes 0.9
===========

0.9 is a code cleanup and refactoring version to get a starting point for other 
backend and platform solutions. 

Users of relational database systems will have to get rid of their tangram-rdbms 
dependencies for library and web archives. Everything that's needed moved to the 
mere library dependecy nucleus for all datanucleus based scenarios, all other 
parts moved to core or jdo layer respectively. So an rdbms project will need the 
tanram-nucleus library compile dependency and the tangram-jdo war dependency 
instead of tangram-rdbms in both cases in the past.

The security aspects have now been renamed from 'solution' to 'feature' which 
might result in a inheritance change and template renaming. 

The MimedBlob stuff now also is called a 'feature' and moved to the core package.

TangramServlet resides in a spring package - you will have to change your web.xml

The code level now is lifted to Java 7 since the problems with the datanucleus 
enhancer seem to have vanished.

The editor is an independent module and can - through the middle layer of mutable 
contents - be used for jpa and jdo base layers. Only the GAE flavour integrates 
this directly. For all other scenarios applications will have to add those extra 
two dependencies to switch on the generic editor.

An ftp module has been added to support IDE synchronisation of codes in the 
repository. It's in an early stage but seems to be working at least for netbeans 
quite well. Create a Code resource with annotaion users.properties, mimetpe 
text/plain and user=passwords tuples inside.

Changes 1.0
===========

The upcoming release focuses on the original term "tangram" again, which means 
putting together existing peaces and combine them into a pretty shape.

Tangram now provides choice for nearly any of the parts we put together:

Storage Layer: JDO, JPA, Ebean

Dependency Injction: Springframework, Dinistiq, Guice (CDI)

Templating: JSP and Apache Velocity

If you stick to the internal abstract "handler" and "action" scheme, you can even 
change the underlying implementations at any time, while it obviously is possible
to directly use e.g. guice modules or spring controllers.
