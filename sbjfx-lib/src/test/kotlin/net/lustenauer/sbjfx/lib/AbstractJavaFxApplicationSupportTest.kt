package net.lustenauer.sbjfx.lib

import net.lustenauer.sbjfx.lib.jfxtest.AbstractSbjfxTest
import net.lustenauer.sbjfx.lib.jfxtest.SampleView
import net.lustenauer.sbjfx.lib.jfxtest.TestApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit


@Disabled
internal class AbstractJavaFxApplicationSupportTest : AbstractSbjfxTest() {

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
    override fun loadDefaultIcons() {
        val images = app.loadDefaultIcons()
        assertThat(images).hasSize(5)
    }

}

