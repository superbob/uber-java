package eu.superbob.uberjava.parser

import com.github.javaparser.ParseStart.COMPILATION_UNIT
import com.github.javaparser.Providers.provider

import java.nio.file.Files
import java.nio.file.Path

import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver

class JavaParserFacade(private val javaSources: Path, private val jars: Collection<Path>) {
    private val typeSolver: TypeSolver = CombinedTypeSolver(ReflectionTypeSolver(true), JavaParserTypeSolver(javaSources), *getJarTypeSolvers(jars))
    private val javaParser: JavaParser = JavaParser(ParserConfiguration().apply { setSymbolResolver(JavaSymbolSolver(typeSolver)) })

    // Using JarTypeSolver#JarTypeSolver(InputStream) instead of JarTypeSolver#JarTypeSolver(Path) because the last
    // one doesn't close the file after reading it. The downside is that the first one creates a temporary file
    // that is deleted on exit :/. See https://github.com/javaparser/javaparser/blob/master/javaparser-symbol-solver-core/src/main/java/com/github/javaparser/symbolsolver/resolution/typesolvers/JarTypeSolver.java#L100-L109
    private fun getJarTypeSolvers(jars: Collection<Path>): Array<JarTypeSolver> =
            jars.map { pathToJar -> JarTypeSolver(Files.newInputStream(pathToJar)) }.toTypedArray()

    fun parse(path: Path): ParsedFile {
        val parse = javaParser.parse(COMPILATION_UNIT, provider(resolve(path).open()))
        if (!parse.isSuccessful) {
            throw ParseProblemException(parse.problems)
        }
        return ParsedFile(parse.result.get())
    }

    fun resolve(className: String): ClassReference {
        val resolvedReferenceTypeDeclaration = typeSolver.solveType(className)
        return ClassReference(resolvedReferenceTypeDeclaration.packageName, resolvedReferenceTypeDeclaration.className)
    }

    private fun resolve(path: Path): Location {
        val direct = Location.direct(javaSources.resolve(path))
        return if (direct.exists()) {
            direct
        } else {
            jars
                    .map(locateInArchive(path))
                    .find { it.exists() } ?: throw IllegalStateException("Cannot find sources for: $path")
        }
    }

    private fun locateInArchive(path: Path): (Path) -> Location {
        return { a -> Location.zip(a, path) }
    }
}
