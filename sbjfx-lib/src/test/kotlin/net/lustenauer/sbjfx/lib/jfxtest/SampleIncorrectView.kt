package net.lustenauer.sbjfx.lib.jfxtest

import net.lustenauer.sbjfx.lib.AbstractFxmlView
import net.lustenauer.sbjfx.lib.anotations.FXMLView

@FXMLView(value = "/i_do_not_exist.fxml", bundle = "testView", css = ["style.css"])
internal class SampleIncorrectView : AbstractFxmlView()
