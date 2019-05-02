package eu.superbob.uberjava

import io.kotlintest.matchers.sequences.containExactlyInAnyOrder
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class UberJavaPluginTest : StringSpec({
    "plugin should initialize correctly" {
        val tempDir = Files.createTempDirectory("uberjava-test")
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(UberJavaPlugin::class.java)

        project.extensions.configure(UberJavaExtension::class.java) { e: UberJavaExtension ->
            e.mainJavaClass.set("uberjava.Single")
            e.outputDirectory.set(tempDir.toFile())
        }

        val task = project.getTasksByName("uberJava", false).iterator().next() as UberJavaTask

        task.mainJavaClass.get() shouldBe "uberjava.Single"
        task.outputDirectory.get().asFile shouldBe tempDir.toFile()

        val expectedMainSourcesDir = project.layout.projectDirectory.dir("src/main/java").asFile

        task.sourceFiles.get().sourceDirectories.asSequence() should containExactlyInAnyOrder(expectedMainSourcesDir)

        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }

        // Testing external sources jars require to hook into internal Gradle API and is to much code to write,
        // so skipping it in this test.
    }
})