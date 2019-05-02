package eu.superbob.uberjava

import eu.superbob.uberjava.parser.ClassReference
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object TargetFileLocator {
    fun generateTargetPath(typeDeclaration: ClassReference): Path {
        var packageDir = typeDeclaration.packageName?.let { Paths.get(it.replace("\\.".toRegex(), "\\" + File.separator)) }
        val classFile = Paths.get(typeDeclaration.className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + ".java")
        return packageDir?.resolve(classFile) ?: classFile
    }
}
