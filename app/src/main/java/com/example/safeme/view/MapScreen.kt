package com.example.safeme.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeme.viewmodel.LocationViewModel
import com.example.safeme.model.Location
import com.google.android.gms.maps.model.MarkerOptions


@SuppressLint("MissingPermission")
@Composable
fun MapScreen(viewModel: LocationViewModel = viewModel()) {

    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val mapView = rememberMapViewWithLifecycle()
    var googleMap: GoogleMap? by remember { mutableStateOf(null) }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var description by remember { mutableStateOf("") }
    var showAlertDialog by remember { mutableStateOf(false) }
    var currentLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    var locations by remember { mutableStateOf(emptyList<Location>()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val shouldMoveCameraToUser = remember { mutableStateOf(true) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return


                if (shouldMoveCameraToUser.value) {
                    val location = locationResult.lastLocation
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))

                    shouldMoveCameraToUser.value = false
                }
            }
        }
    }
    val showPermissionExplanationDialog = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido
                viewModel.startListeningForLocations()
                shouldMoveCameraToUser.value = true
            } else {
                // Permiso denegado
                shouldMoveCameraToUser.value = false
            }
        }
    )

    LaunchedEffect(key1 = true) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED -> {
                        //escucha los puntos guardados
                viewModel.startListeningForLocations()
            }
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) == true -> {

                showPermissionExplanationDialog.value = true
            }
            else -> {

                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    if (showPermissionExplanationDialog.value) {
        AlertDialog(
            onDismissRequest = { showPermissionExplanationDialog.value = false },
            title = { Text("Permiso de ubicación necesario") },
            text = { Text("Esta aplicación necesita el permiso de ubicación para mostrar tu posición actual en el mapa.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionExplanationDialog.value = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    LaunchedEffect(key1 = googleMap) {
        if (googleMap == null) {
            mapView.getMapAsync { map ->
                googleMap = map
                map.uiSettings.isMyLocationButtonEnabled = true
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }
            }
        }


    }

    LaunchedEffect(key1 = Unit) {
        mapView.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isMyLocationButtonEnabled = true

            // Permiso de ubicación, necesario para habilitar el botón de ubicación actual
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
            }

            // Configurar listener para clics prolongados en el mapa
            map.setOnMapLongClickListener { latLng ->
                currentLatLng = latLng
                showAlertDialog = true
            }
        }
    }

    /*LaunchedEffect(viewModel.locations) {
        viewModel.locations.observeForever { updatedLocations ->
            locations = updatedLocations
        }
    }*/
    LaunchedEffect(locations) {
        googleMap?.clear() // Limpia los marcadores
        locations.forEach { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(location.description)
            )
        }
    }

    DisposableEffect(key1 = googleMap) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = Observer<List<Location>> { updatedLocations ->
            locations = updatedLocations
        }
        viewModel.locations.observe(lifecycleOwner, observer)
        onDispose {
            viewModel.locations.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = Modifier,
        factory = { mapView },
        update = { mapView ->
            mapView.getMapAsync { map ->
                googleMap = map
                map.setMapStyle(null)
                map.uiSettings.apply {
                    isMyLocationButtonEnabled = true
                    isZoomControlsEnabled = true
                }
                // Comprueba y solicita permisos si es necesario
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }
            }
        }

    )

    if (showAlertDialog) {
        // Composable de diálogo se coloca directamente en el cuerpo de otro composable
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Zona de riesgo") },
            text = {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val locationInstance = Location(
                            latitude = currentLatLng.latitude,
                            longitude = currentLatLng.longitude,
                            description = description
                        )
                        viewModel.saveLocationToFirebase(locationInstance, context)

                        showAlertDialog = false
                        currentLatLng = LatLng(0.0, 0.0)
                        description = ""
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showAlertDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

}




@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = MapView(context)
    mapView.onCreate(null)

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val lifecycleObserver = MapViewLifecycleObserver(mapView, lifecycle)
        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

class MapViewLifecycleObserver(
    private val mapView: MapView,
    private val lifecycle: Lifecycle
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }
}