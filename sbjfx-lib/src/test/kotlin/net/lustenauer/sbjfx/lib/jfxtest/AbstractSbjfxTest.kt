package net.lustenauer.sbjfx.lib.jfxtest

import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

internal abstract class AbstractSbjfxTest {

    internal lateinit var app: AbstractJavaFxApplicationSupport

    abstract fun loadDefaultIcons()

    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            System.setProperty("testfx.robot", "glass")
            System.setProperty("testfx.headless", "true")
            System.setProperty("prism.order", "sw")
            System.setProperty("prism.text", "t2k")
        }

        @AfterAll
        @JvmStatic
        fun after() {
            System.setProperty("testfx.headless", "false")
        }
    }
}
