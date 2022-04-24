package de.felixroske.jfxsupport.anotations

import org.springframework.stereotype.Component

/**
 * The annotation [FXMLController] is used to mark JavaFX controller
 * classes. Usage of this annotation happens besides registration of such within
 * fxml descriptors.
 *
 * @author Felix Roske
 */
@Component
@Retention(AnnotationRetention.RUNTIME)
annotation class FXMLController 
