package com.example.safeme.data

import com.example.safeme.service.LocationService

class LocationRepository(private val locationService: LocationService) {

    fun registerLocation(
        latitude: Double,
        longitude: Double,
        description: String,
        onComplete: (String?) -> Unit
    ) {
        locationService.createLocationMap(
            latitude,
            longitude,
            description
        )
        { locationId ->
            onComplete(locationId)
        }
    }
}