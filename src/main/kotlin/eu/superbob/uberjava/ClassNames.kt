package eu.superbob.uberjava

object ClassNames {
    fun isAJdkType(qualifiedName: String): Boolean {
        return qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.")
    }
}
