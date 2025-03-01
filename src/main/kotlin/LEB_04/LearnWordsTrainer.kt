package LEB_04

import java.io.File
import kotlin.random.Random

const val MINIMUM_CORRECT_ANSWERS = 3
const val NUMBER_ANSWERS = 4

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

class LearnWordsTrainer {
    private var question: Question? = null
    private val vocabulary = loadDictionary()
    private fun loadDictionary(): MutableList<Word> {
        val vocabulary: MutableList<Word> = mutableListOf()
        val wordsFile: File = File("words.txt")
        if (wordsFile.exists()) {
            val lines = wordsFile.readLines()
            for (line in lines) {
                val lineSplit = line.split("|")
                val word = Word(lineSplit[0], lineSplit[1], lineSplit[2].toIntOrNull() ?: 0)
                vocabulary.add(word)
            }
        }
        return vocabulary
    }

    private fun saveDictionary(words: MutableList<Word>) {
        val wordsFile: File = File("words.txt")
        wordsFile.writeText("")
        for (word in words) {
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    fun getStatistics(): Statistics {
        val learnedWords = vocabulary.filter { it.correctAnswersCount >= MINIMUM_CORRECT_ANSWERS }
        val totalCount = vocabulary.count()
        val learnedCount = learnedWords.count()
        val percent = (learnedCount * 100) / totalCount
        return Statistics(learnedWords, totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = vocabulary.filter { it.correctAnswersCount < MINIMUM_CORRECT_ANSWERS }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size >= NUMBER_ANSWERS) {
            notLearnedList.shuffled().take(NUMBER_ANSWERS)
        } else {
            notLearnedList.shuffled()
        }
        val correctAnswerIndex = Random.nextInt(questionWords.size)
        val correctAnswer = questionWords[correctAnswerIndex]
        val shuffledWords = questionWords.shuffled()
        return Question(
            variants = shuffledWords,
            correctAnswer = correctAnswer,
        )
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
