CLIPSJNI
========

In this project I made the [following improvements](https://sourceforge.net/p/clipsrules/discussion/776945/thread/01312882) to the original [CLIPSJNI](https://sourceforge.net/projects/clipsrules/) project:

 * More "standardized" package name: _net.sf.clipsrules_.
 * Throwing exceptions instead of only printing errors on the stdout.
 * Inline documentation for Environment class' methods and value classes. The idea is to make Java developers' life easier.
 * Mavenizing the project.
 * Creating an OSGi compliant jar.
 * Changes on Value's classes design.
  * In the 0.3 version _PrimitiveValue_ defined all the methods inherited by the subclasses. Each of these methods throw a generic _Exception_ and were overriden in each subclass. IMHO, this is an ankward use of the polimosphism. Additionally, it forces each method to throw a generic and hardly understandable _Exception_.
  * In the 0.4 version I propose, each Subclass defines its owns methods. Furthermore, I've introduced some new classes to have a one-to-one equivalence with CLIPS' datatypes.


Requirements
------------

CLIPSJNI project has been __mavenized__.
This means that [Maven](http://maven.apache.org/) must be installed.

CLIPS' native library must be installed in the system too.
This procedure is already explained in the _README_ file of the _library-src_ directory.


Installation
------------

To install CLIPSJNI in your Maven local repository, simply run:

    mvn install

This will also generate an OSGi compliant jar in the _target_ subfolder that you can use wherever you want.
