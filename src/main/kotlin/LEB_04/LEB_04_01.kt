package LEB_04

fun Question.questionToString(): String {
    return this.variants.mapIndexed { index, word ->
        " ${index + 1} - ${word.translate}"
    }.joinToString(
        separator = "\n",
        prefix = "${correctAnswer.original}:\n",
        postfix = "\n ----------\n 0 - Меню\n"
    )
}

fun main() {
    val trainer = LearnWordsTrainer()
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
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Все слова в словаре выучены.")
                        break
                    } else {
                        val optionString = question.questionToString()
                        println("индекс - ${question.variants.indexOf(question.correctAnswer)}\n")
                        println(optionString)
                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) {
                            break
                        } else if (userAnswerInput in 1..4) {
                            if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                                println("Правильно!\n")
                            } else {
                                println("Неправильно! Правильный ответ: ${question.correctAnswer.translate}\n " +
                                        "индекс - ${question.variants.indexOf(question.correctAnswer)}")
                                println("Введите число от 1 до 4 или '0' для выхода.")
                            }
                        }
                    }
                }
            }

            "2" -> {
                println("Статистика")
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n")
            }

            "0" -> break
            else -> println("Введите число 1,2 или 0")
        }
    }
}
