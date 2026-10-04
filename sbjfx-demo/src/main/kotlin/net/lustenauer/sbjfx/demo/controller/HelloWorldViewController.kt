package net.lustenauer.sbjfx.demo.controller

import javafx.fxml.FXML
import javafx.scene.control.Label
import net.lustenauer.sbjfx.lib.anotations.FXMLController
import java.util.ResourceBundle

@FXMLController
class HelloWorldViewController {

    @FXML
    private lateinit var helloLabel: Label

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    fun initialize() {
        helloLabel.text = resources.getString("hello")
    }
}
