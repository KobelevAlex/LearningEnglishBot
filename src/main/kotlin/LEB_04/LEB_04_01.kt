package LEB_04

import java.io.File
import kotlin.random.Random

const val NUMBER_ANSWERS = 4
const val MINIMUM_CORRECT_ANSWERS = 3
const val STEP_INCREASE = 1

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

val wordsFile: File = File("words.txt")
val vocabulary: MutableList<Word> = mutableListOf()
fun loadDictionary(): MutableList<Word> {
    val lines = wordsFile.readLines()
    for (line in lines) {
        val lineSplit = line.split("|")
        val word = Word(lineSplit[0], lineSplit[1], lineSplit[2].toIntOrNull() ?: 0)
        vocabulary.add(word)
    }
    return vocabulary
}

fun saveDictionary() {
    wordsFile.printWriter().use { out ->
        vocabulary.forEach { word ->
            out.println("${word.original}|${word.translate}|${word.correctAnswersCount}")
        }
    }
}

fun main() {
    val dictionary: MutableList<Word> = loadDictionary()
    while (true) {
        println(
            """
            1 - Учить слова
            2 - Статистика
            0 - Выход
        """.trimIndent()
        )
        val inputNumber = readln()
        when (inputNumber) {
            "1" -> {
                while (true) {
                    println("Учить слова")
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < MINIMUM_CORRECT_ANSWERS }
                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены.")
                        return
                    }
                    val questionWords = if (notLearnedList.size >= NUMBER_ANSWERS) {
                        notLearnedList.shuffled().take(NUMBER_ANSWERS)
                    } else {
                        notLearnedList.shuffled() // Берем все оставшиеся слова
                    }
                    val correctAnswerIndex = Random.nextInt(questionWords.size)
                    val correctAnswer = questionWords[correctAnswerIndex]
                    val shuffledWords = questionWords.toMutableList()
                    shuffledWords.removeAt(correctAnswerIndex)
                    shuffledWords.add(Random.nextInt(questionWords.size), correctAnswer)
                    val optionsString = shuffledWords.mapIndexed { index, word ->
                        " ${index + 1} - ${word.translate}"
                    }.joinToString(
                        separator = "\n",
                        prefix = "${correctAnswer.original}:\n",
                        postfix = "\n ----------\n 0 - Меню\n"
                    )
                    println(optionsString)
                    val userAnswer = readln()
                    if (userAnswer == "0") break
                    val answerIndex = userAnswer.toIntOrNull()
                    if (answerIndex in 1..NUMBER_ANSWERS) {
                        if (shuffledWords[(answerIndex ?: 1) - 1].original == correctAnswer.original) {
                            println("Правильно!\n")
                            correctAnswer.correctAnswersCount++
                            saveDictionary()
                        } else {
                            println("Неправильно! Правильный ответ: ${correctAnswer.translate}\n")
                        }
                    } else {
                        println("Введите число от 1 до 4 или '0' для выхода.")
                    }
                }
            }

            "2" -> {
                println("Статистика")
                val listDictionary = dictionary.filter { it.correctAnswersCount >= 3 }
                val totalCount = vocabulary.count()
                val learnedCount = listDictionary.count()
                val percent = (learnedCount * 100) / totalCount
                println("Выучено $learnedCount из $totalCount слов | $percent%\n")
            }

            "0" -> break
            else -> println("Введите число 1,2 или 0")
        }
    }
}
