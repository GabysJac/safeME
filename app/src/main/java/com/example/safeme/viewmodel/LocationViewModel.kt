package com.example.safeme.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeme.data.LocationRepository
import com.example.safeme.model.Location
import com.example.safeme.service.FirestoreLocationService
import kotlinx.coroutines.launch


class LocationViewModel : ViewModel() {

    private val locationService = FirestoreLocationService()
    private val locationRepository = LocationRepository(locationService)

    private val _locations = MutableLiveData<List<Location>>()
    val locations: LiveData<List<Location>> = _locations


    fun saveLocationToFirebase(location: Location, context: Context) {
        viewModelScope.launch {
            locationRepository.registerLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                description = location.description
            ) { locationId ->
                if (locationId != null) {

                    Toast.makeText(context, "Localización guardada con ID: $locationId", Toast.LENGTH_LONG).show()
                } else {

                    Toast.makeText(context, "Error al guardar la localización.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startListeningForLocations() {
        locationService.listenForLocations { locationsList ->
            _locations.value = locationsList
        }
    }

}
