package net.lustenauer.sbjfx.lib

import org.springframework.core.env.Environment

/**
 * Utility object to read, extract, and map configuration properties from the Spring [Environment].
 * Supports reading single values, indexed property lists, and path resolution from class definitions.
 *
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
@Suppress("unused")
object PropertyReaderHelper {

    /**
     * Looks up a single property or an indexed list of properties (e.g., `name[0]`, `name[1]`) from the [Environment].
     *
     * @param env The Spring [Environment] context.
     * @param propName The base name of the property to resolve.
     * @return A list containing all resolved property values, or an empty list if none were found.
     */
    operator fun get(env: Environment, propName: String): List<String> {
        val singleProp = env.getProperty(propName)
        if (singleProp != null) {
            return listOf(singleProp)
        }

        val list = mutableListOf<String>()
        var counter = 0

        while (true) {
            val prop = env.getProperty("$propName[$counter]") ?: break
            list.add(prop)
            counter++
        }
        return list
    }

    /**
     * Retrieves a typed property value from the [Environment] and applies it via a lambda block if present.
     *
     * @param env The Spring [Environment] context.
     * @param key The target property configuration key.
     * @param type The expected target class type of the property value.
     * @param action The Kotlin lambda block to execute with the resolved value.
     */
    fun <T : Any> setIfPresent(env: Environment, key: String, type: Class<T>, action: (T) -> Unit) {
        env.getProperty(key, type)?.let(action)
    }

    /**
     * Transforms a class package name structure into a valid classes-relative file path sequence.
     * Prefixes and suffixes the resolved path string with standard slashes.
     *
     * Example: `net.lustenauer.sbjfx` becomes `/net/lustenauer/sbjfx/`
     */
    fun determineFilePathFromPackageName(clazz: Class<*>): String {
        val path = clazz.packageName.replace('.', '/')
        return "/$path/"
    }
}
