package eu.superbob.uberjava

import eu.superbob.uberjava.parser.JavaParserFacade
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MergerTest : StringSpec({
    "single project file should return the same single file" {
        withMainSources(UberJavaTestPaths.Single) { merger ->
            val actualResult = merger.mergeToString("uberjava.Single")
            actualResult shouldBe resourceToString("resultClasses/Single.java")
        }
    }

    "project with import should merge all files into a single file" {
        withMainSources(UberJavaTestPaths.Base, UberJavaTestPaths.Import) { merger ->
            val actualResult = merger.mergeToString("uberjava.Base")
            actualResult shouldBe resourceToString("resultClasses/Import.java")
        }
    }

    "project with import on inner class should merge all files into a single file" {
        withMainSources(UberJavaTestPaths.BaseInner, UberJavaTestPaths.ImportInner) { merger ->
            val actualResult = merger.mergeToString("uberjava.BaseInner")
            actualResult shouldBe resourceToString("resultClasses/ImportInner.java")
        }
    }

    "project with import on inner class by outer class should merge all files into a single file" {
        withMainSources(UberJavaTestPaths.BaseInner2, UberJavaTestPaths.ImportInner) { merger ->
            val actualResult = merger.mergeToString("uberjava.BaseInner2")
            actualResult shouldBe resourceToString("resultClasses/ImportInner2.java")
        }
    }

    "project with external jar dependency should merge them into a single file" {
        withMainSources(UberJavaTestPaths.Base).andJars(UberJavaTestPaths.ImportClass, UberJavaTestPaths.ImportSource) { merger ->
            val actualResult = merger.mergeToString("uberjava.Base")
            actualResult shouldBe resourceToString("resultClasses/Import.java")
        }
    }

    "single project file with primitives should return the same single file" {
        withMainSources(UberJavaTestPaths.Primitives) { merger ->
            val actualResult = merger.mergeToString("uberjava.Primitives")
            actualResult shouldBe resourceToString("resultClasses/Primitives.java")
        }
    }

    "project with static import should merge all files into a single file" {
        withMainSources(UberJavaTestPaths.BaseStatic, UberJavaTestPaths.StaticImport) { merger ->
            val actualResult = merger.mergeToString("uberjava.BaseStatic")
            actualResult shouldBe resourceToString("resultClasses/StaticImport.java")
        }
    }

    "project with generics should merge all files into a single file" {
        withMainSources(UberJavaTestPaths.BaseGenerics, UberJavaTestPaths.Something) { merger ->
            val actualResult = merger.mergeToString("uberjava.BaseGenerics")
            actualResult shouldBe resourceToString("resultClasses/Generics.java")
        }
    }

})

private fun withMainSources(vararg mainSourcesDefs: TargetLocationDef, jars: List<Path> = emptyList(), closure: (Merger) -> Unit) {
    withMainSources(mainSourcesDefs.asList(), jars, closure)
}

private fun withMainSources(vararg mainSourcesDefs: TargetLocationDef): List<TargetLocationDef> {
    return mainSourcesDefs.asList()
}

private fun withMainSources(mainSourcesDefs: List<TargetLocationDef>, jars: List<Path> = emptyList(), closure: (Merger) -> Unit) {
    val mainSourcesPath = createMainSources(mainSourcesDefs)
    try {
        val merger = Merger(JavaParserFacade(mainSourcesPath, jars))
        closure.invoke(merger)
    } finally {
        Files.walk(mainSourcesPath)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
    }
}

private fun List<TargetLocationDef>.andJars(classJar: ZipEntryDef, srcJar: ZipEntryDef, closure: (Merger) -> Unit) {
    val externalClassJar = createSingleFileJar(classJar)
    val externalClassSourceJar = createSingleFileJar(srcJar, "-sources.jar")
    try {
        withMainSources(this, listOf(externalClassJar, externalClassSourceJar), closure)
    } finally {
        Files.delete(externalClassJar)
        Files.delete(externalClassSourceJar)
    }
}

private fun createSingleFileJar(zipEntryDef: ZipEntryDef, jarExtension: String = ".jar"): Path {
    val zipFile = Files.createTempFile("external", jarExtension)
    val zipOutputStream = ZipOutputStream(Files.newOutputStream(zipFile))
    zipOutputStream.putNextEntry(ZipEntry(zipEntryDef.name))
    zipOutputStream.use { outputStream ->
        Files.newInputStream(zipEntryDef.path).use { inputStream ->
            inputStream.copyTo(outputStream)
            outputStream.flush()
        }
    }
    return zipFile
}

private fun createMainSources(entries: List<TargetLocationDef>): Path {
    val mainSources = Files.createTempDirectory("sources")
    entries.forEach { entry ->
        Files.createDirectories(mainSources.resolve(entry.target.parent))
        Thread.currentThread().contextClassLoader.getResourceAsStream(entry.location)
                .use { Files.copy(it, mainSources.resolve(entry.target)) }
    }
    return mainSources
}

private fun Merger.mergeToString(mainClass: String) =
        String(ByteArrayOutputStream().also { merge(mainClass, it) }.toByteArray(), Charsets.UTF_8)

private fun resourceToString(location: String): String =
        Thread.currentThread().contextClassLoader.getResourceAsStream(location)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
