package ru.androidsprint.englishtrainer.treaner

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)