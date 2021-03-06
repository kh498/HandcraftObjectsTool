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

package no.uib.inf219.gui.backend.cb.simple

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TextFormatter
import javafx.scene.control.TreeItem
import javafx.util.converter.CharacterStringConverter
import no.uib.inf219.gui.backend.cb.api.ClassBuilder
import no.uib.inf219.gui.backend.cb.api.ParentClassBuilder
import no.uib.inf219.gui.backend.cb.api.SimpleClassBuilder
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.controllers.cbn.ClassBuilderNode
import no.uib.inf219.gui.loader.ClassInformation
import no.uib.inf219.gui.view.LoggerView
import org.apache.commons.text.StringEscapeUtils
import tornadofx.textfield

class CharClassBuilder(
    initial: Char = '\u0000',
    key: ClassBuilder,
    parent: ParentClassBuilder,
    property: ClassInformation.PropertyMetadata? = null,
    immutable: Boolean = false,
    item: TreeItem<ClassBuilderNode>
) : SimpleClassBuilder<Char>(
    Char::class,
    initial,
    key,
    parent,
    property,
    immutable,
    CharacterStringConverter(),
    item
) {

    override fun validate(text: String): Boolean {
        return StringEscapeUtils.unescapeJava(text).length == 1
    }

    override fun createEditView(
        parent: EventTarget,
        controller: ObjectEditorController
    ): Node {
        return parent.textfield {
            textFormatter = TextFormatter<Char> {

                val text: String = it?.controlNewText ?: return@TextFormatter null

                if (it.isContentChange && text.isNotEmpty() && !validate(text)) {
                    LoggerView.log { "Failed to parse '$text' to ${this@CharClassBuilder.type.rawClass.simpleName}" }
                    return@TextFormatter null
                }
                return@TextFormatter it
            }
            bindStringProperty(textProperty(), converter, serObjectObservable)
        }
    }
}
