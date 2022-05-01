package net.lustenauer.sbjfx.demo

import javafx.application.Platform
import javafx.scene.control.Alert
import net.lustenauer.sbjfx.demo.views.HelloWorldView
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.launch
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.setErrorAction
import net.lustenauer.sbjfx.lib.SplashScreen
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application : AbstractJavaFxApplicationSupport()

fun main(args: Array<String?>) {
    // use a custom error action
    setErrorAction(customErrorAction())
    launch(Application::class.java, HelloWorldView::class.java, CustomSplashScreen(), args)
}

fun customErrorAction(): (t: Throwable) -> Unit = {
    Alert(
        Alert.AlertType.ERROR,
        "Error:= ${it.localizedMessage} \n\n" + "The application will stop now."
    ).showAndWait().ifPresent { Platform.exit() }
}

class CustomSplashScreen : SplashScreen() {
    override val imagePath = "/splash/customSplash.png"
}




