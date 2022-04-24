package net.lustenauer.sbfx.demoapp

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport.Companion.launch
import net.lustenauer.sbfx.demoapp.views.HelloWorldView
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application : AbstractJavaFxApplicationSupport()

fun main(args: Array<String?>) {
    launch(Application::class.java, HelloWorldView::class.java, args)
}




