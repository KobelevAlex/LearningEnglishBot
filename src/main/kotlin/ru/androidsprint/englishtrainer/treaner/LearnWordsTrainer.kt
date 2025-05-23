package ru.androidsprint.englishtrainer.treaner

import ru.androidsprint.englishtrainer.telegram.TelegramBotService
import java.io.File


class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {
    var question: Question? = null
    private val vocabulary = loadDictionary()
    private fun loadDictionary(): MutableList<Word> {
        try {
            val wordsFile: File = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val vocabulary: MutableList<Word> = mutableListOf()
            if (wordsFile.exists()) {
                val lines = wordsFile.readLines()
                for (line in lines) {
                    val lineSplit = line.split("|")
                    val word = Word(lineSplit[0], lineSplit[1], lineSplit[2].toIntOrNull() ?: 0)
                    vocabulary.add(word)
                }
            }
            return vocabulary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Не корректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile: File = File("words.txt")
        wordsFile.writeText("")
        for (word in vocabulary) {
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    fun getStatistics(): Statistics {
        val learnedWords = vocabulary.filter { it.correctAnswersCount >= learnedAnswerCount }
        val totalCount = vocabulary.count()
        val learnedCount = learnedWords.count()
        val percent = if (learnedCount > 0) {
            (learnedCount * 100) / totalCount
        } else 0
        return Statistics(learnedWords, totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = vocabulary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = vocabulary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled()
                .take(countOfQuestionWords) + learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()
        val correctAnswer = questionWords.random()
        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        telegramBot: TelegramBotService,
        chatId: Long
    ) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            val message =
                "Все слова в словаре выучены\n"
            telegramBot.sendMessage(chatId, message)
        } else {
            telegramBot.sendQuestion(chatId, question)
        }
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun resetProgress() {
        vocabulary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }
}