package net.lustenauer.sbjfx.lib

import javafx.application.HostServices
import javafx.scene.Scene
import javafx.stage.Stage
import java.awt.SystemTray

/**
 * Thread-safe global singleton object that holds active references to core JavaFX UI components
 * (Stage, Scene, HostServices) and the system environment configuration.
 *
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
@Suppress("unused")
object GUIState {
    lateinit var scene: Scene
    lateinit var stage: Stage

    var title: String = "Java FX Application"
    var hostServices: HostServices? = null
    var systemTray: SystemTray? = null

    val isSceneInitialized: Boolean
        get() = ::scene.isInitialized

    val isStageInitialized: Boolean
        get() = ::stage.isInitialized
}
