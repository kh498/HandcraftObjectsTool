package no.uib.inf219.example.data

import org.bukkit.configuration.serialization.ConfigurationSerializable

/**
 * TODO allow for multiple pages of text
 *
 * @author Elg
 */
class Conversation(
    val text: String,
    val name: String = "",
    val responses: List<Response> = listOf(Response.exitResponse)
) : ConfigurationSerializable {

    companion object {
        const val NAME_PATH = "name"
        const val TEXT_PATH = "text"
        const val RESPONSE_PATH = "responses"

        fun deserialize(map: Map<String, Any?>): Conversation {
            val text = map[TEXT_PATH] as String
            val name = map[NAME_PATH] as String
            val responses = map[RESPONSE_PATH] as List<Response>
            return Conversation(text, name, responses)
        }

        val endConversation = Conversation(
            "(Conversation ended)",
            "End of Conversation",
            listOf(Response("End conversation", end = true, conv = Conversation("")))
        )
    }

    override fun serialize(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map[NAME_PATH] = name
        map[TEXT_PATH] = text
        return map
    }

}
