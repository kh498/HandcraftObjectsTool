package no.uib.inf219.gui.view

import javafx.scene.control.ButtonType
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import no.uib.inf219.extra.YES_DISABLE_WARNING
import no.uib.inf219.extra.reload
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.controllers.Settings
import no.uib.inf219.gui.controllers.Settings.showOverwriteWithRefWarning
import no.uib.inf219.gui.view.select.ReferenceSelectorView
import tornadofx.*

/**
 * @author Elg
 */
class NodeExplorerView(private val controller: ObjectEditorController) : View("Tree Explorer") {

    override val root = scrollpane(
        fitToWidth = true,
        fitToHeight = true
    ).treeview(controller.root.item) {
        controller.tree = this
        root.isExpanded = true

        setOnMouseClicked { event ->
            //note that "isPrimaryButtonDown" and "isSecondaryButtonDown" does not work
            if (selectedValue?.cb == null && event.clickCount == 2 && event.button == MouseButton.PRIMARY) {
                controller.createSelected()
                return@setOnMouseClicked
            }
            if (selectedValue !== root) {
                selectedValue?.cb?.onNodeMouseEvent(event, controller)
            }
        }
        setOnKeyPressed { event ->
            //note that "isPrimaryButtonDown" and "isSecondaryButtonDown" does not work
            if (selectedValue?.cb == null && (event.code == KeyCode.ENTER || event.code == KeyCode.SPACE)) {
                controller.createSelected()
                return@setOnKeyPressed
            }
            if (selectedValue !== root) {
                selectedValue?.cb?.onNodeKeyEvent(event, controller)
            }
        }

        cellFormat { cbn ->
            text = cbn.key.getPreviewValue()
            tooltip("Class: ${cbn.cb?.type ?: cbn.parent.getChildType(cbn.key)}")
            this.contextMenu = null
            cbn.item.expandedProperty().addListener { _, _, newValue ->
                if (!newValue && Settings.collapseChildren == true) {
                    cbn.item.children.forEach { it.isExpanded = false }
                }
            }
            contextmenu {
                if (cbn.item !== root) {

                    val refItem = item("Make reference to...").apply {
                        if (cbn.allowReference) {
                            action {

                                val (key, cb, parent) = cbn

                                val type = parent.getChildType(key)
                                if (type == null) {
                                    information("Failed to find a the type of the child $key for $parent")
                                    return@action
                                }

                                if (cb != null && showOverwriteWithRefWarning != false) {
                                    warning(
                                        "Do you want to overwrite it with a reference to another object?",
                                        "This property is already defined: $cb",
                                        owner = currentWindow,
                                        buttons = *arrayOf(ButtonType.YES, YES_DISABLE_WARNING, ButtonType.CANCEL),
                                        actionFn = { button ->
                                            //hitting esc/closing window also counts as cancel
                                            if (button == ButtonType.CANCEL) {
                                                return@action
                                            } else if (button == YES_DISABLE_WARNING) {
                                                showOverwriteWithRefWarning = false
                                            }
                                            //ButtonType.YES falls through
                                        }
                                    )
                                }

                                val selector: ReferenceSelectorView = find("controller" to controller)
                                val ref = selector.createReference(type, key, parent)

                                if (ref == null) {
                                    warning(
                                        "No reference returned",
                                        "No reference was returned from the search. This could be because you canceled the search (pressed escape) or because the chosen class builder was invalid."
                                    )
                                    return@action
                                }

                                parent[key] = ref

                                cbn.item.children.clear()
                                reload()
                            }
                        } else {
                            isDisable = true
                        }
                    }
                    val coll = this.checkmenuitem("Collapse All Children") {
                        selectedProperty().addListener { _, _, newValue -> Settings.collapseChildren = newValue }
                        isSelected = Settings.collapseChildren!!
                    }

                    setOnHiding {
                        this.items.setAll(refItem, coll)
                    }

                    setOnShowing {
                        cbn.parent.createChildContextItems(cbn.key, this, controller)
                    }

                }
            }
        }
    }
}
