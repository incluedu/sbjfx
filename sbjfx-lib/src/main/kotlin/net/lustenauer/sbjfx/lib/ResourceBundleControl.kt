package net.lustenauer.sbjfx.lib

import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle

/**
 * Custom [ResourceBundle.Control] implementation that enforces a specified [Charset]
 * (e.g., UTF-8) when reading property resource bundles, overriding the Java default ISO-8859-1.
 *
 * @param charset The target character encoding charset to use.
 * @author Emil Forslund
 * @author Patric Hollenstein
 */
class ResourceBundleControl(private val charset: Charset) : ResourceBundle.Control() {

    @Throws(IllegalAccessException::class, InstantiationException::class, IOException::class)
    override fun newBundle(
        baseName: String,
        locale: Locale,
        format: String,
        loader: ClassLoader,
        reload: Boolean
    ): ResourceBundle? {
        if (format != "java.properties") {
            return super.newBundle(baseName, locale, format, loader, reload)
        }

        val bundleName = toBundleName(baseName, locale)
        val resourceName = toResourceName(bundleName, "properties")

        val stream = runCatching {
            if (reload) {
                loader.getResource(resourceName)?.openConnection()?.apply {
                    useCaches = false
                }?.getInputStream()
            } else {
                loader.getResourceAsStream(resourceName)
            }
        }.getOrNull() ?: return null

        return stream.use { inputStream ->
            runCatching {
                PropertyResourceBundle(InputStreamReader(inputStream, charset))
            }.getOrNull()
        }
    }
}
