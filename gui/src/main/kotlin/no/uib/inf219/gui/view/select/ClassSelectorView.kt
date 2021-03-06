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
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ButtonType.CANCEL
import javafx.scene.control.ButtonType.NO
import javafx.scene.control.ButtonType.OK
import javafx.scene.control.ButtonType.YES
import no.uib.inf219.extra.ENABLE_MODULE
import no.uib.inf219.extra.OK_DISABLE_WARNING
import no.uib.inf219.extra.onChange
import no.uib.inf219.gui.Settings.showMrBeanWarning
import no.uib.inf219.gui.ems
import no.uib.inf219.gui.loader.DynamicClassLoader
import no.uib.inf219.gui.view.ControlPanelView.mrBeanModule
import no.uib.inf219.gui.view.select.ClassSelectorView.Companion.StylizedClass
import tornadofx.FX
import tornadofx.action
import tornadofx.booleanProperty
import tornadofx.confirmation
import tornadofx.contextmenu
import tornadofx.disableWhen
import tornadofx.error
import tornadofx.getValue
import tornadofx.information
import tornadofx.item
import tornadofx.runLater
import tornadofx.setValue
import tornadofx.style
import tornadofx.text
import tornadofx.textflow
import tornadofx.warning
import java.lang.reflect.Modifier.ABSTRACT
import java.lang.reflect.Modifier.FINAL
import java.lang.reflect.Modifier.STATIC
import java.lang.reflect.Modifier.SYNCHRONIZED
import java.lang.reflect.Modifier.isAbstract
import java.lang.reflect.Modifier.isFinal
import java.lang.reflect.Modifier.isInterface
import java.lang.reflect.Modifier.toString

/**
 * A view to allow for selection of classes via a GUI. It is near identical to how this works in IntelliJ IDEA.
 *
 * The opened GUI will use [ClassGraph] to find all subclasses (but no interfaces) of
 *
 * ## Example usage
 *
 * ```kotlin
 * val subclass = tornadofx.find<ClassSelectorView>().subtypeOf(Any::class.type())
 * ```
 *
 * @author Elg
 */
class ClassSelectorView : SelectorView<StylizedClass>("Select implementation") {

    /**
     * If we the current [result] class the the class we want to return
     */
    private val finishedSearching = booleanProperty()

    private val superClassProperty = SimpleStringProperty()
    private var superClass by superClassProperty

    override fun cellText(elem: StylizedClass): String {
        return elem.displayName
    }

    override val promptText = "Full class name"

    private val resultType get() = DynamicClassLoader.loadType(result?.className)

    init {
        with(root) {
            resultList.children.add(
                0,
                textflow().apply {
                    fun updateText(): String {
                        return "Choose subclass of $superClass (${filteredData.filteredItems.size} / ${searchResult.size} found)"
                    }

                    textflow().text(updateText()) {
                        style { fontSize = 1.5.ems }
                        superClassProperty.onChange { text = updateText() }
                        searchResult.onChange { text = updateText() }
                        filteredData.filteredItems.onChange { text = updateText() }
                    }
                }
            )
        }

        listview.contextmenu {

            item("Choose class") {
                disableWhen(listview.selectionModel.selectedItemProperty().isNull)
                action {
                    confirmAndClose()
                }
            }
            item("Find subclass") {
                val selProp = listview.selectionModel.selectedItemProperty()
                disableWhen(
                    selProp.isNull.or(
                        SimpleBooleanProperty(false).apply {
                            selProp.addListener { _, _, newValue ->
                                this.set(newValue == null || isFinal(newValue.modifiers))
                            }
                        }
                    )
                )
                action {
                    findSubType()
                }
            }
        }
    }

    private fun findSubType() {
        require(!(resultType?.isFinal ?: true)) {
            "Cannot find result type of null or a final class! Given : $resultType"
        }
        finishedSearching.set(false)
        close()
    }

