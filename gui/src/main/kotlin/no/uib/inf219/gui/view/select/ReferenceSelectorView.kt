package no.uib.inf219.gui.view.select

import com.fasterxml.jackson.databind.JavaType
import no.uib.inf219.extra.findChild
import no.uib.inf219.gui.backend.ClassBuilder
import no.uib.inf219.gui.backend.ParentClassBuilder
import no.uib.inf219.gui.backend.ReferenceClassBuilder
import no.uib.inf219.gui.controllers.classBuilderNode.FilledClassBuilderNode
import java.util.*
import kotlin.collections.HashSet

/**
 * @author Elg
 */
class ReferenceSelectorView : SelectorView<ClassBuilder>("Reference") {

    override val promptText = "Class builder name"


    override fun cellText(elem: ClassBuilder): String {
        val list = LinkedList<ClassBuilder>()
        list.add(elem)

        while (true) {
            val curr = list[0]
            val parent = curr.parent
            if (curr === parent) {
                break
            }

            list.add(0, parent)
        }
        list.removeAt(0)
        
        val path = list.joinToString(separator = " | ") { it.key.getPreviewValue() }

        return "${elem.getPreviewValue()} [$path]"
    }

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
        tornadofx.runAsync {
            searchResult.setAll(
                findInstancesOf(
                    type,
                    controller.root
                ).filter { it != parent.getChild(key) })
            searching = false
        }
        openModal(block = true, owner = currentWindow, escapeClosesWindow = false)

        val ref = result ?: return null
        val item = parent.item.findChild(key)

        return ReferenceClassBuilder(ref.key, ref.parent, key, parent, item).also {
            item.value = FilledClassBuilderNode(key, it, parent, item = item)
        }
    }

    companion object {

        internal fun findInstancesOf(
            type: JavaType,
            cb: ClassBuilder
        ): Set<ClassBuilder> {

            //the set to hold all children of this class builder. Use set to prevent duplicates
            val allChildren = HashSet<ClassBuilder>()
            allChildren.add(cb) //remember to also add the parent
            if (cb is ParentClassBuilder) {
                for (child in cb.getChildren()) {
                    allChildren.addAll(
                        findInstancesOf(
                            type,
                            child
                        )
                    )
                }
            }

            //find all children that is the correct type
            // and isn't a ReferenceClassBuilder to prevent cycles
            return allChildren.filter { it.type.isTypeOrSubTypeOf(type.rawClass) && it !is ReferenceClassBuilder }
                .toSet()
        }
    }
}