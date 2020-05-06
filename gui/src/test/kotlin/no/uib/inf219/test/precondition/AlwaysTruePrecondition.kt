package no.uib.inf219.test.precondition

/**
 * @author Elg
 */

class AlwaysTruePrecondition : Precondition {
    override fun check(): Boolean {
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
