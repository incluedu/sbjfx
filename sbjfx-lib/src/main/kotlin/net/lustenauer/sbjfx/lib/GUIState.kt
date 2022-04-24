package net.lustenauer.sbjfx.lib

import javafx.application.HostServices
import javafx.scene.Scene
import javafx.stage.Stage
import java.awt.SystemTray

/**
 * The object [GUIState] stores Scene and Stage objects as singletons in
 * this VM.
 *
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
object GUIState {
    lateinit var scene: Scene
    lateinit var stage: Stage
    var title: String = "Java Fx Application"
    var hostServices: HostServices? = null
    var systemTray: SystemTray? = null
}
