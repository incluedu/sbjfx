package de.felixroske.jfxsupport

import javafx.scene.Parent
import javafx.scene.control.ProgressBar
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

/**
 * A default standard splash pane implementation Subclass it and override it's
 * methods to customize with your own behavior. Be aware that you can not use
 * Spring features here yet.
 *
 * @author Felix Roske
 * @author Andreas Jay
 */
class SplashScreen {
    /**
     * Override this to create your own splash pane parent node.
     *
     * @return A standard image
     */
    val parent: Parent
        get() {
            val imageView = ImageView(javaClass.getResource(IMAGE_PATH).toExternalForm())
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
    fun visible(): Boolean {
        return true
    }

    companion object {
        /**
         * Use your own splash image instead of the default one.
         *
         * @return "/splash/javafx.png"
         */
        const val IMAGE_PATH = "/splash/javafx.png"
    }
}
