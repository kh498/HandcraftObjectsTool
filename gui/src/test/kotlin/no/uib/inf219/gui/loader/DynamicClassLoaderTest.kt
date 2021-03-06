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

package no.uib.inf219.gui.loader

import no.uib.inf219.extra.type
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension

/**
 * @author Elg
 */
@ExtendWith(ApplicationExtension::class)
internal class DynamicClassLoaderTest {

    @Test
    internal fun `getType() returns primitive int class for "int"`() {
        assertSame(Int::class.type(), DynamicClassLoader.parseClassName("int"))
    }

    @Test
    internal fun `getType() returns primitive long class for "long"`() {
        assertSame(Long::class.type(), DynamicClassLoader.parseClassName("long"))
    }

    @Test
    internal fun `getType() returns primitive boolean class for "boolean"`() {
        assertSame(Boolean::class.type(), DynamicClassLoader.parseClassName("boolean"))
    }

    @Test
    internal fun `getType() returns primitive short class for "short"`() {
        assertSame(Short::class.type(), DynamicClassLoader.parseClassName("short"))
    }

    @Test
    internal fun `getType() returns primitive char class for "char"`() {
        assertSame(Char::class.type(), DynamicClassLoader.parseClassName("char"))
    }

    @Test
    internal fun `getType() returns primitive float class for "float"`() {
        assertSame(Float::class.type(), DynamicClassLoader.parseClassName("float"))
    }

    @Test
    internal fun `getType() returns primitive double class for "double"`() {
        assertSame(Double::class.type(), DynamicClassLoader.parseClassName("double"))
    }

    @Test
    internal fun `getType() returns primitive byte class for "byte"`() {
        assertSame(Byte::class.type(), DynamicClassLoader.parseClassName("byte"))
    }

    @Test
    internal fun `getType() returns primitive int array for "int-then-square-parentheses"`() {
        assertEquals(emptyArray<Int>().javaClass.type(), DynamicClassLoader.parseClassName("int[]"))
        assertEquals(IntArray::class.type(), DynamicClassLoader.parseClassName("int[]"))
    }

    @Test
    internal fun `getType() supports multi-dimensional primitive array`() {
        assertEquals(emptyArray<Array<IntArray>>()::class.type(), DynamicClassLoader.parseClassName("int[][][]"))
    }

    @Test
    internal fun `getType() supports multi-dimensional object array`() {
        assertEquals(
            emptyArray<Array<String>>()::class.type(),
            DynamicClassLoader.parseClassName("java.lang.String[][]")
        )
    }

    @Test
    internal fun `getType() supports multi-dimensional integer object array`() {
        assertEquals(
            emptyArray<Array<Int>>()::class.type(),
            DynamicClassLoader.parseClassName("java.lang.Integer[][]")
        )
    }

    @Test
    internal fun `getType() throws when missing square parentheses`() {
        assertThrows<IllegalArgumentException> { DynamicClassLoader.parseClassName("java.lang.Integer[][") }
    }

    @Test
    internal fun `getType() throws when wrong square parentheses order`() {
        assertThrows<IllegalArgumentException> { DynamicClassLoader.parseClassName("java.lang.Integer[[]]") }
    }

    @Test
    internal fun `getType() throws when given binary int array name`() {
        assertThrows<IllegalArgumentException> { DynamicClassLoader.parseClassName("[I") }
    }

    @Test
    internal fun `getType() throws when given binary int name`() {
        assertThrows<ClassNotFoundException> { DynamicClassLoader.parseClassName("I") }
    }

    @Test
    internal fun `getType() throws when given binary object name`() {
        assertThrows<ClassNotFoundException> { DynamicClassLoader.parseClassName("Ljava.lang.String;") }
    }

    @Test
    internal fun `getType() throws when given binary object array name`() {
        assertThrows<IllegalArgumentException> { DynamicClassLoader.parseClassName("[Ljava.lang.String;") }
    }
}
