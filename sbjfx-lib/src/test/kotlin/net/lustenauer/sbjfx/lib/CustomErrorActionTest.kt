package net.lustenauer.sbjfx.lib

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.savedInitialView
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.setErrorAction
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.showInitialView
import net.lustenauer.sbjfx.lib.AbstractJavaFxApplicationSupport.Companion.splashScreen
import net.lustenauer.sbjfx.lib.jfxtest.SampleIncorrectView
import net.lustenauer.sbjfx.lib.jfxtest.SampleView
import net.lustenauer.sbjfx.lib.jfxtest.TestApp
import org.junit.jupiter.api.*
import org.testfx.api.FxToolkit


internal class CustomErrorActionTest {

    @MockK(relaxed = true)
    private var errorAction: ErrorAction = mockk<ErrorAction>()
    private lateinit var app: AbstractJavaFxApplicationSupport


    @BeforeEach
    @Throws(Exception::class)
    fun setup() {
        every { errorAction.action() } returns Unit
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
        verify(atLeast = 1, atMost = 2) { errorAction.action() }

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
