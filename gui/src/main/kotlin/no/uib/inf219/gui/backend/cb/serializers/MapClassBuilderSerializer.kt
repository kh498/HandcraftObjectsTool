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

package no.uib.inf219.gui.backend.cb.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import no.uib.inf219.gui.backend.cb.api.ClassBuilder
import no.uib.inf219.gui.backend.cb.parents.MapClassBuilder
import no.uib.inf219.gui.backend.cb.parents.MapClassBuilder.Companion.ENTRY_KEY
import no.uib.inf219.gui.backend.cb.parents.MapClassBuilder.Companion.ENTRY_VALUE
import no.uib.inf219.gui.backend.cb.toObject

/**
 * Serialize the [MapClassBuilder]. The internal structure of it (using a set of entries) does not work with the
 * standard jackson [com.fasterxml.jackson.databind.ser.std.MapSerializer]. Especially when it comes to references,
 * only values are allowed to be referenced in HOT.
 *
 * @author Elg
 */
object MapClassBuilderSerializer : AbstractClassBuilderSerializer<MapClassBuilder>(
    MapClassBuilder::class
) {

    override fun serializeMaybeWithType(
        cb: MapClassBuilder,
        gen: JsonGenerator,
        provider: SerializerProvider,
        typeSer: TypeSerializer?
    ) {
        val map = HashMap<Any?, ClassBuilder?>()

        for (entry in cb.serObject) {
            // explicitly convert the key object to it's type. This means that key elements cannot be referenced
            // or be a reference. But having a reference to a key in a map I am considering to be an edge case.
            //
            // This is because jackson is using a different type of serializers with keys as not everything is allowed
            // to be a key in json. So to make this happen we need to make key serializers for all existing cb
            // serializers and use in those to allow for referencing.
            val key = entry.serObject[ENTRY_KEY]?.toObject()

            // value is allowed to be referenced and be a reference!
            val value = entry.serObject[ENTRY_VALUE]
            map[key] = value
        }

        // Do not use the [cb.type] as it might contain the wrong instance of map.
        // as is usually the case when the map only contains one element
        val ser = provider.findValueSerializer(Map::class.java, null)
            ?: error("Failed to find map serializer")

        if (typeSer != null)
            ser.serializeWithType(map, gen, provider, typeSer)
        else
            ser.serialize(map, gen, provider)
    }
}
