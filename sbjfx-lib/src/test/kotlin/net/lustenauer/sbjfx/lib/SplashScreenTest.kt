package net.lustenauer.sbjfx.lib


import javafx.scene.layout.Pane
import net.lustenauer.sbjfx.lib.jfxtest.SpringJavaFxTestingBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


internal class SplashScreenTest : SpringJavaFxTestingBase() {
    private lateinit var splashScreen: SplashScreen

    @BeforeEach
    fun beforeEach() {
        splashScreen = SplashScreen()
    }


    @Test
    @DisplayName("Get parent")
    fun getParentTest() {
        val parent = splashScreen.parent
        assertThat(parent).isInstanceOf(Pane::class.java)
    }

    @Test
    @DisplayName("Is visible test")
    fun isVisibleTest() {
        assertThat(splashScreen.visible).isTrue
    }

    @Test
    @DisplayName("Get image path")
    fun getImagePathTest() {
        assertThat(splashScreen.imagePath).isEqualTo("/splash/javafx.png")
    }
}
