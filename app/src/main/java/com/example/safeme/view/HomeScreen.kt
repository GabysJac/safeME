package com.example.safeme.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.SEND_SMS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import android.telephony.SmsManager
import android.location.Location

@Composable
fun HomeScreen(onPanicClicked: () -> Unit) {
    val circleSize = 300.dp
    val circleStrokeWidth = 30.dp
    val typography = MaterialTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .size(circleSize + circleStrokeWidth * 2)
            .clickable(onClick = onPanicClicked)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radiusOuter = size.minDimension / 2
            val radiusMiddle = radiusOuter - circleStrokeWidth.toPx()
            val radiusInner = radiusMiddle - circleStrokeWidth.toPx()

            // círculo exterior con opacidad
            drawCircle(
                color = Color(0xFFFF0000).copy(alpha = 0.27f),
                center = center,
                radius = radiusOuter
            )
            // círculo medio con opacidad
            drawCircle(
                color = Color(0xFFFF0000).copy(alpha = 0.24f),
                center = center,
                radius = radiusMiddle
            )
            // círculo interior sólido
            drawCircle(
                color = Color(0xFFFC5108),
                center = center,
                radius = radiusInner
            )
        }

        Text(
            "AUXILIO",
            color = Color.White,
            modifier = Modifier
                .clickable(onClick = onPanicClicked)
                .align(Alignment.Center),
            fontSize = typography.headlineMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
fun onPanicClicked(context: Context) {
    sendEmergencyNotifications(context)
    makeEmergencyCall(context)
}

private fun makeEmergencyCall(context: Context) {
    val callIntent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:911")
    }
    startActivity(context, callIntent, null)
}

private fun sendEmergencyNotifications(context: Context) {
    // Verifica si la app tiene permisos para acceder a la ubicación y enviar SMS
    if (ContextCompat.checkSelfPermission(context, SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        // Obtén la ubicación actual
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        // Si se obtuvo la ubicación, envía la ubicación junto con el mensaje de emergencia
        location?.let {
            // Formato de mensaje con latitud y longitud
            val message = "Emergencia! Estoy en la ubicación: Latitud ${location.latitude}, Longitud ${location.longitude}. Necesito ayuda!"

            // Envía el mensaje a cada contacto
            val smsManager = SmsManager.getDefault()
            val emergencyContacts = listOf("1234567890", "0987654321") // reemplaza con los números reales
            emergencyContacts.forEach { phoneNumber ->
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
        } ?: Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_LONG).show()
    } else {
        // Si no hay permisos, solicítalos o notifica al usuario
        Toast.makeText(context, "Permiso de ubicación y SMS requerido", Toast.LENGTH_SHORT).show()
        // Considera pedir los permisos aquí usando ActivityCompat.requestPermissions(...)
    }
}