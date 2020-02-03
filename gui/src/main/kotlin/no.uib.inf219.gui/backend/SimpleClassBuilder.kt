package no.uib.inf219.gui.backend

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ser.PropertyWriter
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.util.StringConverter
import javafx.util.converter.*
import no.uib.inf219.gui.Styles
import no.uib.inf219.gui.controllers.ObjectEditorController
import no.uib.inf219.gui.converter.UUIDStringConverter
import no.uib.inf219.gui.loader.ClassInformation
import tornadofx.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * A class builder intended for primitive classes to be used as leaf nodes in the class builder tree.
 *
 * Every sub-class probably want to override
 *
 * @author Elg
 */
abstract class SimpleClassBuilder<T : Any> internal constructor(
    primClass: Class<T>,
    private val initialValue: T,
    override val parent: ClassBuilder<*>,
    override val name: String? = null,
    override val property: PropertyWriter?,
    private val converter: StringConverter<T>? = null
) : ClassBuilder<T> {

    override val type: JavaType = ClassInformation.toJavaType(primClass)

    private val valueProperty: SimpleObjectProperty<T> by lazy { SimpleObjectProperty<T>() }
    fun valueProperty(): ObservableValue<T> = valueProperty
    var value: T
        get() = valueProperty.get()
        set(value) = valueProperty.set(value)

    init {
        value = initialValue
    }

    override fun toObject(): T = value

    override fun getSubClassBuilders(): Map<String, ClassBuilder<*>> {
        return emptyMap()
    }

    override fun toView(
        parent: EventTarget,
        controller: ObjectEditorController
    ): Node {
        return parent.vbox {
            addClass(Styles.parent)
            vbox {
                addClass(Styles.parent)
                label("Required? ${isRequired()}")
                label("Type: ${type.rawClass}")
            }
            textarea {
                bindStringProperty(textProperty(), converter, valueProperty)
            }
        }
    }

    override fun createClassBuilderFor(property: String): ClassBuilder<Any>? {
        return null
    }


    /**
     * Reset the value this holds to the [initialValue] provided in the constructor
     */
    override fun reset(property: String, element: ClassBuilder<*>?): ClassBuilder<*>? {
        require(element == null || element == this) { "Given element is not null or this" }
        value = initialValue
        return null
    }

    override fun isLeaf(): Boolean {
        return true
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getDefaultConverter(): StringConverter<T>? = when (type.rawClass) {
        Int::class.javaPrimitiveType -> IntegerStringConverter()
        Long::class.javaPrimitiveType -> LongStringConverter()
        Double::class.javaPrimitiveType -> DoubleStringConverter()
        Float::class.javaPrimitiveType -> FloatStringConverter()
        Date::class -> DateStringConverter()
        BigDecimal::class -> BigDecimalStringConverter()
        BigInteger::class -> BigIntegerStringConverter()
        Number::class -> NumberStringConverter()
        LocalDate::class -> LocalDateStringConverter()
        LocalTime::class -> LocalTimeStringConverter()
        LocalDateTime::class -> LocalDateTimeStringConverter()
        Boolean::class.javaPrimitiveType -> BooleanStringConverter()
        //non-default converts
        UUID::class -> UUIDStringConverter
        else -> null
    } as StringConverter<T>?

    private fun bindStringProperty(
        stringProperty: StringProperty,
        converter: StringConverter<T>?,
        property: ObservableValue<T>
    ) {
        if (stringProperty.isBound) stringProperty.unbind()

        ViewModel.register(stringProperty, property)

        @Suppress("UNCHECKED_CAST")
        if (type.isTypeOrSuperTypeOf(String::class.java)) when {
            else -> stringProperty.bindBidirectional(property as Property<String>)
        } else {
            val effectiveConverter = converter ?: getDefaultConverter<T>()
            when {
                effectiveConverter != null -> stringProperty.bindBidirectional(
                    property as Property<T>,
                    effectiveConverter
                )
                else -> throw IllegalArgumentException("Cannot convert from $type to String without an explicit converter")
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleClassBuilder<*>) return false

        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }


    override fun toString(): String {
        return "Simple CB; value=$value, clazz=$type)"
    }

    override fun previewValue(): String {
        return value.toString()
    }
}
