package com.anbui.data.models.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent disconnect request send by client
 */
@Serializable
@SerialName(BaseModel.DISCONNECT)
class Disconnect() : BaseModel()
