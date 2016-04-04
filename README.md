Tangram Dynamic Extendable Web Applications
===========================================

[![Build Status](https://api.travis-ci.org/mgoellnitz/tangram.svg?branch=master)](https://travis-ci.org/mgoellnitz/tangram)
[![Coverage Status](https://coveralls.io/repos/github/mgoellnitz/tangram/badge.svg?branch=master)](https://coveralls.io/github/mgoellnitz/tangram?branch=master)

Tangram is a framework comprised of an assembly of proven Java library modules
to form a basis for web applications which need the following core elements:

- Data driven through models expressed in Java
- Easy mapping of URLs to action implementations
- Dynamic extendability along the way (without deployments)
- Present reasonable defaults for any aspect
- Quick path to get something running
- Long path to keep it alive and let the application grow

While the core forms a stable plattform basis for web applications to provide
long time maintainable applications, anything from CSS, JavaScript, URL Scheme,
Business Logic, Actions, to even the Model can be changed at runtime.

It comes with limited CMS functionality by using Java Data Object (JDO), Java
Persistence API (JPA) implementations, or even EBean as data sources and presenting
a more or less generic editor for that situation. JDO can be used in conjunction
with Google App Engine while JDO and JPA are tested with relational database
systems and MongoDB stand alone or on the OpenShift plattform. Additionally the
use of CoreMedia CMS repositories as data source is possible.

The emphasis of the latest work and releases is towards dynamic templating and
continous enhancement of web applications. As a result Object Oriented Templating
can not only be used with static JSP Files but also with Apache Velocity code placed
in the repository. Additionally the base bean classes can be dynamically extended
by Groovy codes in the repository.

Tangram not only allows for the convenient presentation of content but also the
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

[https://github.com/mgoellnitz/tangram-examples](https://github.com/mgoellnitz/tangram-examples]

To make things easier for applications using this framework we provide a Gradle plugin now.

This plugin can be used separately independent of the rest of the framework - which very
much makes sense for applications using JPA, JDO, or EBean, needing some WAR file overlay
("underlying") mechanism, or simply want to have automatic minification of CSS and JavaScript
resources in their webapps folder.

[http://qiqiaoban.blogspot.de/2015/10/gradle-plugin-in-tangram-now-grown-up.html](http://qiqiaoban.blogspot.de/2015/10/gradle-plugin-in-tangram-now-grown-up.html)

A growing set of documentation items can be found in the wiki of this project:

[https://github.com/mgoellnitz/tangram/wiki](https://github.com/mgoellnitz/tangram/wiki)

Maven Repositories
------------------

Releases:

https://jcenter.bintray.com/

Snapshots:

https://raw.githubusercontent.com/mgoellnitz/artifacts/master

HOW-TO
======

To make all this work you need

- Java 7 or 8
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

If you need to set a runtime library to e.g. compile with Java 8 for target
systems with Java 7 you can provide a JDK pointer:

```bash
gradle -Pjdk=/opt/jdk1.7.0_80 clean build publishToMavenLocal
```

Packaging source code and API documentation is triggered by the "release" switch.

```bash
gradle -Prelease clean build publishToMavenLocal
```

Test coverage is checked with the Jacoco plugin. To obtain a combined view of
the test coverage of all modules, you need to call the jacocoCominbedReport
task at the root level. The jacocoTestReport task only works for the individual
modules.

```bash
gradle -Prelease clean build jacocoCombinedReport publishToMavenLocal
```

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

The current release version focuses on the original term "tangram" again, which means
putting together existing peaces and combine them into a pretty shape.

Tangram now provides choice for nearly any of the parts we put together:

Storage Layer: JDO, JPA, Ebean

Dependency Injection: Springframework, Dinistiq, Guice (CDI)

Templating: JSP and Apache Velocity

If you stick to the internal abstract "handler" and "action" scheme, you can even
change the underlying implementations at any time, while it obviously is possible
to directly use e.g. guice modules or spring controllers.
