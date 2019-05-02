UberJava gradle plugin
======================

[![Build
Status](https://travis-ci.org/superbob/uber-java.svg?branch=master)](https://travis-ci.org/superbob/uber-java)

Use this plugin to merge multiple Java source files info a single one.

You can see it as a
[Shadow](https://imperceptiblethoughts.com/shadow/),
[Shade](http://maven.apache.org/plugins/maven-shade-plugin/) or even
[One-JAR](http://one-jar.sourceforge.net/) but for Java sources instead of JAR files.

It is particularly useful when doing Java contests such as
[codewars](https://www.codewars.com/),
[CodingGame](https://www.codingame.com) or
[battledev [FR]](https://battledev.blogdumoderateur.com/).

Using your favorite IDE and this plugin you can code your Java program organizing it into multiple source files,
then when you're done run the plugin and you'll have a single Java file ready to be submitted.

What does it do?
----------------

Basically it takes all the JRE standard library imports found across all detected Java source files,
put them on the top of the resulting file, then takes all classes and add them to the resulting file as side classes
of the main class.

Usage
-----

To make it work you will need a _build.gradle_ file such as this one:

    plugins {
        id 'java'
        id 'eu.superbob.uberjava' version '1.0.0'
    }
    
    uberJava {
        mainJavaClass = 'com.isograd.exercise.IsoContest'
    }
    
    repositories {
        jcenter()
    }

Then run this command:

    ./gradlew uberJava

And you'll have the result in _build/generated/uber-java/com/isograd/exercise/IsoContest.java_.

Features
--------

### Selective imports

The plugin will parse Java sources, analyze them and include only what's necessary based on imports and usages.  
Thanks to this you can have unused Java sources, they won't be included in the resulting file.

Because of this feature all Java classes must be imported or named explicitly. It won't see classes used from reflection
or _Class.forName('xxx')_ stuff. 

### External sources (Experimental)

It can import external sources, for example if you use _commons-lang3_ _StringUtils_ you'll have to add these
dependencies to your gradle build:

    dependencies {
        compile 'org.apache.commons:commons-lang3:3.8.1'
        compile 'org.apache.commons:commons-lang3:3.8.1:sources'
    }

It will only work if you provide the _:sources_ dependency that contains the Java sources as the plugin works
at the Java level.

Be careful as this can result in huge resulting files. External files can have themselves imports and the plugin will
import everything.

Thanks
------

Big thanks to the [JavaParser](https://javaparser.org/) library that handle all the Java parsing under the hood.

License
-------

All files provided here are licensed under the Simplified BSD "2-Clause" License. See the [LICENSE](LICENSE) file
for the complete copyright notice.
