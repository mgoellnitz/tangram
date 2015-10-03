Tangram Dynamic Extendable Web Applications
===========================================

[![Build Status](https://api.travis-ci.org/mgoellnitz/tangram.svg?branch=master)](https://travis-ci.org/mgoellnitz/tangram)

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

https://github.com/mgoellnitz/tangram-examples

To make things easier for applications using this framework we provide a Gradle plugin now.

This plugin can be used seperately independent of the rest of the framework - which very
much makes sense for applications using JPA, JDO, or EBean, needing some WAR file overlay
("underlying") mechanism, or simply want to have automatic minification of CSS and JavaScript
resources in their webapps folder.

http://qiqiaoban.blogspot.de/2015/10/gradle-plugin-in-tangram-now-grown-up.html

Maven Repositories
------------------

Releases:

https://jcenter.bintray.com/

Snapshots:

https://raw.githubusercontent.com/mgoellnitz/artifacts/master

HOW-TO
======

To make all this work you need

- Java 7 (Java 8 is not working and Java 7 is needed right now for GAE compatibility)
- Gradle 2.2.1 or later

Gradle 1.x can only be used, if you recompile dinistiq and tangram with that very Gradle
version due to some incompatibilities with the groovy version used. Ubuntu LTS users be
warned not use the Gradle version referenced by their Version 12 LTS install.

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

Changes 1.0
===========

The upcoming release focuses on the original term "tangram" again, which means
putting together existing peaces and combine them into a pretty shape.

Tangram now provides choice for nearly any of the parts we put together:

Storage Layer: JDO, JPA, Ebean

Dependency Injection: Springframework, Dinistiq, Guice (CDI)

Templating: JSP and Apache Velocity

If you stick to the internal abstract "handler" and "action" scheme, you can even
change the underlying implementations at any time, while it obviously is possible
to directly use e.g. guice modules or spring controllers.
