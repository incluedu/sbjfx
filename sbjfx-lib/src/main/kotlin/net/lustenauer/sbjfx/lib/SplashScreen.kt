package net.lustenauer.sbjfx.lib

import javafx.scene.Parent
import javafx.scene.control.ProgressBar
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import net.lustenauer.sbjfx.lib.exceptions.ResourceNotFoundException

/**
 * A default standard splash pane implementation Subclass it and override its
 * methods to customize with your own behavior. Be aware that you can not use
 * Spring features here yet.
 *
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
open class SplashScreen {
    /**
     * Change this for custom splash image of default splash pane
     */
    var imagePath = DEFAULT_IMAGE_PATH

    /**
     * Change this for custom style of default splash pane
     *
     * @return empty by default
     */
    var style = DEFAULT_STYLE

    /**
     * Change this for custom header text in default splash pane
     */
    var headerText = DEFAULT_HEADER_TEXT

    /**
     * Change this for custom footer text in default splash pane
     */
    var footerText = DEFAULT_FOOTER_TEXT

    /**
     * Change this for custom content text in default splash pane
     */
    var contentText = DEFAULT_CONTENT_TEXT

    /**
     * Customize if the splash screen should be visible at all.
     *
     * @return true by default
     */
    var visible = true

    /**
     * Override this to create your own splash pane parent node.
     *
     * @return A standard image
     */
    open val parent: Parent
        get() {
            val imageView = ImageView(
                javaClass.getResource(imagePath)?.toExternalForm()
                    ?: throw ResourceNotFoundException("Cannot found image path $imagePath")
            )

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
