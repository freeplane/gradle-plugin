# Freeplane Gradle Addon Plugin

## Directory structure:

Put your source code to be packaged as jar file under

* src/main/groovy
* src/main/java
* src/main/resources

Put your scripts under
* src/scripts/groovy

## Example gradle.build

```gradle

buildscript {
    repositories {
        maven { url "http://dl.bintray.com/freeplane/freeplane" }
    }
    dependencies {
        classpath 'org.freeplane:gradle-freeplane-plugin:0.1'
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'org.freeplane.addon-plugin'

freeplane {
    directory = '/freeplane/installation/directory'
}

dependencies {
// addon dependency jar files are packaged with the add-on
    addon fileTree(dir: 'lib', include: '*.jar')
}

```

## Debugging

For debugging import gradle project and freeplane gradle project into your IDE
See https://www.freeplane.org/wiki/index.php/IDE_setup