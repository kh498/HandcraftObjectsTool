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

package no.uib.inf219.example.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import no.uib.inf219.example.storage.Identifiable
import no.uib.inf219.example.storage.RetrievableStorage
import no.uib.inf219.example.storage.StoreHandler

/**
 * TODO allow for multiple pages of text
 *
 * @author Elg
 */
class Conversation : Identifiable<String> {

    @JsonPropertyDescription("The text to player will read")
    @JsonProperty("text", required = true)
    var text: String = ""

    @JsonPropertyDescription("The name of this conversation for later referencing")
    @JsonProperty("name", defaultValue = "\"Conversation\"", required = false)
    var name: String = "Conversation #${++createId}"
        set(value) {
            field = value
            val store: RetrievableStorage<String, Conversation> =
                StoreHandler.getStore(Conversation::class.java)
            store.update(this)
        }

    @JsonPropertyDescription("The responses the player can have to the the text in this conversation")
    @JsonProperty("responses", defaultValue = "[]", required = false)
    var responses: MutableList<Response> = ArrayList()
        get() = if (field.isEmpty()) Response.exitResponse else field

    init {
        val store: RetrievableStorage<String, Conversation> =
            StoreHandler.getStore(Conversation::class.java)
        store.store(this)
    }

    /**
     * If this conversation have been held, setter always sets this to `true`
     */
    @JsonIgnore
    var hasBeenRead: Boolean = false
        set(_) {
            field = true
        }

    companion object {

        @JvmStatic
        var createId = 0
        val endConversation: Conversation = create("(Conversation ended)", "End of Conversation", Response.exitResponse)

        @JvmStatic
        fun create(text: String, name: String? = null, responses: MutableList<Response>? = null): Conversation {
            val conv = Conversation()
            conv.text = text

            if (name != null)
                conv.name = name
            if (responses != null)
                conv.responses = responses
            return conv
        }
    }

    override fun getId(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Conversation) return false

        if (text != other.text) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
