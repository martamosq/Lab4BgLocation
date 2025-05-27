package com.ar.backgroundlocation
import com.ar.backgroundlocation.LocationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun MainScreen() {
    val context = LocalContext.current

    val latitude = remember { mutableStateOf<Double?>(null) }
    val longitude = remember { mutableStateOf<Double?>(null) }

    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Pide la última ubicación conocida una vez que la UI se monta
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        latitude.value = loc.latitude
                        longitude.value = loc.longitude
                    }
                }
                .addOnFailureListener {
                    // ignoramos
                }
        }
    }

    // UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Título principal
            Text(
                text = "Compartir ubicación",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // Placeholder de imagen (Box con ícono de imagen)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📍 Ayudaaa", color = Color.Gray)
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } || input.isEmpty()) {
                        phoneNumber = input
                    }
                },
                label = { Text("Número de teléfono") },
                placeholder = { Text("Ej: 50713456789") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Mensaje") },
                placeholder = { Text("Escribe tu mensaje") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val fullMessage = if (latitude.value != null && longitude.value != null) {
                        "$message\n\nUbicación: https://maps.google.com/?q=${latitude.value},${longitude.value}"
                    } else {
                        "$message\n\nUbicación: no disponible"
                    }
                    val uri = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(fullMessage)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                enabled = phoneNumber.isNotBlank() && message.isNotBlank() // aquí puedes cambiar para que también requiera ubicación:
                // enabled = phoneNumber.isNotBlank() && message.isNotBlank() && latitude.value != null && longitude.value != null
            ) {
                Text("Enviar por WhatsApp")
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (latitude.value != null && longitude.value != null)
                    "Ubicación actual: ${latitude.value}, ${longitude.value}"
                else
                    "Obteniendo ubicación...",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

        }
    }
}



