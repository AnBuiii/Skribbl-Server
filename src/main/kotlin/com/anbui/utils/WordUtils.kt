package com.anbui.utils

import java.io.File

val words = readWordList("src/main/resources/wordlist.txt")

/**
 * read word from static resource
 */
fun readWordList(fileName: String): List<String> {
    val inputStream = File(fileName).inputStream()
    val readWords = mutableListOf<String>()
    inputStream.bufferedReader().forEachLine {
        readWords.add(it)
    }
    return readWords
}

/**
 * get random [amount] word
 */
fun getRandomWord(amount: Int): List<String> = words.shuffled().take(amount)

/*
 * "random word" -> "_ _ _ _ _   _ _ _ _"
 */
fun String.transformToUnderscores() = map {
    if (it != ' ') '_' else ' '
}.joinToString(" ")
