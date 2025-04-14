package ru.androidsprint.englishtrainer.telegram

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

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "${URL_API}bot$botToken/getUpdates?offset=$updateId"
        val client = httpClient
        val requestGetUpdates = HttpRequest.newBuilder()
            .uri(URI.create(urlGetUpdates))
            .build()
        val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
        return responseGetUpdates.body()
    }

    fun sendMessage(chatId: String, text: String) {
        require(text.isNotEmpty() && text.length <= TELEGRAM_TEXT_MESSAGE_LIMIT) { "Сообщение должно содержать от 1 до 4096 символов." }
        val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
        val url = "${URL_API}bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val client = httpClient
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Response: ${response.body()}")
    }

    fun sendMenu(chatId: String) {
        val url = "${URL_API}bot$botToken/sendMessage"
        val sendMenuBody = """
            {
              "chat_id": $chatId,
              "text": "Основное меню",
              "reply_markup": 
              {
                 "inline_keyboard": 
                [
                  [
                    { "text": "Изучить слова",
                      "callback_data": "learn_word_clicked"
                    },
                    { "text": "Статистика",
                      "callback_data": "statistics_clicked"
                    }
                  ]
                ]
              }
            }
        """.trimIndent()
        val client = httpClient
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Response: ${response.body()}")
    }
}