package ru.androidsprint.englishtrainer.telegram

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.androidsprint.englishtrainer.telegram.entities.*
import ru.androidsprint.englishtrainer.treaner.LearnWordsTrainer
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

    internal val json = Json {
        ignoreUnknownKeys = true
    }

    fun responseRezult(lastUpdateId: Long): List<Update> {
        val responseString: String = getUpdates(lastUpdateId)
        val response: Response = json.decodeFromString(responseString)
        return response.rezult
    }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .build()

    internal fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "${URL_API}bot$botToken/getUpdates?offset=$updateId"
        val client = httpClient
        val requestGetUpdates = HttpRequest.newBuilder()
            .uri(URI.create(urlGetUpdates))
            .build()
        val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
        return responseGetUpdates.body()
    }

    fun sendMessage(chatId: Long, message: String) {
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

    fun sendMenu(chatId: Long) {
        val url = "${URL_API}bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
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

    fun sendQuestion(chatId: Long, question: Question) {
        val url = "${URL_API}bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(
                            text = word.translate,
                            callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                        )
                    )
                } + listOf(
                    listOf(
                        InlineKeyboard(
                            text = "В меню",
                            callbackData = BACK_MENU
                        )
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
}