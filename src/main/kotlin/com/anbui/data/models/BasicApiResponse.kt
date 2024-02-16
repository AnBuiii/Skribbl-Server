package com.anbui.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BasicApiResponse(
    val isSuccessful: Boolean,
    val message: String? = null,
)
