package no.uib.inf219.gui.backend.primitive

import com.fasterxml.jackson.databind.ser.PropertyWriter
import javafx.util.converter.IntegerStringConverter
import no.uib.inf219.gui.backend.ClassBuilder
import no.uib.inf219.gui.backend.SimpleNumberClassBuilder

class IntClassBuilder(
    initial: Int = 0,
    name: String,
    parent: ClassBuilder<*>? = null,
    prop: PropertyWriter? = null
) :
    SimpleNumberClassBuilder<Int>(
        Int::class.java, initial, name, parent, prop,
        IntegerStringConverter()
    ) {}
