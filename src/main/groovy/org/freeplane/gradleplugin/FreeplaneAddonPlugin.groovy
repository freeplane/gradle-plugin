package org.freeplane.gradleplugin
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.GroovyCompile

class FreeplaneAddonPluginExtension {
    String freeplaneDirectory = null
    String addonDefinitionMindMapFileName = null
    String addonSourceDirectory = 'src/addon'
    List<String> includes = ['**/*']
    List<String> excludes = ['**/*.bak', '**/~*', '**/$~*.mm~']
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
                compile.extendsFrom(addon)
            }
            dependencies {
                ivy "org.apache.ivy:ivy:2.4.0"
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
                        srcDirs = ['src/test/groovy', 'src/scripts/groovy']
                    }
                }
            }

            afterEvaluate {
                assert configuration.freeplaneDirectory != null : "freeplane directory should be set"
                dependencies {
                    compileOnly fileTree(dir: configuration.freeplaneDirectory, include: '**/*.jar')
                }

                task ('prepareAddonSource', type: Sync) {
                    group = 'freeplane'
                    description = 'Prepares addon sources for packaging.'
                    from configuration.addonSourceDirectory
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

                task ('packageAddon', type: Exec) {
                    group = 'freeplane'
                    description = 'Packages addon.'
                    dependsOn 'prepareAddonSource'
                    onlyIf {! Os.isFamily(Os.FAMILY_MAC)}
                    workingDir "$buildDir/addon"
                    String addonDefinitionFileName = configuration.addonDefinitionMindMapFileName ?: defaultAddonDefinitionFileName
                    String starter = Os.isFamily(Os.FAMILY_WINDOWS) ? 'freeplaneConsole.exe' : 'freeplane.sh'
                    commandLine "$configuration.freeplaneDirectory/$starter", '-S', '-Xaddons.devtools.releaseAddOn_on_single_node', "$buildDir/addon/$addonDefinitionFileName"
                }
            }
        }
    }

    private String getDefaultAddonDefinitionFileName() {
        List<String> addonDefinitionFileNames = new FileNameByRegexFinder().getFileNames(configuration.addonSourceDirectory, /.*\.mm/)
        assert addonDefinitionFileNames.size() == 1
        String name = new File(addonDefinitionFileNames[0]).name
        return name
    }
}