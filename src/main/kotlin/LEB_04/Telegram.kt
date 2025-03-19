package LEB_04

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult = messageTextRegex.find(updates)
        if (matchResult != null) {
            val text = matchResult.groups[1]?.value
            println("Текст сообщения: $text")
            val newUpdateId = updates.substringAfter("\"update_id\":").substringBefore(",").toInt()
            updateId = newUpdateId + 1
        }
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client = HttpClient.newBuilder().build()
    val requestGetUpdates = HttpRequest.newBuilder()
        .uri(URI.create(urlGetUpdates))
        .build()
    val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
    return responseGetUpdates.body()
}