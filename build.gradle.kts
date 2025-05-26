plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("ru.androidsprint.englishtrainer.telegram.TelegramKt")
}

kotlin {
    jvmToolchain(11)
}