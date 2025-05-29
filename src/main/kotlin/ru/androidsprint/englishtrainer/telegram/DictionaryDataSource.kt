package ru.androidsprint.englishtrainer.telegram

import java.sql.*

//fun main() {
//    var connection: Connection? = null
//    try {
//        connection = DriverManager.getConnection("jdbc:sqlite:sample.db")
//        val statement: Statement = connection.createStatement()
//        statement.queryTimeout = 30 // set timeout to 30 sec.
//        statement.executeUpdate("drop table if exists person")
//        statement.executeUpdate("create table person (id integer, name string)")
//        statement.executeUpdate("insert into person values(1, " leo ")")
//        statement.executeUpdate("insert into person values(2, " yui ")")
//        val rs: ResultSet = statement.executeQuery("select * from person")
//        while (rs.next()) {
//            // read the result set
//            println("name = " + rs.getString("name"))
//            println("id = " + rs.getInt("id"))
//        }
//    } catch (e: SQLException) {
//        // if the error message is 'out of memory',
//        // it probably means no database file is found
//        System.err.println(e.message)
//    } finally {
//        try {
//            connection?.close()
//        } catch (e: SQLException) {
//            // connection close failed.
//            System.err.println(e.message)
//        }
//    }
//}

fun main() {
    try {
        // Устанавливаем соединение с базой данных SQLite
        DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
            // Создаем объект для выполнения SQL-запросов
            val statement = connection.createStatement()

            // Создаем таблицу, если она еще не существует
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    text VARCHAR,
                    translate VARCHAR
                );
                """.trimIndent()
            )

            // Вставляем запись в таблицу
            statement.executeUpdate("INSERT INTO words (text, translate) VALUES ('hello', 'привет')")
        }
    } catch (e: SQLException) {
        // Обрабатываем исключения и выводим сообщение об ошибке
        println("Ошибка базы данных: ${e.message}")
    }
}
