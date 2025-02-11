package LEB_04

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val wordsFile: File = File("words.txt")
    val lines = wordsFile.readLines()
    val dictionary: MutableList<Word> = mutableListOf()
    for (line in lines) {
        val lineSplint = line.split("|")
        val word = Word(lineSplint[0], lineSplint[1], correctAnswersCount = null ?: 0)
        dictionary.add(word)
    }
    println(dictionary)
}