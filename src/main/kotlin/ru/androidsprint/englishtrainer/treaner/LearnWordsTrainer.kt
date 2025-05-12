package ru.androidsprint.englishtrainer.treaner

import kotlinx.serialization.json.Json
import ru.androidsprint.englishtrainer.telegram.TelegramBotService
import java.io.File
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException


data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val listDictionary: List<Word>,
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {
    var question: Question? = null
    val vocabulary = loadDictionary()
    private fun loadDictionary(): MutableList<Word> {
        try {
            val vocabulary: MutableList<Word> = mutableListOf()
            val wordsFile: File = File("words.txt")
            wordsFile.createNewFile()
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

    private fun saveDictionary(words: MutableList<Word>) {
        val wordsFile: File = File("words.txt")
        wordsFile.writeText("")
        for (word in words) {
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
                saveDictionary(vocabulary)
                true
            } else {
                false
            }
        } ?: false
    }
}