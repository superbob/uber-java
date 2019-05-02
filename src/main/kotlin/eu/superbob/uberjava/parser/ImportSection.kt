package eu.superbob.uberjava.parser

import com.github.javaparser.ast.ImportDeclaration
import eu.superbob.uberjava.ClassNames.isAJdkType
import eu.superbob.uberjava.parser.Filters.missingInByKey
import java.util.*

class ImportSection(imports: List<ImportDeclaration>) {
    private val imports: MutableList<ImportDeclaration> = ArrayList(imports)

    fun getImports(): List<ImportDeclaration> {
        return imports
    }

    fun merge(other: ImportSection) {
        val otherToAdd = other.imports
                .filter(missingInByKey(imports) { it.nameAsString })
        imports.addAll(otherToAdd)
    }

    fun clean() {
        val cleanedImports = imports
                .filter { i -> isAJdkType(i.nameAsString) }
                .distinctBy { it.nameAsString }
                .sortedBy { it.nameAsString }
        imports.clear()
        imports.addAll(cleanedImports)
    }
}
