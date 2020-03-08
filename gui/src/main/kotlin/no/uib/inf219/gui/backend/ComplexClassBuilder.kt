package no.uib.inf219.gui.backend

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.PropertyWriter
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.text.TextAlignment
import no.uib.inf219.extra.toCb
import no.uib.inf219.gui.Styles
import no.uib.inf219.gui.backend.primitive.StringClassBuilder
import no.uib.inf219.gui.backend.serializers.ComplexClassBuilderSerializer
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.loader.ClassInformation
import no.uib.inf219.gui.view.ControlPanelView.mapper
import no.uib.inf219.gui.view.OutputArea
import tornadofx.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * A class builder intended to be used for normal classes. It is 'complex' due containing multiple other [ClassBuilder]s.
 *
 * @author Elg
 */
@JsonSerialize(using = ComplexClassBuilderSerializer::class)
class ComplexClassBuilder<out T>(
    override val type: JavaType,
    override val name: String,
    override val parent: ClassBuilder<*>? = null,
    override val property: PropertyWriter? = null
) : ClassBuilder<T> {

    //TODO make all property info, default etc into a single class

    /**
     * Hold information about the given property
     */
    val propInfo: Map<String, PropertyWriter>

    /**
     * Holds the default value for the given property
     */
    private val propDefaults: MutableMap<String, Any?> = HashMap()

    /**
     * Information about the generics of [T], is `null` when the class does not have a generic type
     */
    val typeSerializer: TypeSerializer?

    override val serObject = HashMap<String, ClassBuilder<*>?>()
    override val serObjectProperty = serObject.toProperty()

    init {
        val (typeSer, pinfo) = ClassInformation.serializableProperties(type)
        typeSerializer = typeSer
        propInfo = pinfo

        //initiate all valid values to null or default
        // to allow for iteration when populating Node explorer
        for ((key, v) in propInfo) {
            val propAn = v.getAnnotation(JsonProperty::class.java)
            val default: Any? =
                if (propAn != null) {
                    val defaultStr = propAn.defaultValue
                    if (defaultStr.isEmpty()) {
                        null
                    } else {
                        try {
                            mapper.readValue(defaultStr, v.type) as Any?
                        } catch (e: Throwable) {
                            OutputArea.logln("Failed to parse default value for property $key of $type. Given string '$defaultStr'")
                            OutputArea.logln(e.localizedMessage)
                            null
                        }
                    }
                } else null
            propDefaults[key] = default

            if (default != null || v.type.isPrimitive) {
                //only create a class builder for properties that has a default value
                // or is primitive (which always have default values)
                createClassBuilderFor(key.toCb())
            } else {
                this.serObject[key] = null
            }
        }
    }

    private fun cbToString(cb: ClassBuilder<*>?): String {
        return (cb as? StringClassBuilder)?.serObject
            ?: kotlin.error("Wrong type of key was given. Expected a ClassBuilder<String> but got $cb")
    }

    override fun createClassBuilderFor(key: ClassBuilder<*>, init: ClassBuilder<*>?): ClassBuilder<*>? {
        val propName = cbToString(key)

        val prop = propInfo[propName]
        require(prop != null) { "The class $type does not have a property with the name '$propName'. Expected one of the following: ${propInfo.keys}" }
        require(init == null || init.type == getChildType(key)) {
            "Given initial value have different type than expected. expected ${getChildType(key)} got ${init?.type}"
        }

        return this.serObject.computeIfAbsent(propName) {
            init ?: getClassBuilder(prop.type, propName, propDefaults[propName], prop)
        }
    }

    override fun resetChild(key: ClassBuilder<*>, element: ClassBuilder<*>?) {
        val propName = cbToString(key)

        require(element == null || element == propInfo[propName]) {
            "The class $type does not have a property with the name '$key'. Expected one of the following: $propInfo"
        }

        val remove = element?.reset() ?: false
        if (remove) this.serObject[propName] = null
    }

    override fun reset(): Boolean {
        for (prop in this.serObject.keys) {
            resetChild(prop.toCb())
        }
        return false
    }

    override fun toView(
        parent: EventTarget,
        controller: ObjectEditorController
    ): Node {
        return parent.scrollpane(fitToWidth = true, fitToHeight = true) {

            if (this@ComplexClassBuilder.serObject.isEmpty()) {
                textflow {
                    textAlignment = TextAlignment.CENTER

                    label("Class ")
                    label(type.rawClass.canonicalName) { font = Styles.monospaceFont }
                    label(" have no simple serializable properties")
                }
            } else {
                squeezebox {
                    for ((name, cb) in this@ComplexClassBuilder.serObject) {
                        if (cb != null) {
                            fold("$name: ${cb.getPreviewValue()}") {
                                cb.toView(this, controller)
                            }
                        } else {
                            fold("$name: (null)") {
                                val createdCB = createClassBuilderFor(name.toCb())
                                controller.reloadView()
                                if (createdCB != null) {
                                    controller.select(createdCB)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getChildType(cb: ClassBuilder<*>): JavaType? {
        return propInfo[cbToString(cb)]?.type
    }

    override fun getPreviewValue(): String {
        return this.serObject.map { it.key + ": " + it.value?.getPreviewValue() }.joinToString(", ")
    }

    override fun getSubClassBuilders(): Map<ClassBuilder<*>, ClassBuilder<*>?> =
        this.serObject.mapKeys { it.key.toCb() }

    override fun isLeaf(): Boolean = false

    override fun isImmutable(): Boolean = false

    override fun toString(): String {
        return "Complex CB; type=$type)"
    }

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapClassBuilder<*, *>) return false

        if (type != other.type) return false
        if (parent != other.parent) return false
        if (name != other.name) return false
        if (property != other.property) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (property?.hashCode() ?: 0)
        return result
    }
}
