package net.lustenauer.sbjfx.lib

import javafx.scene.Parent
import javafx.scene.control.ProgressBar
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import net.lustenauer.sbjfx.lib.exceptions.ResourceNotFoundException

/**
 * Default standard JavaFX splash pane implementation.
 * Subclass this to customize with your own layout and image assets.
 * Note that Spring Boot dependency injection is not yet available during this lifecycle stage.
 *
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
open class SplashScreen {

    open var imagePath: String = DEFAULT_IMAGE_PATH
    open var style: String = DEFAULT_STYLE
    open var headerText: String = DEFAULT_HEADER_TEXT
    open var footerText: String = DEFAULT_FOOTER_TEXT
    open var contentText: String = DEFAULT_CONTENT_TEXT
    open var visible: Boolean = true

    /**
     * Resolves and constructs the graphical UI parent node container for the splash screen window.
     *
     * @return The fully populated [Parent] layout node hierarchy.
     * @throws ResourceNotFoundException If the designated image resource file cannot be located.
     */
    open val parent: Parent
        get() {
            val resourceUrl = javaClass.getResource(imagePath)?.toExternalForm()
                ?: throw ResourceNotFoundException("Cannot find image resource at path '$imagePath'")

            val imageView = ImageView(resourceUrl)

            return VBox().apply {
                style = this@SplashScreen.style
                children.addAll(
                    Pane(
                        imageView,
                        Text(20.0, 30.0, headerText).apply { style = "-fx-font-size: 25; -fx-underline: true" },
                        Text(20.0, 50.0, contentText),
                        Text(20.0, 390.0, footerText)
                    ),
                    ProgressBar().apply { prefWidth = imageView.image.width },
                )
            }
        }

    companion object {
        const val DEFAULT_STYLE = ""
        const val DEFAULT_IMAGE_PATH = "/splash/javafx.png"
        const val DEFAULT_HEADER_TEXT = "SBJFX"
        const val DEFAULT_FOOTER_TEXT = "This is free software"
        const val DEFAULT_CONTENT_TEXT = "Spring Boot JavaFX support"
    }
}
