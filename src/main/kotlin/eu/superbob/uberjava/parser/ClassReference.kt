package eu.superbob.uberjava.parser

import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration

data class ClassReference(val packageName: String?, val className: String) {
    val qualifiedName: String
        get() = if (packageName == null || packageName.isEmpty()) className else "$packageName.$className"

    companion object {
        fun from(typeDeclaration: ResolvedTypeDeclaration): ClassReference {
            return ClassReference(typeDeclaration.packageName, typeDeclaration.className)
        }
    }
}
