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
    def excludes = ['**/*.bak', '**/~*', '**/$~*.mm~']
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
                String osSpecificPath = Os.isFamily(Os.FAMILY_MAC) ? 'Contents/Java/' : ''
                dependencies {
                    compile fileTree("$configuration.freeplaneDirectory/$osSpecificPath"){
                        include '*.jar'
                        include 'core/org.freeplane.core/lib/*.jar'
                        include 'plugins/org.freeplane.plugin.script/lib/*.jar'
                    }
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

                task ('packageAddon', type: JavaExec) {
                    group = 'freeplane'
                    description = 'Packages addon.'
                    dependsOn 'prepareAddonSource'
                    workingDir "$buildDir/addon"
                    String addonDefinitionFileName = configuration.addonDefinitionMindMapFileName ?: defaultAddonDefinitionFileName
                    classpath = files("${configuration.freeplaneDirectory}/${osSpecificPath}freeplanelauncher.jar")
                    maxHeapSize = '1024m'
                    if(Os.isFamily(Os.FAMILY_WINDOWS)) {
                        jvmArgs "-Dorg.freeplane.userfpdir=${System.env.APPDATA}/Freeplane"
                    }
                    if(Os.isFamily(Os.FAMILY_MAC)) {
                        main = 'org.freeplane.launcher.Launcher'
                        jvmArgs '-Dapple.laf.useScreenMenuBar=true', '-Xdock:name=Freeplane'
                    }
                    args '-S', '-Xaddons.devtools.releaseAddOn_on_single_node', "$buildDir/addon/$addonDefinitionFileName"
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