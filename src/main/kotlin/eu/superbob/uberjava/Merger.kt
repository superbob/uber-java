package eu.superbob.uberjava

import eu.superbob.uberjava.parser.ClassReference
import eu.superbob.uberjava.parser.JavaParserFacade
import eu.superbob.uberjava.parser.ParsedFile

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*
import eu.superbob.uberjava.TargetFileLocator.generateTargetPath
import eu.superbob.uberjava.ClassNames.isAJdkType

class Merger(private val javaParserFacade: JavaParserFacade) {
    fun merge(mainClass: String, output: OutputStream) {
        val scheduledFiles = LinkedList<Path>()
        scheduledFiles.add(generateTargetPath(javaParserFacade.resolve(mainClass)))
        val parsedFiles = ArrayList<ParsedFile>()
        val seenClasses = HashSet<ClassReference>()
        while (!scheduledFiles.isEmpty()) {
            val fileToAnalyze = scheduledFiles.remove()
            val parsedFile = javaParserFacade.parse(fileToAnalyze)
            seenClasses.addAll(parsedFile.definedTypes)
            parsedFile.extractClassNames()
                    .asSequence()
                    .distinct()
                    .filter { c -> !seenClasses.contains(c) }
                    .filter { c -> !isAJdkType(c.qualifiedName) }
                    .map { generateTargetPath(it) }
                    .filter { c -> !scheduledFiles.contains(c) }
                    .forEach { scheduledFiles.add(it) }
            parsedFiles.add(parsedFile)
        }
        val uberClass = mergeClasses(parsedFiles)
        writeClass(uberClass, output)
    }

    private fun mergeClasses(parsedFiles: List<ParsedFile>): ParsedFile {
        val mainClass = parsedFiles.iterator().next()
        parsedFiles.drop(1).forEach { mainClass.merge(it) }
        return mainClass
    }

    private fun writeClass(mainClass: ParsedFile, outputStream: OutputStream) {
        BufferedWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
                .use { bufferedWriter -> bufferedWriter.append(mainClass.toString()) }
    }
}
