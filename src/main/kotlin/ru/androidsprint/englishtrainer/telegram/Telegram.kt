package ru.androidsprint.englishtrainer.telegram

import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_word_clicked"

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
        val matchResultChatId = chatIdRegex.find(updates)
        val matchResultUpdateId = updateIdRegex.find(updates)
        val matchDataRegex = dataRegex.find(updates)
        if (matchResultText != null && matchResultChatId != null && matchResultUpdateId != null) {
            val text = matchResultText.groups[1]?.value
            val chatId = matchResultChatId.groups[1]?.value
            val data = matchDataRegex?.groups?.get(1)?.value
            if (text != null && chatId != null) {
                telegramBot.sendMessage(chatId, text)
                val newUpdateId = matchResultUpdateId.groups[1]
                    ?.value
                    ?.toInt() ?: 0
                updateId = newUpdateId + 1
            }
            if (text?.lowercase() == "/start" && chatId != null) {
                telegramBot.sendMenu(chatId)
            }
            if (data?.lowercase() == STATISTICS_CLICKED && chatId != null) {
                val statistics = trainer.getStatistics()
                val message =
                    "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n"
                telegramBot.sendMessage(chatId, message)
            }
        }
    }
}