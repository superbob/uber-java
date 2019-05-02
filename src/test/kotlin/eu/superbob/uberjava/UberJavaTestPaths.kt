package eu.superbob.uberjava

import java.nio.file.Path
import java.nio.file.Paths

typealias ZipEntryDef = Pair<String, Path>
val ZipEntryDef.name: String get() = first
val ZipEntryDef.path: Path get() = second

typealias TargetLocationDef = Pair<Path, String>
val TargetLocationDef.target: Path get() = first
val TargetLocationDef.location: String get() = second

object UberJavaTestPaths {
    val ImportClass: ZipEntryDef = "uberjava/imported/Import.class" to Paths.get("build/classes/java/externalJar/uberjava/imported/Import.class")
    val ImportSource: ZipEntryDef = "uberjava/imported/Import.java" to Paths.get("src/externalJar/java/uberjava/imported/Import.java")

    val Single: TargetLocationDef = Paths.get("uberjava/Single.java") to "srcClasses/Single.java"
    val Base: TargetLocationDef = Paths.get("uberjava/Base.java") to "srcClasses/Base.java"
    val Import: TargetLocationDef = Paths.get("uberjava/imported/Import.java") to "srcClasses/Import.java"
    val Primitives: TargetLocationDef = Paths.get("uberjava/Primitives.java") to "srcClasses/Primitives.java"
    val BaseStatic: TargetLocationDef = Paths.get("uberjava/BaseStatic.java") to "srcClasses/BaseStatic.java"
    val StaticImport: TargetLocationDef = Paths.get("uberjava/imported/StaticImport.java") to "srcClasses/StaticImport.java"

    val BaseInner: TargetLocationDef = Paths.get("uberjava/BaseInner.java") to "srcClasses/BaseInner.java"
    val BaseInner2: TargetLocationDef = Paths.get("uberjava/BaseInner2.java") to "srcClasses/BaseInner2.java"
    val ImportInner: TargetLocationDef = Paths.get("uberjava/imported/ImportInner.java") to "srcClasses/ImportInner.java"

    val BaseGenerics: TargetLocationDef = Paths.get("uberjava/BaseGenerics.java") to "srcClasses/BaseGenerics.java"
    val Something: TargetLocationDef = Paths.get("uberjava/Something.java") to "srcClasses/Something.java"
}
