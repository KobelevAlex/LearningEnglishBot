package ru.androidsprint.englishtrainer.telegram

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.androidsprint.englishtrainer.treaner.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_TEXT_MESSAGE_LIMIT = 4096

class TelegramBotService(private val botToken: String) {
    companion object {
        const val URL_API = "https://api.telegram.org/"
    }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()

    fun Question.inlineKeyboard(): String {
        return this.variants.mapIndexed { index, variant ->
            """
        {
            "text": "${variant.translate}",
            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX}${index}"
        }
        """.trimIndent()
        }.joinToString(",")
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "${URL_API}bot$botToken/getUpdates?offset=$updateId"
        val client = httpClient
        val requestGetUpdates = HttpRequest.newBuilder()
            .uri(URI.create(urlGetUpdates))
            .build()
        val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
        return responseGetUpdates.body()
    }

    fun sendMessage(json: Json, chatId: Long?, message: String) {
        val url = "${URL_API}bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = httpClient
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Response: ${response.body()}")
    }

    fun sendMenu(json: Json, chatId: Long?) {
        val url = "${URL_API}bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = httpClient
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Response: ${response.body()}")
    }

    fun sendQuestion(json: Json, chatId: Long?, question: Question) {
        val url = "${URL_API}bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                })
            ),
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = httpClient
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Response: ${response.body()}")
    }
}