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
            "1" -> {
                while (true) {
                    println("Учить слова")
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }
                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены.")
                        return
                    }
                    val questionWords = notLearnedList.shuffled().take(4)
                    val correctAnswer = questionWords[1]
                    val shuffledWord = questionWords.shuffled()
                    println(
                        """
                        ${correctAnswer.original}:
                         1 - ${shuffledWord[0].translate}
                         2 - ${shuffledWord[1].translate}
                         3 - ${shuffledWord[2].translate}
                         4 - ${shuffledWord[3].translate}
                    """.trimIndent()
                    )
                    val inputNumber = readln()
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
