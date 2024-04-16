package com.example.safeme.service
import android.util.Log
import com.example.safeme.model.Location
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FirestoreLocationService : LocationService {

    private val db = Firebase.firestore

    override fun createLocationMap(
        latitude: Double,
        longitude: Double,
        description: String,
        onComplete: (String?) -> Unit
    ) {
        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "description" to description
        )

        db.collection("locations").add(locationData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentId = task.result?.id
                onComplete(documentId)
            } else {
                onComplete(null)
            }
        }
    }


    fun listenForLocations(onLocationsReceived: (List<Location>) -> Unit) {
        db.collection("locations").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreLocationService", "Listen failed.", e)
                return@addSnapshotListener
            }

            val locations = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Location::class.java)
            }

            if (locations != null) {
                onLocationsReceived(locations)
            }
        }
    }
}
