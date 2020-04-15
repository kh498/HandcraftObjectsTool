package no.uib.inf219.gui.backend

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.TreeItem
import no.uib.inf219.extra.onChange
import no.uib.inf219.extra.textCb
import no.uib.inf219.gui.backend.serializers.ParentClassBuilderSerializer
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.controllers.classBuilderNode.ClassBuilderNode
import no.uib.inf219.gui.loader.ClassInformation
import tornadofx.hbox
import tornadofx.onDoubleClick
import tornadofx.toProperty

/**
 * A reference to another class builder.
 *
 * @author Elg
 */
@JsonSerialize(using = ParentClassBuilderSerializer::class)
class ReferenceClassBuilder(
    private val refKey: ClassBuilder,
    private val refParent: ParentClassBuilder,
    override val key: ClassBuilder,
    override val parent: ParentClassBuilder,
    override val item: TreeItem<ClassBuilderNode>
) : ClassBuilder {

    override val serObject: ClassBuilder
        get() = refParent.getChild(refKey)
            ?: error("Failed to find a serObject with the given reference parent and ref key. Cannot make a reference to a null class builder")

    override val property: ClassInformation.PropertyMetadata? = parent.getChildPropertyMetadata(key)
    override val type: JavaType = serObject.type
    override val serObjectObservable = serObject.toProperty()

    init {
        //TODO test if reference get reconnected if the referencing object is reset
        // test: If ref is nulled out this should also be nulled out

        require(refKey != key || refParent !== parent) {
            "Direct cycle detected, the object we're serializing is this!"
        }

        require(serObject !is ReferenceClassBuilder || !parent.isLeaf()) {
            //TODO find out if reference chaining should be allowed. If not update this message with why.
            "Chain of cb references is not supported as of now"
        }

//            //run this later, the parent need to have time to assign this as a child to it self
//            require(parent.getChild(key) === this) { "Parent with given key does not give this class builder" }

        refParent.serObjectObservable.onChange {
            if (refParent.getChild(refKey) == null) {
                //it was completely removed, this should be removed from the parent
                refParent.serObjectObservable.removeListener(this)
                parent.resetChild(key, this@ReferenceClassBuilder, restoreDefault = true)
                println("removed parent")
            }
        }
    }

    override fun createEditView(parent: EventTarget, controller: ObjectEditorController): Node {
        return parent.hbox {
            alignment = Pos.CENTER

            onDoubleClick {
                controller.select(serObject)
            }

            textCb(serObject) {
                "This class builder is only a reference to ${getPreviewValue()}. Double click to edit the referenced class builder."
            }
        }
    }

    override fun getPreviewValue() =
        "Ref to ${serObject.key.getPreviewValue()} property of ${serObject.parent.key.getPreviewValue()}"

    override fun isLeaf(): Boolean = true
    override fun isImmutable() = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReferenceClassBuilder) return false

        //ser objects must be same object
        if (serObject !== other.serObject) return false
        if (parent != other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serObject.hashCode()
        result = 31 * result + parent.hashCode()
        return result
    }

    override fun toString(): String {
        return "Ref CB; ref child $refKey of $refParent)"
    }
}
