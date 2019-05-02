package eu.superbob.uberjava.parser

object Filters {
    fun <T> missingInByKey(imports: List<T>, keyExtractor: (T) -> Any): (T) -> Boolean {
        return { o -> imports.stream().noneMatch { i -> keyExtractor(i) == keyExtractor(o) } }
    }
}
