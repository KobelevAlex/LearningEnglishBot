package ru.androidsprint.englishtrainer.telegram

import ru.androidsprint.englishtrainer.telegram.entities.Update
import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_word_clicked"
const val RESET_CLICKED = "reset_clicked"
const val START = "/start"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer"
const val BACK_MENU = "back_menu"


fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBot = TelegramBotService(botToken)
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()
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

            data?.lowercase() == BACK_MENU -> {
                telegramBot.sendMenu(chatId)
            }

            data?.lowercase() == STATISTICS_CLICKED -> {
                val statistics = trainer.getStatistics()
                val answer =
                    "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n"
                telegramBot.sendMessage(chatId, answer)
            }

            data?.lowercase() == LEARN_WORDS_CLICKED -> {
                trainer.checkNextQuestionAndSend(
                    trainer,
                    telegramBot,
                    chatId
                )
            }

            callbackData != null -> {
                val isCorrect = trainer.checkAnswer(callbackData)
                val correctAnswer = trainer.question?.correctAnswer
                val answer = if (isCorrect) {
                    "Правильно!\n"
                } else {
                    "Неправильно! ${correctAnswer?.original} - это ${correctAnswer?.translate}"
                }
                telegramBot.sendMessage(chatId, answer)
                trainer.checkNextQuestionAndSend(trainer, telegramBot, chatId)
            }

            data == RESET_CLICKED -> {
                trainer.resetProgress()
                telegramBot.sendMessage(chatId, "Прогресс сброшен")
            }
        }
    }

    while (true) {
        Thread.sleep(2000)
        val updates = telegramBot.responseRezult(lastUpdateId)
        if (updates.isEmpty()) continue
        val sortedUpdates = updates.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, telegramBot) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}