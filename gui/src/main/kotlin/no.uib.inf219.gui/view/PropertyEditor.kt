package no.uib.inf219.gui.view

import no.uib.inf219.gui.controllers.ObjectEditorController
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.onChange

/**
 * @author Elg
 */
class PropertyEditor : Fragment("Attribute Editor") {

    private val controller: ObjectEditorController by param()

    override val root = borderpane {
        controller.currProp.onChange {
            center = it?.middle?.toView(this, controller)
        }
    }
}
