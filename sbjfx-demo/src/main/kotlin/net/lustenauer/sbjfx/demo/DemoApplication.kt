package net.lustenauer.sbjfx.demo

import net.lustenauer.sbjfx.demo.views.HelloWorldView
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.launch
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application : AbstractJavaFxApplicationSupport()

fun main(args: Array<String?>) {
    launch(Application::class.java, HelloWorldView::class.java, args)
}




