package eu.superbob.uberjava

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import java.io.File
import java.nio.file.Path
import java.util.stream.Collectors

class UberJavaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("uberJava", UberJavaExtension::class.java, project)

        val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSets = javaPlugin.sourceSets
        val mainSourceSet = sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!!

        val pathSetProperty = project.objects.setProperty(File::class.java)

        project.afterEvaluate { p -> pathSetProperty.set(p.configurations.getByName("compile").resolve()) }

        project.tasks.register("uberJava", UberJavaTask::class.java).configure { task ->
            task.description = "Merge multiple Java source files into a single Java file"
            task.group = "UberJava"
            task.mainJavaClass.set(extension.mainJavaClass)
            task.outputDirectory.set(extension.outputDirectory)
            task.sourceFiles.set(mainSourceSet.allJava)
            task.jars.set(pathSetProperty)
        }
    }
}
