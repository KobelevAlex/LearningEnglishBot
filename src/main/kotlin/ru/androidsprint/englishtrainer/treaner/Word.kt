package ru.androidsprint.englishtrainer.treaner

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)