package eu.superbob.uberjava.parser

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayList

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.ModifierVisitor
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.types.ResolvedType

class ParsedFile(private val compilationUnit: CompilationUnit) {
    val definedTypes: List<ClassReference>
        get() {
            val classNames = ArrayList<ClassReference>()
            compilationUnit.accept(object : VoidVisitorAdapter<MutableList<ClassReference>>() {
                override fun visit(n: ClassOrInterfaceDeclaration, collector: MutableList<ClassReference>) {
                    super.visit(n, collector)
                    collector.add(ClassReference.from(n.resolve()))
                }

                override fun visit(n: EnumDeclaration, collector: MutableList<ClassReference>) {
                    super.visit(n, collector)
                    collector.add(ClassReference.from(n.resolve()))
                }
            }, classNames)
            return classNames
        }

    var importSection: ImportSection
        get() = ImportSection(compilationUnit.imports)
        set(importSection) {
            compilationUnit.accept(object : ModifierVisitor<Void?>() {
                override fun visit(n: ImportDeclaration, arg: Void?): Node {
                    val node = super.visit(n, arg)
                    n.remove()
                    return node
                }
            }, null)
            importSection.getImports().forEach { compilationUnit.addImport(it) }
        }

    fun extractClassNames(): List<ClassReference> {
        val classNames = ArrayList<ClassReference>()
        compilationUnit.accept(object : VoidVisitorAdapter<MutableList<ClassReference>>() {
            override fun visit(n: ClassOrInterfaceType, collector: MutableList<ClassReference>) {
                super.visit(n, collector)
                try {
                    val symbolResolver = compilationUnit.getData(Node.SYMBOL_RESOLVER_KEY)
                    val resolvedType = symbolResolver.toResolvedType(n, ResolvedType::class.java)
                    if (resolvedType.isReferenceType) {
                        collector.add(ClassReference.from(resolvedType.asReferenceType().typeDeclaration))
                    }
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    System.err.println("An exception occurred while parsing '$n'")
                }

            }

            override fun visit(n: MethodCallExpr, collector: MutableList<ClassReference>) {
                super.visit(n, collector)
                val bytes = ByteArrayOutputStream()
                try {
                    val symbolResolver = compilationUnit.getData(Node.SYMBOL_RESOLVER_KEY)
                    val resolvedMethodDeclaration = symbolResolver.resolveDeclaration(n, ResolvedMethodDeclaration::class.java)
                    collector.add(ClassReference.from(resolvedMethodDeclaration.declaringType()))
                } catch (e: RuntimeException) {
                    print(String(bytes.toByteArray(), StandardCharsets.UTF_8))
                    System.err.println("An exception occurred while parsing '$n'")
                }
            }
        }, classNames)
        return classNames
    }

    fun merge(other: ParsedFile) {
        val targetImportSection = importSection
                .apply { merge(other.importSection) }
                .apply { clean() }
        importSection = targetImportSection
        other.compilationUnit.comments.forEach { it.remove() }
        other.compilationUnit.types.forEach { type ->
            type.isPublic = false
            compilationUnit.addType(type)
        }
    }

    override fun toString(): String {
        return compilationUnit.toString()
    }
}
