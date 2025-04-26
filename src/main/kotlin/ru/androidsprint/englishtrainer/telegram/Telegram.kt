package ru.androidsprint.englishtrainer.telegram

import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_word_clicked"
const val START = "/start"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer"

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBot = TelegramBotService(botToken)
    var updateId = 0
    val messageTextRegex: Regex = """"text":"(.*?)"""".toRegex()
    val chatIdRegex: Regex = """"chat":\{"id":(\d+)""".toRegex()
    val updateIdRegex: Regex = """"update_id":(\d+)""".toRegex()
    val dataRegex: Regex = """"data":"(.+?)"""".toRegex()
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(updateId)
        val newUpdateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        updateId = newUpdateId + 1
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value
        when {
            text?.lowercase() == START -> {
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
        }
    }
}