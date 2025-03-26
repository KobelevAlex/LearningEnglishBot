package LEB_04

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService {
    val messageTextRegex: Regex = """"text":"(.*?)"""".toRegex()
    val chatIdRegex: Regex = """"chat":\{"id":(\d+)""".toRegex()
    val updateIdRegex: Regex = """"update_id":(\d+)""".toRegex()

    fun getUpdates(botToken: String, updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val requestGetUpdates = HttpRequest.newBuilder()
            .uri(URI.create(urlGetUpdates))
            .build()
        val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
        return responseGetUpdates.body()
    }

    fun sendMessage(botToken: String, chatId: String, text: String) {
        require(text.isNotEmpty() && text.length <= 4096) { "Сообщение должно содержать от 1 до 4096 символов." }
        val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Response: ${response.body()}")
    }
}

fun main(args: Array<String>) {
    val telegramBot = TelegramBotService()
    val botToken = args[0]
    var updateId = 0
    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(botToken, updateId)
        val matchResultText = telegramBot.messageTextRegex.find(updates)
        val matchResultChatId = telegramBot.chatIdRegex.find(updates)
        val matchResultUpdateId = telegramBot.updateIdRegex.find(updates)
        if (matchResultText != null && matchResultChatId != null && matchResultUpdateId != null) {
            val text = matchResultText.groups[1]?.value
            val chatId = matchResultChatId.groups[1]?.value
            if (text != null && chatId != null) {
                println("Текст сообщения: $text")
                telegramBot.sendMessage(botToken, chatId, text)
                val newUpdateId = matchResultUpdateId.groups
                    .get(1)
                    ?.value
                    ?.toInt() ?: 0
                updateId = newUpdateId + 1
            }
        }
    }
}
