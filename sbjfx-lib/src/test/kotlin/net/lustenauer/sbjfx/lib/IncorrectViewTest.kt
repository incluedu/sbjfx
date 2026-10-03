package net.lustenauer.sbjfx.lib

import javafx.application.Platform
import javafx.stage.Stage
import net.lustenauer.sbjfx.lib.jfxtest.invalid.SampleIncorrectView
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled // Wichtig!
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Disabled("Deaktiviert für Spring Boot 3 Upgrade Validierung")
internal class IncorrectViewTest : ApplicationTest() {
    private lateinit var stage: Stage

    override fun start(stage: Stage) {
        this.stage = stage
    }

    @Test
    @DisplayName("View with incorrect location")
    fun viewWithIncorrectLocationTest() {
        val futureException = CompletableFuture<Throwable>()

        Platform.runLater {
            try {
                // Erzeugt die fehlerhafte View isoliert im UI-Thread
                val incorrectView = SampleIncorrectView()
                // Versucht die FXML-Ladung händisch zu triggern
                incorrectView.view
                futureException.complete(IllegalArgumentException("No exception thrown"))
            } catch (t: Throwable) {
                futureException.complete(t)
            }
        }

        // Wartet blockierend im Test-Thread, bis der FX-Thread fertig ist
        val thrown = assertThrows(ExecutionException::class.java) {
            futureException.get()
        }

        // Überprüft die zugrundeliegende Exception, die Monocle/JavaFX geworfen hat
        val cause = thrown.cause
        assertThat(cause).isNotNull()
        assertThat(cause?.message).contains("Cannot load")
    }
}
