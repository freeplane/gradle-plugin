package org.freeplane.gradleplugin
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.GroovyCompile

class FreeplaneAddonPluginExtension {
    String freeplaneDirectory = null
    String addonDefinitionMindMapFileName = null
    String addonSourceDirectory = 'src/addon'
    def includes = ['**/*']
    def excludes = ['**/*.bak', '**/~*', '**/$~*.mm~', '**/*.gdsl', '**/*.dsld']
    String maxHeapSize = '1024m'
    String userDirectory = Os.isFamily(Os.FAMILY_WINDOWS) ? "${System.env.APPDATA}/Freeplane" : Os.isFamily(Os.FAMILY_UNIX) ? (System.env.XDG_CONFIG_HOME ?: "${System.env.HOME}/.config" + '/freeplane') : null
    def jvmArgs = null
}

class FreeplaneAddonPlugin implements Plugin<Project> {
    private FreeplaneAddonPluginExtension configuration

    void apply(Project project) {
        project.with {
            configuration = extensions.create('freeplane', FreeplaneAddonPluginExtension)
            pluginManager.apply GroovyPlugin.class
            configurations {
                ivy
                addon
                compileOnly.extendsFrom(addon)
                scriptsImplementation.extendsFrom(implementation)
            }
            dependencies {
                ivy "org.apache.ivy:ivy:2.4.0"
                scriptsImplementation sourceSets.main.output
            }
            tasks.withType(GroovyCompile) {
                groovyClasspath += configurations.ivy
            }

            sourceSets {
                main {
                    groovy {
                        srcDirs = ['src/main/groovy']
                    }
                }

                test {
                    groovy {
                        srcDirs = ['src/test/groovy']
                    }
                }
                scripts {
                    groovy {
                        srcDirs = ['src/scripts/groovy']
                    }
                }
            }

            afterEvaluate {
                assert configuration.freeplaneDirectory != null : "freeplane directory should be set"

                String osSpecificPath = Os.isFamily(Os.FAMILY_MAC) ? 'Contents/app/' : ''
                if (Os.isFamily(Os.FAMILY_MAC) && !new File("$configuration.freeplaneDirectory/$osSpecificPath").exists()) {
                    osSpecificPath = ''
                }

                dependencies {
                    compileOnly fileTree("$configuration.freeplaneDirectory/$osSpecificPath"){
                        include '*.jar'
                        include 'core/org.freeplane.core/lib/*.jar'
                        include 'plugins/org.freeplane.plugin.script/lib/*.jar'
                    }
                }

                sourceSets {
                    scripts.compileClasspath += configurations.compileClasspath
                    scripts.runtimeClasspath += configurations.compileClasspath
                    test.compileClasspath += configurations.compileClasspath
                    test.runtimeClasspath += configurations.compileClasspath
                }

                task ('prepareAddonSource', type: Sync) {
                    group = 'freeplane'
                    description = 'Prepares addon sources for packaging.'
                    from project.file(configuration.addonSourceDirectory)
                    into "$buildDir/addon"
                    include configuration.includes
                    exclude configuration.excludes
                    into ('lib') {
                        from jar.outputs
                    }
                    into ('lib') {
                        from (configurations.addon) {
                            include('*.jar')
                        }
                    }

                    into ('scripts') {
                        from 'src/scripts/groovy'
                    }
                }

                task ('packageAddon', type: JavaExec) {
                    group = 'freeplane'
                    description = 'Packages addon.'
                    dependsOn 'prepareAddonSource'
                    workingDir "$buildDir/addon"
                    String addonDefinitionFileName = configuration.addonDefinitionMindMapFileName ?: this.getDefaultAddonDefinitionFileName(project)
                    classpath = files("${configuration.freeplaneDirectory}/${osSpecificPath}freeplanelauncher.jar")
                    maxHeapSize = configuration.maxHeapSize
                    if(configuration.userDirectory != null) {
                        jvmArgs "-Dorg.freeplane.userfpdir=${configuration.userDirectory}"
                    }
                    if(Os.isFamily(Os.FAMILY_MAC)) {
                        main = 'org.freeplane.launcher.Launcher'
                        jvmArgs '-Dapple.laf.useScreenMenuBar=true', '-Xdock:name=Freeplane'
                    }
                    if(configuration.jvmArgs != null) {
                        jvmArgs configuration.jvmArgs
                    }
                    args '-S', '-Xaddons.devtools.releaseAddOn_on_single_node', "$buildDir/addon/$addonDefinitionFileName"
                }
            }
        }
    }

    private String getDefaultAddonDefinitionFileName(Project project) {
        List<String> addonDefinitionFileNames = new FileNameByRegexFinder().getFileNames(project.file(configuration.addonSourceDirectory).path, /\.mm$/)
        assert addonDefinitionFileNames.size() == 1
        String name = new File(addonDefinitionFileNames[0]).name
        return name
    }
}
