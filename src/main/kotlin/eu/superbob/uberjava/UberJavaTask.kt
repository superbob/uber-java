package eu.superbob.uberjava

import eu.superbob.uberjava.parser.JavaParserFacade
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

import java.io.FileOutputStream
import java.nio.file.Path

import eu.superbob.uberjava.TargetFileLocator.generateTargetPath
import org.gradle.api.tasks.*
import java.io.File
import java.util.stream.Collectors

open class UberJavaTask : DefaultTask() {
    // Task configured "input" properties (see UberJavaPlugin)
    @Input val mainJavaClass: Property<String> = project.objects.property(String::class.java)
    @OutputDirectory val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
    @InputFiles val sourceFiles: Property<SourceDirectorySet> = project.objects.property(SourceDirectorySet::class.java)
    @InputFiles val jars: SetProperty<File> = project.objects.setProperty(File::class.java)

    // Intermediate property used afterwards
    private val javaParserFacade = sourceFiles.flatMap { sourceFiles -> jars
            .map { jars -> JavaParserFacade(sourceFiles.sourceDirectories.singleFile.toPath(), jars.stream().map { it.toPath() }.collect(Collectors.toList())) } }

    // "output" property used to support incremental build
    @OutputFile val outputFile: Provider<RegularFile> = mainJavaClass.flatMap { className -> outputDirectory
            .flatMap { outputDirectory -> javaParserFacade
                    .map { p -> outputDirectory.file(generateTargetPath(p.resolve(className)).toString()) } } }

    // Lazy loaded merger
    private val lazyLoadedMerger: Provider<Merger> = javaParserFacade.map { Merger(it) }

    @TaskAction
    fun merge() {
        lazyLoadedMerger.get().merge(mainJavaClass.get(), FileOutputStream(outputFile.get().asFile))
    }
}
