package com.anbui.data.models

import kotlinx.serialization.Serializable

/**
 * Basic response for all HTTP request
 */
@Serializable
data class BasicApiResponse(
    val isSuccessful: Boolean,
    val message: String? = null,
)
