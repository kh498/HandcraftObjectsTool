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

import com.fasterxml.jackson.databind.JavaType
import no.uib.inf219.extra.findChild
import no.uib.inf219.extra.isTypeOrSuperTypeOfPrimAsObj
import no.uib.inf219.gui.backend.cb.api.ClassBuilder
import no.uib.inf219.gui.backend.cb.api.ParentClassBuilder
import no.uib.inf219.gui.backend.cb.checkNoCycle
import no.uib.inf219.gui.backend.cb.node
import no.uib.inf219.gui.backend.cb.path
import no.uib.inf219.gui.backend.cb.reference.ReferenceClassBuilder
import no.uib.inf219.gui.controllers.cbn.FilledClassBuilderNode

/**
 * @author Elg
 */
class ReferenceSelectorView : SelectorView<ClassBuilder>("Reference") {

    override val promptText = "Class builder name"

    override fun cellText(elem: ClassBuilder) = "${elem.getPreviewValue()} [${elem.path}]"

    override fun confirmAndClose() {
        close()
    }

    fun createReference(
        type: JavaType,
        key: ClassBuilder,
        parent: ParentClassBuilder
    ): ReferenceClassBuilder? {

        result = null
        searching = true
        searchResult.clear()
        val currSerObj = parent[key]
        searchResult.setAll(
            findInstancesOf(
                type,
                controller.root
            ).filter {
                it !== currSerObj && (currSerObj == null || it.checkNoCycle(key, parent))
            }
        )
        searching = false

        openModal(block = true, owner = currentWindow, escapeClosesWindow = false)

        val ref = result ?: return null
        val item = parent.item.findChild(key)
        require(item.value.allowReference) { "Somehow you selected a value that does not allow references! $ref" }

        return ReferenceClassBuilder(ref.key, ref.parent, key, parent, item).also {
            item.value = FilledClassBuilderNode(key, it, parent, item, true)
        }
    }

    companion object {

        internal fun findInstancesOf(
            wantedType: JavaType,
            cb: ClassBuilder
        ): Set<ClassBuilder> {

            // the set to hold all children of this class builder. Use set to prevent duplicates
            val allChildren = HashSet<ClassBuilder>()
            allChildren.add(cb) // remember to also add the parent
            if (cb is ParentClassBuilder) {
                for ((_, child) in cb.getChildren()) {
                    if (child == null) continue
                    allChildren.addAll(findInstancesOf(wantedType, child))
                }
            }

            // find all children that is the correct type
            return allChildren.filter {
                it.type.isTypeOrSuperTypeOfPrimAsObj(wantedType.rawClass) && it.node.allowReference
            }.toSet()
        }
    }
}
