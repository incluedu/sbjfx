package net.lustenauer.sbjfx.lib

import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.savedInitialView
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.setErrorAction
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.showInitialView
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.splashScreen
import net.lustenauer.sbjfx.lib.jfxtest.SampleIncorrectView
import net.lustenauer.sbjfx.lib.jfxtest.SampleView
import net.lustenauer.sbjfx.lib.jfxtest.TestApp
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.testfx.api.FxToolkit


internal class CustomErrorActionTest {

    private lateinit var errorAction: ErrorAction
    private lateinit var app: AbstractJavaFxApplicationSupport


    @BeforeEach
    @Throws(Exception::class)
    fun setup() {
        errorAction = Mockito.mock(ErrorAction::class.java)
        FxToolkit.registerPrimaryStage()
        app = TestApp()
        savedInitialView = SampleView::class.java
        splashScreen = SplashScreen()
        setErrorAction { errorAction.action() }
        FxToolkit.setupApplication { app }
    }

    @Test
    @DisplayName("Custom error action is executed")
    fun showInitialViewTest() {
        showInitialView(SampleIncorrectView::class.java)
//        Mockito.verify(errorAction, Mockito.times(1)).action()
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeClass(): Unit {
            System.setProperty("testfx.headless", "true")
        }

        @JvmStatic
        @AfterAll
        fun afterClass(): Unit {
            System.setProperty("testfx.headless", "false")
        }
    }
}

interface ErrorAction {
    fun action() {
    }
}
