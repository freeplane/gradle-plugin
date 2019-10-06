package org.freeplane.gradleplugin
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.GroovyCompile

class FreeplaneAddonPluginExtension {
    String directory = ''
}

class FreeplaneAddonPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.with {
            FreeplaneAddonPluginExtension freeplane = extensions.create('freeplane', FreeplaneAddonPluginExtension)
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
                assert ! freeplane.directory.isEmpty() : "freeplane directory should be set"
                dependencies {
                    compileOnly fileTree(dir: freeplane.directory, include: '**/*.jar')
                }

                task ('prepareAddonSource', type: Copy) {
                    group = 'freeplane'
                    description = 'Prepares addon sources for packaging.'
                    from 'src/addon'
                    into "$buildDir/addon"
                    into ('lib') {
                        from jar.outputs
                    }
                    into ('lib') {
                        from (configurations.addon) {
                            include("*.jar")
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
                    List<String> addonDefinitionFileNames = new FileNameByRegexFinder().getFileNames('src/addon', /.*\.mm/)
                    assert addonDefinitionFileNames.size() == 1
                    String name = new File(addonDefinitionFileNames[0]).name
                    String starter = Os.isFamily(Os.FAMILY_WINDOWS) ? 'freeplaneConsole.exe' : 'freeplane.sh'
                    commandLine "$freeplane.directory/$starter", '-S', '-Xaddons.devtools.releaseAddOn_on_single_node', "$buildDir/addon/$name"
                }
            }
        }
    }
}