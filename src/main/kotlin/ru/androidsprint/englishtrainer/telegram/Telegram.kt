package ru.androidsprint.englishtrainer.telegram

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_word_clicked"
const val RESET_CLICKED = "reset_clicked"
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
    val text: String? = null,
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
    val text: String? = null,
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
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val updates = telegramBot.responseRezult(lastUpdateId)
        if (updates.isEmpty()) continue
        val sortedUpdates = updates.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, telegramBot) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>, telegramBot: TelegramBotService) {
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val message = update.message?.text
    val data = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }
    val callbackData = data?.substringAfter(
        CALLBACK_DATA_ANSWER_PREFIX
    )?.toIntOrNull()
    when {
        message?.lowercase() == START -> {
            telegramBot.sendMenu(chatId)
        }

        data?.lowercase() == STATISTICS_CLICKED -> {
            val statistics = trainer.getStatistics()
            val message =
                "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n"
            telegramBot.sendMessage(chatId, message)
        }

        data?.lowercase() == LEARN_WORDS_CLICKED -> {
            trainer.checkNextQuestionAndSend(
                trainer,
                telegramBot,
                chatId
            )
        }

        callbackData != null -> {
            if (trainer.checkAnswer(callbackData)) {
                val message =
                    "Правильно!\n"
                telegramBot.sendMessage(chatId, message)
                trainer.checkNextQuestionAndSend(
                    trainer,
                    telegramBot,
                    chatId
                )
            } else {
                val message = """
                     Неправильно! ${trainer.question?.correctAnswer?.original}- это ${trainer.question?.correctAnswer?.translate}
                """.trimIndent()
                telegramBot.sendMessage(chatId, message)
                trainer.checkNextQuestionAndSend(
                    trainer,
                    telegramBot,
                    chatId
                )
            }
        }

        data == RESET_CLICKED -> {
            trainer.resetProgress()
            telegramBot.sendMessage(chatId, "Прогресс сброшен")
        }
    }
}
