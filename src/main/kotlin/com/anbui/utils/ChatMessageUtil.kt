package com.anbui.utils

import com.anbui.data.models.messages.ChatMessage
import java.util.*

/**
 * Check if guess message sent by player match [word] or not
 */
fun ChatMessage.isMatchesWord(word: String): Boolean {
    return message.lowercase(Locale.getDefault()).trim() == word.lowercase(Locale.getDefault()).trim()
}