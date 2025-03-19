package LEB_04

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"
    val urlGeUpdates = "https://api.telegram.org/bot$botToken/getUpdates"
    val client = HttpClient.newBuilder().build()
    val requestGetMe = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()
    val responseGetMe = client.send(requestGetMe, HttpResponse.BodyHandlers.ofString())
    println(responseGetMe.body())
    val requestGetMeGeUpdates = HttpRequest.newBuilder().uri(URI.create(urlGeUpdates)).build()
    val responseGetMeGeUpdates = client.send(requestGetMeGeUpdates, HttpResponse.BodyHandlers.ofString())
    println(responseGetMeGeUpdates.body())
}