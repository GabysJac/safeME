package com.example.safeme.service

interface LocationService {

    fun createLocationMap(
        latitude: Double,
        longitude: Double,
        description: String,
        onComplete: (String?) -> Unit

    )
}