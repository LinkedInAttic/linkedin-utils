1. Introduction
The project represents a set of utility classes that are used by other LinkedIn open 
source projects.

2. Compilation
In order to compile the code you need
* java 1.6
* gradle 0.9-rc2 (http://www.gradle.org/)

At the top simply run

gradle test

which should compile and run all the tests.

3. Directory structure
* org.linkedin.util-core
Contains a set of java utilities with no external dependencies (except slf4j
for logging)

* org.linkedin.util-groovy
Contains a set of groovy utilities
