CLIPSJNI
========

In this project I will apply [some improvements](https://sourceforge.net/p/clipsrules/discussion/776945/thread/01312882) to the original [CLIPSJNI](https://sourceforge.net/projects/clipsrules/) project:

 * More "standardized" package name (e.g. something like net.sf.clipsrules)
 * Throwing exceptions instead of printing errors on the stdout
 * Documenting the Environment class methods a little bit
 * Mavenizing the project
 * Creating an OSGi compliant jar


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
