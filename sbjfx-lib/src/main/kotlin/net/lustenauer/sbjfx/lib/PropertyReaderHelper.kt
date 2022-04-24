package de.felixroske.jfxsupport

import org.springframework.core.env.Environment
import java.util.function.Consumer

/**
 * The utility PropertyReaderHelper.
 *
 * @author Felix Roske
 * @author Andreas Jay
 */
object PropertyReaderHelper {
    /**
     * Lookup in [Environment] a certain property or a list of properties.
     *
     * @param env
     * the [Environment] context from which to
     * @param propName
     * the name of the property to lookup from [Environment].
     * @return the list
     */
    @JvmStatic
    operator fun get(env: Environment, propName: String): List<String> {
        val list: MutableList<String> = ArrayList()
        val singleProp = env.getProperty(propName)
        if (singleProp != null) {
            list.add(singleProp)
            return list
        }
        var counter = 0
        var prop = env.getProperty("$propName[$counter]")
        while (prop != null) {
            list.add(prop)
            counter++
            prop = env.getProperty("$propName[$counter]")
        }
        return list
    }

    /**
     * Load from [Environment] a key with a given type. If success key is
     * present supply it in [Consumer].
     *
     * @param <T>
     * the generic type
     * @param env
     * the env
     * @param key
     * the key
     * @param type
     * the type
     * @param function
     * the function
    </T> */
    @JvmStatic
    fun <T> setIfPresent(env: Environment, key: String, type: Class<T>, function: Consumer<T>) {
        env.getProperty(key, type)?.let { function.accept(it) }
    }

    /**
     * Determine file path from package name creates from class package instance
     * the file path equivalent. The path will be prefixed and suffixed with a
     * slash.
     *
     * @return the path equivalent to a package structure.
     */
    @JvmStatic
    fun determineFilePathFromPackageName(clazz: Class<*>): String =
        "/" + clazz.getPackage().name.replace('.', '/') + "/"
}
