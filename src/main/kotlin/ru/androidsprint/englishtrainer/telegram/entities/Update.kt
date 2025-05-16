package ru.androidsprint.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.androidsprint.englishtrainer.telegram.entities.CallbackQuery
import ru.androidsprint.englishtrainer.telegram.entities.Message

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)