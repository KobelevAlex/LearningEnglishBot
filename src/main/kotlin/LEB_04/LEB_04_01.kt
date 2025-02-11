package LEB_04

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val wordsFile: File = File("words.txt")
    val vocabulary: MutableList<Word> = mutableListOf()
    fun loadDictionary(): MutableList<Word> {
        val lines = wordsFile.readLines()
        for (line in lines) {
            val lineSplint = line.split("|")
            val word = Word(lineSplint[0], lineSplint[1], correctAnswersCount = null ?: 0)
            vocabulary.add(word)
        }
        return vocabulary
    }

    val dictionary: MutableList<Word> = loadDictionary()
    println(loadDictionary())
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
            "2" -> println("Статистика")
            "0" -> break
            else -> println("Введите число 1,2 или 0")
        }
    }
}