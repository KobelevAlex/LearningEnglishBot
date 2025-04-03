package LEB_04

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val URL_API = "https://api.telegram.org/"

class TelegramBotService(private val botToken: String) {
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
        require(text.isNotEmpty() && text.length <= 4096) { "Сообщение должно содержать от 1 до 4096 символов." }
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

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBot = TelegramBotService(botToken)
    var updateId = 0
    val messageTextRegex: Regex = """"text":"(.*?)"""".toRegex()
    val chatIdRegex: Regex = """"chat":\{"id":(\d+)""".toRegex()
    val updateIdRegex: Regex = """"update_id":(\d+)""".toRegex()
    val dataRegex: Regex =""""data":"(.+?)"""".toRegex()
    val trainer = LearnWordsTrainer()
    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(updateId)
        val matchResultText = messageTextRegex.find(updates)
        val matchResultChatId = chatIdRegex.find(updates)
        val matchResultUpdateId = updateIdRegex.find(updates)
        val matchDataRegex = dataRegex.find(updates)
        if (matchResultText != null && matchResultChatId != null && matchResultUpdateId != null) {
            val text = matchResultText.groups[1]?.value
            val chatId = matchResultChatId.groups[1]?.value
            val data= matchDataRegex?.groups?.get(1)?.value
            if (text != null && chatId != null) {
                telegramBot.sendMessage(chatId, text)
                val newUpdateId = matchResultUpdateId.groups[1]
                    ?.value
                    ?.toInt() ?: 0
                updateId = newUpdateId + 1
            }
            if (text?.lowercase() == "/start" && chatId != null) {
                telegramBot.sendMenu(chatId)
            }
            if (data?.lowercase() == "statistics_clicked" && chatId != null) {
                telegramBot.sendMessage(chatId, "выучено 10 из 10")
            }
        }
    }
}