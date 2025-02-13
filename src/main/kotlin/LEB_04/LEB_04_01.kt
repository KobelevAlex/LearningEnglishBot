package LEB_04

import java.io.File

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
            "1" -> println("Учить слова")
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