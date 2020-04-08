package no.uib.inf219.extra

import javafx.beans.property.StringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import no.uib.inf219.gui.backend.ClassBuilder
import tornadofx.text
import tornadofx.textflow
import tornadofx.vbox

/**
 * Create a text object that is dependent on the given class builder.
 * Every time the given [cb] changes the text will be updated.
 */
fun EventTarget.textCb(cb: ClassBuilder, value: ClassBuilder.() -> String) {

    text(value(cb)) {
        this.textProperty().bindCbText(cb, value)
    }
}


fun StringProperty.bindCbText(cb: ClassBuilder, value: ClassBuilder.() -> String) {
    cb.serObjectObservable.onChange {
        this.set(value(cb))
    }
}


fun EventTarget.centeredText(vararg lines: String, op: VBox.() -> Unit = {}) {
    vbox {
        alignment = Pos.CENTER
        textflow {
            textAlignment = TextAlignment.CENTER
            for (line in lines.dropLast(1)) {
                text(line + "\n")
            }
            //do not add a newline to the last element
            text(lines.last())
        }
        op()
    }
}
