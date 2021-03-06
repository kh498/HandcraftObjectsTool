/*
 * Copyright 2020 Karl Henrik Elg Barlinn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.uib.inf219.gui.view.select

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult
import no.uib.inf219.extra.onChange
import no.uib.inf219.gui.Styles
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.ems
import tornadofx.SortedFilteredList
import tornadofx.View
import tornadofx.addClass
import tornadofx.asObservable
import tornadofx.borderpane
import tornadofx.getValue
import tornadofx.hbox
import tornadofx.listview
import tornadofx.onUserSelect
import tornadofx.replaceWith
import tornadofx.runLater
import tornadofx.selectedItem
import tornadofx.setValue
import tornadofx.style
import tornadofx.text
import tornadofx.textfield
import tornadofx.vbox
import kotlin.math.max

/**
 * @author Elg
 */
abstract class SelectorView<T>(title: String) : View(title) {

    protected val searchResult = ArrayList<T>().asObservable()
    protected val filteredData = SortedFilteredList(searchResult)

    /**
     * The [T] selected by the use when closing the dialog
     */
    protected var result: T? = null

    val controller: ObjectEditorController by param()

    protected val searchingProperty = SimpleBooleanProperty()
    protected var searching by searchingProperty
    protected val label: Node
    protected lateinit var listview: ListView<T>
    protected lateinit var textLabelProperty: StringProperty
    protected val resultList: VBox

    final override val root = borderpane()

    abstract fun cellText(elem: T): String
    abstract val promptText: String
    abstract fun confirmAndClose()

    init {
        with(root) {

            style {
                minWidth = 45.ems
                minHeight = 25.ems
            }

            label = hbox {
                alignment = Pos.CENTER
                text(ClassSelectorView.SEARCHING) {
                    addClass(Styles.largefont)

                    searchingProperty.onChange {
                        runLater {
                            text = if (searching) ClassSelectorView.SEARCHING else ClassSelectorView.NO_SUBCLASSES_FOUND
                        }
                    }
                }
            }

            resultList = vbox {

                addClass(Styles.parent)

                textfield {
                    promptText = this@SelectorView.promptText
                    textLabelProperty = textProperty()

                    textProperty().onChange {
                        if (text.length >= 3) {

                            val sorted: List<BoundExtractedResult<T>> =
                                FuzzySearch.extractTop(
                                    textLabelProperty.value,
                                    filteredData,
                                    { cellText(it).replace('.', ' ') },
                                    max(filteredData.size * 0.10, 500.0).toInt()
                                )
                                    ?: return@onChange

                            val map: Map<T, BoundExtractedResult<T>> = sorted.map { it.referent to it }.toMap()

                            runLater {
                                filteredData.predicate = {
                                    map.containsKey(it)
                                }

                                filteredData.sortedItems.setComparator { a, b ->
                                    val aScore = map[a]?.score ?: -1
                                    val bScore = map[b]?.score ?: -1
                                    return@setComparator bScore - aScore
                                }

                                listview.scrollTo(0)
                            }
                        } else {
                            runLater {
                                filteredData.predicate = { _ -> true }
                            }
                        }
                    }
                }

                listview = listview(filteredData.sortedItems)

                with(listview) {

                    setOnMouseClicked {
                        result = listview.selectedItem
                    }

                    // close when pressing enter and something is selected or double clicking
                    onUserSelect(2) {
                        result = it
                        confirmAndClose()
                    }

                    cellFormat {
                        runLater {
                            text = cellText(it)
                        }
                    }

                    addEventHandler(KeyEvent.ANY) { event ->
                        if (event.code == KeyCode.ESCAPE) {
                            result = null
                            close()
                        }
                    }
                }
            }

            searchingProperty.onChange {
                runLater {
                    if (searching) {
                        center.replaceWith(label, sizeToScene = true, centerOnScreen = true)
                        this@SelectorView.title = "Searching..."
                    } else {
                        center.replaceWith(resultList, sizeToScene = true, centerOnScreen = true)
                        this@SelectorView.title = promptText
                    }
                }
            }
            center = label
        }
    }
}
