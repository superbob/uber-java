package eu.superbob.uberjava

import java.io.File

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

open class UberJavaExtension(project: Project) {
    val mainJavaClass: Property<String> = project.objects.property(String::class.java)
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
            project.layout.buildDirectory.dir(DEFAULT_BUILD_DIRECTORY))

    companion object {
        private val DEFAULT_BUILD_DIRECTORY = "generated" + File.separator + "uber-java"
    }
}
