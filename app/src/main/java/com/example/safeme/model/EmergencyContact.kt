package com.example.safeme.model

import androidx.compose.runtime.Immutable

@Immutable
data class EmergencyContact(
    val id: Int,
    val name: String,
    val phone: String,
    val relationship: String
)