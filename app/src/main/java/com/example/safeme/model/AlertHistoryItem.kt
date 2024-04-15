package com.example.safeme.model

import androidx.compose.runtime.Immutable

@Immutable
data class AlertHistoryItem(
    val id: Int,
    val type: String,
    val timestamp: Long,
    val location: String,
    val description: String
)