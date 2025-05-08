package ru.androidsprint.englishtrainer.telegram

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_word_clicked"
const val START = "/start"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Response(
    @SerialName("result")
    val rezult: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)
@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBot = TelegramBotService(botToken)
    var lastUpdateId = 0L
    val json = Json {
        ignoreUnknownKeys = true
    }
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBot.getUpdates(lastUpdateId)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.rezult
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1


        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val message = firstUpdate.message?.text
        val data = firstUpdate.callbackQuery?.data
        val callbackData = data?.substringAfter(
            CALLBACK_DATA_ANSWER_PREFIX
        )?.toIntOrNull()
        when {
            message?.lowercase() == START -> {
                telegramBot.sendMenu(json, chatId)
            }

            data?.lowercase() == STATISTICS_CLICKED -> {
                val statistics = trainer.getStatistics()
                val message =
                    "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n"
                telegramBot.sendMessage(json, chatId, message)
            }

            data?.lowercase() == LEARN_WORDS_CLICKED -> {
                trainer.checkNextQuestionAndSend(
                    json,
                    trainer,
                    telegramBot,
                    chatId
                )
            }

            callbackData != null ->
                if (trainer.checkAnswer(callbackData)) {
                    val message =
                        "Правильно!\n"
                    telegramBot.sendMessage(json, chatId, message)
                    trainer.checkNextQuestionAndSend(
                        json,
                        trainer,
                        telegramBot,
                        chatId
                    )
                } else {
                    val message = """
                     Неправильно! ${trainer.question?.correctAnswer?.original}- это ${trainer.question?.correctAnswer?.translate}
                """.trimIndent()
                    telegramBot.sendMessage(json, chatId, message)
                    trainer.checkNextQuestionAndSend(
                        json,
                        trainer,
                        telegramBot,
                        chatId
                    )
                }
        }
    }
}