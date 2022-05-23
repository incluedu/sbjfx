package net.lustenauer.sbjfx.lib

import net.lustenauer.sbjfx.lib.jfxtest.AbstractSbjfxTest
import net.lustenauer.sbjfx.lib.jfxtest.SampleIncorrectView
import net.lustenauer.sbjfx.lib.jfxtest.SampleView
import net.lustenauer.sbjfx.lib.jfxtest.TestApp
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.testfx.api.FxToolkit

internal class CustomErrorActionTest : AbstractSbjfxTest() {

    private lateinit var errorAction: ErrorAction

    @BeforeEach
    @Throws(Exception::class)
    fun setup() {
        errorAction = Mockito.mock(ErrorAction::class.java)
        FxToolkit.registerPrimaryStage()
        app = TestApp()
        AbstractJavaFxApplicationSupport.savedInitialView = SampleView::class.java
        AbstractJavaFxApplicationSupport.splashScreen = SplashScreen()
        AbstractJavaFxApplicationSupport.setErrorAction { errorAction.action() }
        FxToolkit.setupApplication { app }
    }

    @Test
    @DisplayName("Custom error action is executed")
    override fun loadDefaultIcons() {
        AbstractJavaFxApplicationSupport.showInitialView(SampleIncorrectView::class.java)
        Mockito.verify(errorAction, Mockito.times(3)).action()
    }
}

internal interface ErrorAction {
    fun action()
}
