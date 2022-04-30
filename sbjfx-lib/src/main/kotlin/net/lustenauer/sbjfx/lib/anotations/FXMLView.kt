package net.lustenauer.sbjfx.lib.anotations

import org.springframework.stereotype.Component

/**
 * The annotation [FXMLView] indicates a class to be used in the context
 * of an JavaFX view. Such classes are used in combination with fxml markup
 * files.
 *
 * @author Felix Roske
 */
@Component
@Retention(AnnotationRetention.RUNTIME)
annotation class FXMLView(
    /**
     * Value refers to a relative path from where to load a certain fxml file.
     *
     * @return the relative file path of a views fxml file.
     */
    val value: String = "",
    /**
     * Css files to be used together with this view.
     *
     * @return the string[] listing all css files.
     */
    val css: Array<String> = [],
    /**
     * Resource bundle to be used with this view.
     *
     * @return the string of such resource bundle.
     */
    val bundle: String = "",
    /**
     * The encoding that will be sued when reading the [.bundle] file.
     * The default encoding is ISO-8859-1.
     *
     * @return  the encoding to use when reading the resource bundle
     */
    val encoding: String = "ISO-8859-1",
    /**
     * The default title for this view for modal.
     *
     * @return The default title string.
     */
    val title: String = "",
    /**
     * The style to be applied to the underlying stage
     * when using this view as a modal window.
     */
    val stageStyle: String = "UTILITY"
)
