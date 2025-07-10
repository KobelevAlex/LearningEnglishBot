package ru.androidsprint.englishtrainer.telegram
import ru.androidsprint.englishtrainer.treaner.Word
import java.io.File
import java.sql.*

const val DEFAULT_FILE_NAME ="words.txt"

interface IUserDictionary {
    fun getNumOfLearnedWords(): Int
    fun getSize(): Int
    fun getLearnedWords(): List<Word>
    fun getUnlearnedWords(): List<Word>
    fun setCorrectAnswersCount(word: String, correctAnswersCount: Int)
    fun resetUserProgress()
}

fun main() {
    val wordsFile = File("words.txt")
    updateDictionary(wordsFile)
}

fun updateDictionary(wordsFile: File) {
    try {
        // Устанавливаем соединение с базой данных SQLite
        DriverManager.getConnection("jdbc:sqlite:data3.db").use { connection ->
            connection.autoCommit = false // Для транзакционной вставки
            // Создаем таблицу, если она еще не существует
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS words (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        text VARCHAR,
                        translate VARCHAR
                    );
                    """.trimIndent()
                )
            }

            if (wordsFile.exists()) {
                // Используем подготовленный запрос для вставки
                val insertStmt = connection.prepareStatement("INSERT INTO words (text, translate) VALUES (?, ?)")
                val lines = wordsFile.readLines()
                for (line in lines) {
                    val lineSplit = line.split("|")
                    if (lineSplit.size >= 2) {
                        val word = lineSplit[0].trim()
                        val translation = lineSplit[1].trim()
                        insertStmt.setString(1, word)
                        insertStmt.setString(2, translation)
                        insertStmt.addBatch()
                    }
                }
                insertStmt.executeBatch()
                connection.commit()
            }
        }
    } catch (e: SQLException) {
        println("Ошибка базы данных: ${e.message}")
    } catch (e: Exception) {
        println("Произошла ошибка: ${e.message}")
    }
}

class FileUserDictionary(
    private val fileName: String = DEFAULT_FILE_NAME,
    private val learningThreshold: Int = DEFAULT_LEARNING_THRESHOLD,
) : IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (e: Exception) {
        throw IllegalArgumentException('Некорректный файл')
    }

    // ...

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        dictionary.find { it.original == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun resetUserProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun loadDictionary(): List<Word> {
        val wordsFile = File(fileName)
        // ...
        return dictionary
    }

    private fun saveDictionary() {
        val file = File(fileName)
        val newFileContent = dictionary.map { '${it.original}|${it.translate}|${it.correctAnswersCount}' }
        file.writeText(newFileContent.joinToString(separator = '\n'))
    }
}


/*
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class VocabularyBot : TelegramLongPollingBot() {

    private val dbUrl = "jdbc:sqlite:vocabulary.db"
    private val connection: Connection = DriverManager.getConnection(dbUrl)

    init {
        // Инициализация базы данных
        connection.createStatement().executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS vocabulary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                word TEXT UNIQUE,
                translation TEXT,
                learned INTEGER DEFAULT 0
            );
            """.trimIndent()
        )
    }

    override fun getBotUsername() = "YourBotUsername"
    override fun getBotToken() = "YOUR_BOT_TOKEN"

    override fun onUpdateReceived(update: Update?) {
        if (update?.hasMessage() == true && update.message.hasText()) {
            val chatId = update.message.chatId.toString()
            val text = update.message.text.trim()
            val parts = text.split(" ")

            when {
                text.startsWith("/start") -> {
                    sendMessage(chatId, "Привет! Используйте /add слово перевод, чтобы добавить слово.")
                }
                text.startsWith("/add") -> {
                    if (parts.size >= 3) {
                        val word = parts[1]
                        val translation = parts[2]
                        addWord(word, translation, chatId)
                    } else {
                        sendMessage(chatId, "Использование: /add слово перевод")
                    }
                }
                text.startsWith("/quiz") -> {
                    startQuiz(chatId)
                }
                text.startsWith("/list") -> {
                    listWords(chatId)
                }
                else -> {
                    handleAnswer(chatId, text)
                }
            }
        }
    }

    private fun addWord(word: String, translation: String, chatId: String) {
        val stmt = connection.prepareStatement("INSERT OR IGNORE INTO vocabulary (word, translation) VALUES (?, ?)")
        stmt.setString(1, word)
        stmt.setString(2, translation)
        val rows = stmt.executeUpdate()
        if (rows > 0) {
            sendMessage(chatId, "Слово добавлено: $word - $translation")
        } else {
            sendMessage(chatId, "Это слово уже есть в словаре.")
        }
    }

    private fun listWords(chatId: String) {
        val rs = connection.createStatement().executeQuery("SELECT word, translation, learned FROM vocabulary")
        val sb = StringBuilder()
        while (rs.next()) {
            sb.append("${rs.getString("word")} - ${rs.getString("translation")} (учено ${rs.getInt("learned")} раз)\n")
        }
        if (sb.isEmpty()) {
            sendMessage(chatId, "Словарь пуст.")
        } else {
            sendMessage(chatId, sb.toString())
        }
    }

    private fun startQuiz(chatId: String) {
        val rs = connection.createStatement().executeQuery("SELECT word, translation FROM vocabulary ORDER BY RANDOM() LIMIT 1")
        if (rs.next()) {
            val word = rs.getString("word")
            val translation = rs.getString("translation")
            // Сохраняем текущий вопрос в памяти пользователя
            userQuestions[chatId] = Pair(word, translation)
            sendMessage(chatId, "Что означает слово: $word?")
        } else {
            sendMessage(chatId, "В словаре нет слов. Добавьте слова командой /add.")
        }
    }

    private val userQuestions = mutableMapOf<String, Pair<String, String>>() // чатID -> (слово, перевод)

    private fun handleAnswer(chatId: String, answer: String) {
        val question = userQuestions[chatId]
        if (question == null) {
            sendMessage(chatId, "Начните викторину командой /quiz.")
            return
        }

        val (word, translation) = question
        if (answer.equals(translation, ignoreCase = true)) {
            // Обновляем количество изучений
            val stmt = connection.prepareStatement("UPDATE vocabulary SET learned = learned + 1 WHERE word = ?")
            stmt.setString(1, word)
            stmt.executeUpdate()
            sendMessage(chatId, "Правильно! Молодец!")
        } else {
            sendMessage(chatId, "Неверно. Правильный ответ: $translation")
        }
        userQuestions.remove(chatId)
    }

    private fun sendMessage(chatId: String, text: String) {
        val message = SendMessage()
        message.chatId = chatId
        message.text = text
        executeAsync(message)
    }
}

fun main() {
    val bot = VocabularyBot()
    // Обязательно запусти в приложении, которое инициализирует TelegramApi
    // Например, используем TelegramBotsТестовая среда или интеграцию
}
 */
