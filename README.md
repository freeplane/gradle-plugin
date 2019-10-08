# Gradle Plugin for Freeplane Script and Add-on Development
The plugin allows easy debugging of Freeplane groovy scripts in IDE and adds tasks `packageAddon` and `prepareAddonSource` to package your scripts as Freeplane add-on. The packaging requires Freeplane with installed Developer Tools Add-on (see https://www.freeplane.org/wiki/index.php/Add-ons_(install)#Developer_Tools).

On MacOS gradle should run under JAVA 8 for executing `packageAddon`. Otherwise you can run `prepareAddonSource` and  open with build/addon/yourFile.mm in Freeplane and package it manually. On other OS all JAVA versions are supported.

## Usage

### Directory structure:

Put your source code to be packaged as jar file under

* src/main/groovy
* src/main/java
* src/main/resources

Put your scripts under

* src/scripts/groovy

Add your dependencies either to special configuration `addon` to be included in the created add-on installer.

Put your add-on definition mind map and add-on resources not packaged in add-on jar into

* /src/addon


```gradle
buildscript {
    repositories {
        maven { url "http://dl.bintray.com/freeplane/freeplane" }
    }
    dependencies {
        classpath 'org.freeplane:gradle-freeplane-plugin:0.3'
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'org.freeplane.gradle-freeplane-plugin'

freeplane {
    // mandatory, freeplane installation directory.
    // (Freeplane.app directory on MacOS)
    // In this example it is taken from system environment.
    freeplaneDirectory = System.env.FREEPLANE_DIR

    // optional, addon source directory
    // addonSourceDirectory = 'src/addon'

    // optional, to be set only if more then one mind map file in the addon source directory
    // addonDefinitionMindMapFileName = 'Greetings.mm'

    // optional, includes
    // includes = ['**/*']

    // optional, excludes
    // excludes = ['**/*.bak', '**/~*', '**/$~*.mm~']

    // optional, max heap size for freeplane
    // maxHeapSize = '1024m'

    // optional, freeplane user setting directory
    // userDirectory = 'user-directory'

    // optional, additional Java Runtime options
    // jvmArgs = []

}

jar {
    archiveFileName = 'greetings.jar'
}
```

## Debugging

For debugging

* import gradle project and freeplane gradle project into your IDE (See [related freeplane wiki page](https://www.freeplane.org/wiki/index.php/IDE_setup))
* run Freeplane in IDE debugger
* add classes compiled by IDE to script class path and addon source script directory to script directories in Freeplane prefs
* Let IDE compile the addon project
