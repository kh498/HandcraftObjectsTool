package no.uib.inf219.gui.backend.primitive

import com.fasterxml.jackson.databind.ser.PropertyWriter
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.util.converter.BooleanStringConverter
import no.uib.inf219.gui.backend.ClassBuilder
import no.uib.inf219.gui.backend.SimpleClassBuilder
import tornadofx.bind
import tornadofx.checkbox

class BooleanClassBuilder(
    initial: Boolean = false,
    name: String,
    parent: ClassBuilder<*>? = null,
    prop: PropertyWriter? = null
) :
    SimpleClassBuilder<Boolean>(
        Boolean::class.java, initial, name, parent, prop,
        BooleanStringConverter()
    ) {
    override fun editView(parent: Pane): Node {
        return parent.checkbox {
            bind(valueProperty)
        }
    }
}
