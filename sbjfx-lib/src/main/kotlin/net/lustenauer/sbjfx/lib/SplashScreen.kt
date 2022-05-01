package net.lustenauer.sbjfx.lib

import javafx.scene.Parent
import javafx.scene.control.ProgressBar
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
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
     *
     */
    open val imagePath = DEFAULT_IMAGE_PATH

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
            val splashProgressBar = ProgressBar()
            splashProgressBar.prefWidth = imageView.image.width
            val vbox = VBox()
            vbox.children.addAll(imageView, splashProgressBar)
            return vbox
        }

    /**
     * Customize if the splash screen should be visible at all.
     *
     * @return true by default
     */
    open fun visible(): Boolean = true


    companion object {
        /**
         * Use your own splash image instead of the default one.
         *
         * @return "/splash/javafx.png"
         */
        const val DEFAULT_IMAGE_PATH = "/splash/javafx.png"
    }
}
