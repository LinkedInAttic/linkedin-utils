1. Introduction
---------------
The project represents a set of utility classes that are used by other LinkedIn open 
source projects.

2. Compilation
--------------
In order to compile the code you need
* java 1.6
* gradle 0.9-rc2 (http://www.gradle.org/)

At the top simply run

gradle test

which should compile and run all the tests.

3. IDE Support
--------------
You can issue the command (at the top)

gradle idea

which will use the gradle IDEA plugin to create the right set of modules in order to open the
project in IntelliJ IDEA.

4. Directory structure
----------------------
* org.linkedin.util-core
Contains a set of java utilities with no external dependencies (except slf4j
for logging)

Javadoc: http://www.kiwidoc.com/java/l/p/org.linkedin/org.linkedin.util-core

* org.linkedin.util-groovy
Contains a set of groovy utilities

5. Build configuration
----------------------
You can configure some of the build properties by creating the file
~/.org.linkedin.linkedin-utils.properties

Properties:
# where the build goes (if you don't like to have it in the source tree)
top.build.dir=xxx
# where the software is installed (when running package-install)
top.install.dir=xxx
# where the software is published (locally) (when running uploadArchives)
top.publish.dir=xxx

(see build.gradle for how it is being used)
