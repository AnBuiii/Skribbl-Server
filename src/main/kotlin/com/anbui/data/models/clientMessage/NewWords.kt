package com.anbui.data.models.clientMessage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Present message of list of words drawing player will choose from
 */
@Serializable
@SerialName(BaseModel.NEW_WORD)
data class NewWords(
    val newWords: List<String>
) : BaseModel()