    override fun confirmAndClose() {

        fun returnSelectedType() {
            finishedSearching.set(true)
            close()
        }

        val realResult = resultType
        if (realResult != null) {

            when {
                realResult.isAbstract -> {
                    if (!mrBeanModule.enabled) {
                        if (showMrBeanWarning == true) {
                            information(
                                "Cannot select an abstract class when the Mr Bean module is not enabled.",
                                "You will now be asked to select a subclass of ${realResult.rawClass}",
                                owner = currentWindow,
                                buttons = *arrayOf(OK, CANCEL, OK_DISABLE_WARNING, ENABLE_MODULE),
                                actionFn = {
                                    when (it) {
                                        OK_DISABLE_WARNING -> showMrBeanWarning = false
                                        ENABLE_MODULE -> {
                                            mrBeanModule.enabled = true
                                            confirmAndClose()
                                            return
                                        }
                                        else -> return
                                    }
                                }
                            )
                        }

                        // mr bean is not enabled so we cannot return abstract types
                        findSubType()
                        return
                    }
                    val resultType: String = if (realResult.isInterface) "interface" else "abstract class"
                    val contentInfo: String =
                        "The class you have selected is either an interface or an abstract class.\n" +
                            "You can select an abstract type as the Mr Bean module is enabled in the settings."

                    confirmation(
                        "Do you want to return the selected $resultType ${realResult.rawClass?.name}?",
                        "$contentInfo\n" +
                            "\n" +
                            "If you choose YES you will select this class and the dialogue will close.\n" +
                            "If you choose NO then you will be asked to select a subclass of this class.\n" +
                            "If you select CANCEL no choice will be made can you are free to choose another class.",
                        title = "Return the selected $resultType ${realResult.rawClass?.name}?",
                        owner = currentWindow,
                        buttons = *arrayOf(YES, NO, CANCEL),
                        actionFn = {
                            when (it) {
                                YES -> returnSelectedType() // return the abstract class
                                NO -> findSubType() // find an impl of the selected class
                                CANCEL -> return // Return to search , do not select the class
                            }
                        }
                    )
                }
            }

            // default to returning selected class
            returnSelectedType()
        }
    }

    /**
     * Ask the user to select a subclass of the given [superType]. This method will block till the class is selected or the user cancels the search (in which case the returned value will be `null`)
     *
     * @param superType The super class to find the subclasses to
     * @param showAbstract If abstract classes should be listed
     *
     * @return A user selected subtype of [superType]. The returned type is guaranteed to not be abstract if [showAbstract] is `false`. `null` might be returned if the user cancels the search (it closing the selection window)
     *
     * @throws IllegalArgumentException if the super type is not allowed. A type is not allowed if it is a primitive class, final class or does not have a canonical name (such as local and anonymous classes)
     * @throws IllegalArgumentException if the given types does not have a java class associated with it
     */
    fun subtypeOf(superType: JavaType, showAbstract: Boolean = false): JavaType? {
        result = superType.toStylizedClass()
        finishedSearching.value = false
        do {
            val rt = resultType
            if (rt == null) {
                error("Failed to find the java type of $result", owner = FX.primaryStage)
                return null
            }
            tornadofx.runAsync {
                searchForSubtypes(rt, showAbstract)
            }
            openModal(block = true, owner = currentWindow, escapeClosesWindow = true)
        } while (result != null && !finishedSearching.value)
        return resultType
    }

