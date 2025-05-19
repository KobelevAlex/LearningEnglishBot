package ru.androidsprint.englishtrainer.treaner

data class Statistics(
    val listDictionary: List<Word>,
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)