package net.lustenauer.sbjfx.lib

import javafx.application.Platform
import javafx.stage.Modality
import javafx.stage.Stage
import net.lustenauer.sbjfx.lib.jfxtest.SampleView
import net.lustenauer.sbjfx.lib.jfxtest.SpringJavaFxTestingBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testfx.api.FxAssert
import org.testfx.framework.junit5.Start
import org.testfx.matcher.base.NodeMatchers
import org.testfx.util.WaitForAsyncUtils


internal class SampleViewTest : SpringJavaFxTestingBase() {
    @Autowired
    private lateinit var sampleView: SampleView
    private lateinit var stage: Stage

    @Start
    override fun start(stage: Stage) {
        this.stage = stage
    }

    @AfterEach
    fun afterEach() {
        Platform.runLater { sampleView.hide() }
    }

    @Test
    @DisplayName("Show view")
    fun showViewTest() {
        Platform.runLater { sampleView.showView(Modality.APPLICATION_MODAL) }
        WaitForAsyncUtils.waitForFxEvents()
        FxAssert.verifyThat(sampleView.view, NodeMatchers.isVisible())
    }

    @Test
    @DisplayName("Show view given stage")
    fun showViewGivenStageTest() {
        Platform.runLater { sampleView.showView(stage, Modality.NONE) }
        Thread.sleep(1000)
        WaitForAsyncUtils.waitForFxEvents()
        FxAssert.verifyThat(sampleView.view, NodeMatchers.isVisible())
    }

    @Test
    @DisplayName("Show view and wait")
    fun showViewAndWaitTest() {
        Platform.runLater { sampleView.showViewAndWait(Modality.WINDOW_MODAL) }
        WaitForAsyncUtils.waitForFxEvents()
        FxAssert.verifyThat(sampleView.view, NodeMatchers.isVisible())
    }

    @Test
    @DisplayName("Show view and wait given stage")
    fun showViewAndWaitGivenStageTest() {
        Platform.runLater { sampleView.showViewAndWait(stage, Modality.APPLICATION_MODAL) }
        WaitForAsyncUtils.waitForFxEvents()
        FxAssert.verifyThat(sampleView.view, NodeMatchers.isVisible())
    }
}
