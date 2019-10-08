# Freeplane Gradle Addon Development Plugin
The plugin adds tasks `packageAddon` and `prepareAddonSource`. It requires Freeplane with installed Developer Tools Add-on (see https://www.freeplane.org/wiki/index.php/Add-ons_(install)#Developer_Tools).

## Directory structure:

Put your source code to be packaged as jar file under

* src/main/groovy
* src/main/java
* src/main/resources

Put your scripts under

* src/scripts/groovy

Add your dependencies either to special configuration `addon` to be included in the created add-on installer.

Put your add-on definition mind map and add-on resources not packaged in add-on jar into

* /src/addon

## Usage

```gradle
buildscript {
    repositories {
        maven { url "http://dl.bintray.com/freeplane/freeplane" }
    }
    dependencies {
        classpath 'org.freeplane:gradle-freeplane-plugin:0.2'
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
    addonSourceDirectory = 'src/addon'

    // optional, to be set only if more then one mind map file in the addon source directory
    addonDefinitionMindMapFileName = 'Greetings.mm'

    // optional, includes
    includes = ['**/*']

    // optional, excludes
    excludes = ['**/*.bak', '**/~*', '**/$~*.mm~']

}

jar {
    archiveFileName = 'greetings.jar'
}
```

## Debugging

For debugging import gradle project and freeplane gradle project into your IDE
See https://www.freeplane.org/wiki/index.php/IDE_setup
