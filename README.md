Tangram Dynamic Extendable Web Applications
===============================

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
- Gradle 1.6

simply type

```bash
gradle clean publishToMavenLocal
```

and then you can build and use your projects using tangram or the examples

And optionally you might need Google App Engine but just if you want to use it for your target systems
(Yes, you will want to install java AND python version)

Eclipse preparation:

```bash
gradle eclipse
```
  
(output folders are set to .../build/classes/main not to .../bin)

Caveat:

On buildhive current versions of tangram don't build since cloudbees only has gradle 1.4 available 
there and the tangram maven publishing code got cleaned up ot meet gradle 1.6 tooling.
