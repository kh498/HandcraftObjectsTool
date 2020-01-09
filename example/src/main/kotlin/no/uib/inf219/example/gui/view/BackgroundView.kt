package no.uib.inf219.example.gui.view

import no.uib.inf219.example.gui.controller.ViewController
import tornadofx.View
import tornadofx.borderpane

/**
 * @author Elg
 */
class BackgroundView : View() {

    val controller: ViewController = ViewController()

    override val root = borderpane {
        title = controller.conv.name
        center = ConversationView(controller).root
    }
}
