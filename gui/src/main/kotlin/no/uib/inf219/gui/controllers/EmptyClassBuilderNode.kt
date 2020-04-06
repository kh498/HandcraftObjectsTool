package no.uib.inf219.gui.controllers

import javafx.scene.control.TreeItem
import no.uib.inf219.gui.backend.ClassBuilder
import no.uib.inf219.gui.backend.ParentClassBuilder

/**
 * A class builder node where the class builder is always null aka empty
 */
data class EmptyClassBuilderNode(
    override val key: ClassBuilder,
    override val parent: ParentClassBuilder
) : ClassBuilderNode {

    override val item: TreeItem<ClassBuilderNode> = TreeItem(this)
    override val cb: Nothing? = null

    override fun ensurePresentClassBuilder(): FilledClassBuilderNode {
        val cb = parent.createChildClassBuilder(key, item = item)
        return cb.node
    }

    override fun resetClassBuilder(restoreDefault: Boolean): EmptyClassBuilderNode {
        //nothing to reset the cb is always null
        return this
    }

    override fun toString(): String {
        return "EmptyClassBuilderNode(key=$key, parent=$parent)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmptyClassBuilderNode

        if (key != other.key) return false
        if (parent != other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + parent.hashCode()
        return result
    }
}
