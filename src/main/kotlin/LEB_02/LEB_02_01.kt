package LEB_02

import java.io.File

fun main() {
    val wordsFile: File = File("words.txt")
    val listWordsFile = wordsFile.readLines()
    for (word in listWordsFile) {
        println(word)
    }
}