package net.lustenauer.sbjfx.lib

import javafx.application.Platform
import javafx.stage.Stage
import net.lustenauer.sbjfx.lib.jfxtest.SampleIncorrectView
import net.lustenauer.sbjfx.lib.jfxtest.SpringJavaFxTestingBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testfx.framework.junit5.Start

internal class IncorrectViewTest : SpringJavaFxTestingBase() {

    @Autowired
    private lateinit var incorrectView: SampleIncorrectView

    private lateinit var stage: Stage

    @Start
    override fun start(stage: Stage) {
        this.stage = stage
    }

    @Test
    @DisplayName("View with incorrect location")
    fun viewWithIncorrectLocationTest() {
        var thrown = Exception()
        Platform.runLater() {
            thrown = assertThrows(Exception::class.java) {
                init(incorrectView)
                super.start(stage)
            }
        }
        Thread.sleep(1000) // wait one second
        assertThat(thrown.message).isEqualTo("Cannot load 'sampleincorrect'")
    }
}
