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

package no.uib.inf219.gui.backend.cb.api

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TextFormatter
import javafx.scene.control.TreeItem
import javafx.scene.input.MouseButton
import javafx.util.StringConverter
import no.uib.inf219.extra.removeNl
import no.uib.inf219.gui.Styles.Companion.numberChanger
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.controllers.cbn.ClassBuilderNode
import no.uib.inf219.gui.loader.ClassInformation
import no.uib.inf219.gui.view.LoggerView
import tornadofx.addClass
import tornadofx.button
import tornadofx.fitToParentHeight
import tornadofx.hbox
import tornadofx.style
import tornadofx.textfield
import tornadofx.vbox
import kotlin.reflect.KClass

/**
 * A simple class builder that handles all primitives that can be seen as a [Number] (except [Char])
 *
 * @author Elg
 */
abstract class SimpleNumberClassBuilder<T : Number>(
    primClass: KClass<T>,
    initialValue: T,
    key: ClassBuilder?,
    parent: ParentClassBuilder?,
    property: ClassInformation.PropertyMetadata?,
    immutable: Boolean,
    converter: StringConverter<T>,
    item: TreeItem<ClassBuilderNode>
) : SimpleClassBuilder<T>(primClass, initialValue, key, parent, property, immutable, converter, item) {

    override fun createEditView(
        parent: EventTarget,
        controller: ObjectEditorController
    ): Node {
        return parent.hbox {

            vbox {
                style {
                    fitToParentHeight()
                }
                button("+") {
                    style {
                        addClass(numberChanger)
                    }
                    setOnMouseClicked { event ->
                        if (event.button == MouseButton.PRIMARY) {

                            var num = 1
                            if (event.isShiftDown) num *= 10
                            if (event.isControlDown) num *= 100

                            @Suppress("UNCHECKED_CAST")
                            serObject = when (serObject::class) {
                                Int::class -> (serObject as Int).plus(num) as T
                                Long::class -> (serObject as Long).plus(num) as T
                                Double::class -> (serObject as Double).plus(num) as T
                                Float::class -> (serObject as Float).plus(num) as T
                                Short::class -> (serObject as Short).plus(num).toShort() as T
                                Byte::class -> (serObject as Byte).plus(num).toByte() as T
                                else -> throw IllegalStateException("Unknown number ${serObject::class.java.simpleName}")
                            }
                        }
                    }
                }
                button("-") {
                    style {
                        addClass(numberChanger)
                    }
                    setOnMouseClicked { event ->
                        if (event.button == MouseButton.PRIMARY) {

                            var num = 1
                            if (event.isShiftDown) num *= 10
                            if (event.isControlDown) num *= 100
                            @Suppress("UNCHECKED_CAST")
                            serObject = when (serObject::class) {
                                Int::class -> (serObject as Int).minus(num) as T
                                Long::class -> (serObject as Long).minus(num) as T
                                Double::class -> (serObject as Double).minus(num) as T
                                Float::class -> (serObject as Float).minus(num) as T
                                Short::class -> (serObject as Short).minus(num).toShort() as T
                                Byte::class -> (serObject as Byte).minus(num).toByte() as T
                                else -> throw IllegalStateException("Unknown number ${serObject::class.java.simpleName}")
                            }
                        }
                    }
                }
            }
            textfield {
                textFormatter = TextFormatter<Short> {

                    val text = it.controlNewText.removeNl().trim()
                    if (it.isContentChange && text.isNotEmpty() && !validate(text)) {
                        LoggerView.log { "Failed to parse '$text' to ${this@SimpleNumberClassBuilder.type.rawClass.simpleName}" }
                        return@TextFormatter null
                    }
                    return@TextFormatter it
                }
                bindStringProperty(textProperty(), converter, serObjectObservable)
            }
            button("reset") {
                setOnAction {

                    @Suppress("UNCHECKED_CAST")
                    serObject = when (serObject::class) {
                        Int::class -> 0 as T
                        Long::class -> 0L as T
                        Double::class -> 0.0 as T
                        Float::class -> 0f as T
                        Short::class -> (0.toShort()) as T
                        Byte::class -> (0.toByte()) as T
                        else -> throw IllegalStateException("Unknown number ${serObject::class.java.simpleName}")
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return "Simple Number CB; value=$serObject, clazz=$type)"
    }
}
