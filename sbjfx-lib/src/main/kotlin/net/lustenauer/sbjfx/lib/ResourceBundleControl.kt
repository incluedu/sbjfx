package net.lustenauer.sbjfx.lib

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

/**
 * Control that uses a custom [Charset] when reading resource bundles,
 * compared to the default charset which is ISO-8859-1.
 *
 * @author Emil Forslund
 * @since  2.1.6
 */
class ResourceBundleControl(charset: Charset) : ResourceBundle.Control() {
    private val charset: Charset

    init {
        this.charset = Objects.requireNonNull(charset)
    }

    @Throws(IllegalAccessException::class, InstantiationException::class, IOException::class)
    override fun newBundle(
        baseName: String,
        locale: Locale,
        format: String,
        loader: ClassLoader,
        reload: Boolean
    ): ResourceBundle {
        val bundleName = toBundleName(baseName, locale)
        val resourceName = toResourceName(bundleName, "properties")
        var bundle: ResourceBundle? = null
        var stream: InputStream? = null
        if (reload) {
            val url = loader.getResource(resourceName)
            if (url != null) {
                val connection = url.openConnection()
                if (connection != null) {
                    connection.useCaches = false
                    stream = connection.getInputStream()
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName)
        }
        if (stream != null) {
            bundle = try {
                PropertyResourceBundle(
                    InputStreamReader(
                        stream, charset
                    )
                )
            } finally {
                stream.close()
            }
        }
        return bundle!!
    }
}