    /**
     * Update the list of subclasses to be those of [superType]. Unless doing something fancy it's recommended to use [subtypeOf]
     *
     * @param superType The super class to find the subclasses to
     * @param showAbstract If abstract classes should be listed
     */
    private fun searchForSubtypes(superType: JavaType, showAbstract: Boolean) {
        require(superType.rawClass != null) { "Given java '$superType' types does not have a raw class" }
        require(superType.rawClass.canonicalName != null) { "Given super class '${superType.toCanonical()}' must have a canonical name" }
        require(!superType.isPrimitive) { "Given super class '${superType.toCanonical()}' cannot be primitive" }
        require(!superType.isFinal) { "Given super class '${superType.toCanonical()}' cannot be final" }

        synchronized(this) {

            runLater {
                result = null
                superClass = superType.rawClass.name
                textLabelProperty.set("")
                searching = true
            }
            val superClass: Class<*> = superType.rawClass

            ClassGraph()
                .enableExternalClasses()
                .addClassLoader(DynamicClassLoader)
                .scan().use { scanResult ->
                    val cil: ClassInfoList =
                        when {
                            superClass == Any::class.java -> scanResult.allClasses
                            superClass.isInterface -> scanResult.getClassesImplementing(superClass.name)
                            else -> scanResult.getSubclasses(superClass.canonicalName)
                        }
                    val classes = cil.filter {
                        // Only show abstract types when wanted
                        // and never show annotations
                        (showAbstract || !isAbstract(it.modifiers)) && !it.isAnnotation
                    }.map {
                        it.toStylizedClass()
                    }

                    runLater {
                        if (classes.isEmpty()) {
                            val stylized = superType.toStylizedClass()
                            warning(
                                "No subclasses found for '${stylized.displayName}'",
                                "Do you want to return the super class ${stylized.displayName}, if not nothing will be returned.",
                                buttons = *arrayOf(YES, NO),
                                actionFn = {
                                    when (it) {
                                        YES -> this@ClassSelectorView.result = stylized
                                        NO -> this@ClassSelectorView.result = null
                                    }
                                    finishedSearching.set(true)
                                    this@ClassSelectorView.close()
                                }
                            )
                        } else {
                            searchResult.setAll(classes)
                        }
                    }
                }
            searching = false
        }
    }

    companion object {
        const val SEARCHING = "Searching..."
        const val NO_SUBCLASSES_FOUND = "No subclasses found"
        private const val UNWANTED_MODIFIERS = (SYNCHRONIZED or STATIC).inv()
        private const val ENUM_BIT = 0x00004000

        data class StylizedClass(val displayName: String, val className: String, val modifiers: Int)

        fun JavaType.toStylizedClass() = this.rawClass.toStylizedClass()

        fun Class<*>.toStylizedClass(): StylizedClass {
            return stylizedClassBuilder(this, name, modifiers)
        }

        fun ClassInfo.toStylizedClass(): StylizedClass {
            return stylizedClassBuilder(this, name, modifiers)
        }

        private fun stylizedClassBuilder(
            typeObj: Any,
            className: String,
            modifiers: Int
        ): StylizedClass {
            val typeName = when (typeObj) {
                is Class<*> -> {
                    when {
                        typeObj.isEnum -> "enum "
                        typeObj.isInterface -> "" // handled by Modifier.toString
                        typeObj.isAnnotation -> "annotation "
                        else -> "class "
                    }
                }
                is ClassInfo -> {
                    when {
                        typeObj.isEnum -> "enum "
                        typeObj.isInterface -> "" // handled by Modifier.toString
                        typeObj.isStandardClass -> "class "
                        typeObj.isAnnotation -> "annotation "
                        typeObj.isAnonymousInnerClass -> "<anonymous inner class> "
                        else -> "???"
                    }
                }
                else -> {
                    kotlin.error("Unknown object to extract type from: ${typeObj::class}")
                }
            }

            // Apply UNWANTED_MODIFIERS to never show the synchronized or static keyword on the modifier string
            // as it is not very useful information
            val cleanedModifiers = (modifiers and UNWANTED_MODIFIERS).let {
                // remove some redundant information to reduce noise of modifiers
                when {
                    isInterface(it) -> it and ABSTRACT.inv()
                    it and ENUM_BIT != 0 -> it and FINAL.inv()
                    else -> it
                }
            }
            val mods = toString(cleanedModifiers).let { it + if (it.isNotEmpty()) " " else "" }

            return StylizedClass("$mods$typeName$className", className, modifiers)
        }
    }
}
