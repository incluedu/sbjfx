package net.lustenauer.sbjfx.lib.jfxtest

import net.lustenauer.sbjfx.lib.AbstractFxmlView
import net.lustenauer.sbjfx.lib.anotations.FXMLView

@FXMLView(value = "/testView.fxml", bundle = "testView", css = ["/style.css"])
internal class SampleView : AbstractFxmlView()
