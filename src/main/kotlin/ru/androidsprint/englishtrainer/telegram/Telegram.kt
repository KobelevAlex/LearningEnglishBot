package ru.androidsprint.englishtrainer.telegram

import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_word_clicked"
const val START = "/start"

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
        val matchResultText = messageTextRegex.find(updates)
        val matchDataRegex = dataRegex.find(updates)
        if (matchResultText != null) {
            val text = matchResultText.groups[1]?.value
            val newUpdateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
            updateId = newUpdateId + 1
            val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
            val data = matchDataRegex?.groups?.get(1)?.value
            if (text != null) {
                telegramBot.sendMessage(chatId, text)
            }
            if (text?.lowercase() == START) {
                telegramBot.sendMenu(chatId)
            }
            if (data?.lowercase() == STATISTICS_CLICKED) {
                val statistics = trainer.getStatistics()
                val message =
                    "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n"
                telegramBot.sendMessage(chatId, message)
            }
        }
    }
}