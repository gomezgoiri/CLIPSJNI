CLIPSJNI demos
==============

This directory contains four different CLIPSJNI usage examples/demos:

 * AnimalDemo
 * AutoDemo
 * SudokuDemo
 * WineDemo


Requirements
------------

The demos are _mavenized_ too.
This means that [Maven](http://maven.apache.org) must be installed.

Furthermore, install CLIPSJNI in your Maven local repository before compiling and/or executing any of the demos.


Compile a demo
--------------

Go to the demo's top folder and simply run:

    mvn compile

Or to recompile it from scratch:

    mvn clean compile


Running a demo
--------------

Go to the demo's top folder and simply run:

    mvn exec:java
