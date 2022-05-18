package net.lustenauer.sbjfx.lib

import net.lustenauer.sbjfx.lib.jfxtest.SampleView
import net.lustenauer.sbjfx.lib.jfxtest.TestApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.testfx.api.FxToolkit


internal class AbstractJavaFxApplicationSupportTest {

    private lateinit var app: AbstractJavaFxApplicationSupport

    @BeforeEach
    fun init() {
        app = TestApp()
        AbstractJavaFxApplicationSupport.savedInitialView = SampleView::class.java
        AbstractJavaFxApplicationSupport.splashScreen = SplashScreen()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { app }
    }

    @Test
    @DisplayName("Load default icons")
    fun loadDefaultIcons() {
        val images = app.loadDefaultIcons()
        assertThat(images).hasSize(5)
    }

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

