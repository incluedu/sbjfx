package net.lustenauer.sbjfx.lib.anotations

import org.springframework.stereotype.Component

/**
 * Stereotype annotation used to mark JavaFX controller classes as Spring-managed beans.
 * Allows seamless dependency injection (DI) into views loaded via FXML descriptors.
 *
 * @author Felix Roske
 * @author Patric Hollenstein
 */
@Component
@Retention(AnnotationRetention.RUNTIME)
annotation class FXMLController
