package no.uib.inf219.gui.backend.simple

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.util.StringConverter
import no.uib.inf219.gui.backend.ClassBuilder
import no.uib.inf219.gui.backend.SimpleClassBuilder
import no.uib.inf219.gui.loader.ClassInformation
import tornadofx.combobox
import tornadofx.onChange
import java.lang.reflect.Field

/**
 * @author Elg
 */
class EnumClassBuilder<T : Enum<*>>(
    clazz: Class<T>,
    initialValue: T? = null,
    name: ClassBuilder<*>? = null,
    parent: ClassBuilder<*>? = null,
    property: ClassInformation.PropertyMetadata? = null
) : SimpleClassBuilder<T>(
    clazz,
    initialValue ?: getDefaultEnumValue(clazz),
    name,
    parent,
    property,
    false,
    EnumConverter(clazz)
) {

    private val enumValues = findEnumValues(clazz)

    override fun editView(parent: Pane): Node {
        return parent.combobox(
            property = serObjectObservable,
            values = enumValues
        ) {
            //Select the initial value as the first one
            value = initialValue

            selectionModel.selectedItemProperty().onChange {
                print("new $it")
            }

            setOnKeyPressed { event ->
                //find the first enum that starts with the given text and make it the selected value
                enumValues.find {
                    it.name.startsWith(event.text, true)
                }.also {
                    selectionModel.select(it)
                }
            }
        }
    }

    companion object {

        /**
         * When [enumValues<T>] cannot be used
         */
        fun <T : Enum<*>> findEnumValues(enumClass: Class<T>): List<T> {
            require(enumClass.isEnum) { "Given class is not an enum class" }
            @Suppress("UNCHECKED_CAST")
            return enumClass.enumConstants.sortedBy { it.name }
        }

        fun <T : Enum<*>> getDefaultEnumValue(enumClass: Class<T>): T {
            val enumConstants = findEnumValues(enumClass)
            for (value in enumConstants) {
                val field: Field = enumClass.getField(value.name)
                if (field.isAnnotationPresent(JsonEnumDefaultValue::class.java)) {
                    return value
                }
            }
            return enumConstants[0]
        }
    }

    internal class EnumConverter<T : Enum<*>>(clazz: Class<T>) : StringConverter<T>() {

        //key is nullable to allow for shorter fromString :)
        private val values: Map<String?, T> = findEnumValues(clazz).map { it.name to it }.toMap()

        override fun toString(enum: T?) = enum?.name

        override fun fromString(name: String?) = values[name]
    }
}
