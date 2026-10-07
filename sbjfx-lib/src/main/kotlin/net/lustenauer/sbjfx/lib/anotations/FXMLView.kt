package net.lustenauer.sbjfx.lib.anotations

import javafx.stage.Modality
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

/**
 * Stereotype annotation indicating that a class represents a JavaFX view configuration bean.
 * Seamlessly integrates FXML layouts, styling stylesheets, and localized resource bundles
 * into the Spring dependency injection container.
 *
 * @author Felix Roske
 * @author Patric Hollenstein
 */
@Component
@Retention(AnnotationRetention.RUNTIME)
annotation class FXMLView(

    /**
     * The explicit Spring bean name. Aliased directly to [Component.value]
     * to ensure full compatibility with Spring Boot 3 framework standards.
     */
    @get:AliasFor(annotation = Component::class, attribute = "value")
    val value: String = "",

    /**
     * The explicit classpath path to the target FXML layout file (e.g., "/view/mainWindow.fxml").
     * Separated from the Spring bean name to prevent path validation issues in Spring Boot 3.
     */
    val fxml: String = "",

    /**
     * Local CSS stylesheets to be applied together with this specific view layout hierarchy.
     */
    val css: Array<String> = [],

    /**
     * The classpath descriptor linking a custom resource bundle containing localized strings for this view.
     */
    val bundle: String = "",

    /**
     * The character encoding format applied when reading the specified configuration [bundle].
     * Defaults to the classic standard ISO-8859-1 layout block.
     */
    val encoding: String = "ISO-8859-1",

    /**
     * The window title string displayed on the frame container when active.
     */
    val title: String = "",

    /**
     * The graphical stage layout formatting style applied when spawned as a window frame (e.g., "UTILITY", "DECORATED").
     */
    val stageStyle: String = "UTILITY",

    /**
     * Configures window modal blocking behavior strategies using JavaFX standard modality models.
     */
    val modality: Modality = Modality.NONE
)
