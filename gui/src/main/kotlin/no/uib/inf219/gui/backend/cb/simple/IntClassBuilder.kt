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

import javafx.scene.control.TreeItem
import javafx.util.converter.IntegerStringConverter
import no.uib.inf219.gui.backend.cb.api.ClassBuilder
import no.uib.inf219.gui.backend.cb.api.ParentClassBuilder
import no.uib.inf219.gui.backend.cb.api.SimpleNumberClassBuilder
import no.uib.inf219.gui.controllers.cbn.ClassBuilderNode
import no.uib.inf219.gui.loader.ClassInformation

class IntClassBuilder(
    initial: Int = 0,
    key: ClassBuilder?,
    parent: ParentClassBuilder?,
    property: ClassInformation.PropertyMetadata? = null,
    immutable: Boolean = false,
    item: TreeItem<ClassBuilderNode>
) : SimpleNumberClassBuilder<Int>(
    Int::class,
    initial,
    key,
    parent,
    property,
    immutable,
    IntegerStringConverter(),
    item
)
