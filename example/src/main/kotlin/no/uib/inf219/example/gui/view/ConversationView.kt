package no.uib.inf219.example.gui.view

import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import no.uib.inf219.example.data.Conversation
import no.uib.inf219.example.data.Response
import no.uib.inf219.example.gui.Styles
import tornadofx.*

/**
 * @author Elg
 */
class ConversationView(val tab: Tab, var conv: Conversation) : View() {

    override val root = borderpane {
        title = "conversation"
        style {
            padding = box(5.px)
        }
        setText(this, conv.text)
        bottom {
            hbox {
                alignment = Pos.BASELINE_LEFT
                spacing = 5.0
                createButtons(conv.responses, this)
            }
        }
    }

    private fun createButtons(resps: List<Response>, parent: HBox) {
        with(parent) {
            clear()

            for (response in resps) {
                with(button(response.text)) {
                    setOnAction {
                        if (response.shouldClose()) {
                            tab.close()
                            return@setOnAction
                        }
                        conv = response.conv
                        setText(root, conv.text)
                        createButtons(response.conv.responses, parent)
                    }
                    tooltip = response.tooltip()
                    addClass(Styles.responseButton)
                }

            }
        }
    }

    private fun setText(parent: BorderPane, text: String) {
        with(parent) {
            top = label(text) {
                addClass(Styles.conversationLabel)
            }
        }
    }
}
