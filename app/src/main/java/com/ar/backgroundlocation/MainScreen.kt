package com.ar.backgroundlocation

import android.content.Context
import android.content.Intent
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.compose.material3.Button

import androidx.lifecycle.viewmodel.compose.viewModel
import com.ar.backgroundlocation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.ar.backgroundlocation.utils.showNotification
import com.ar.backgroundlocation.utils.sendMessageViaWhatsapp

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat


// Definición del ViewModel dentro del mismo archivo
class LocationViewModel : ViewModel() {
    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow

    // Método para actualizar la ubicación desde LocationService u otra fuente
    fun updateLocation(location: Location) {
        _locationFlow.value = location
    }
}

@Composable
fun MainScreen(viewModel: LocationViewModel = viewModel()) {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

// Este bloque escucha los broadcasts de ubicación y actualiza el ViewModel
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val lat = intent?.getDoubleExtra("latitude", 0.0)
                val lon = intent?.getDoubleExtra("longitude", 0.0)
                if (lat != null && lon != null) {
                    val location = Location("provider").apply {
                        latitude = lat
                        longitude = lon
                    }
                    viewModel.updateLocation(location)
                }
            }
        }

        val filter = IntentFilter("com.ar.backgroundlocation.LOCATION_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val location by viewModel.locationFlow.collectAsState(initial = null)
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enviar Ubicación por WhatsApp", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.whatslogo),
            contentDescription = "WhatsApp Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            placeholder = { Text("507XXXXXXX") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensaje") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (location != null) {
                val fullMessage = "$message\nMi ubicación es: https://maps.google.com/?q=${location!!.latitude},${location!!.longitude}"
                sendMessageViaWhatsapp(context, phoneNumber, fullMessage)
                showNotification(context, "Mensaje enviado", "Mensaje con ubicación enviado")
            } else {
                showNotification(context, "Error", "Ubicación no disponible")
            }
        }) {
            Text("Enviar por WhatsApp")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            Toast.makeText(context, "Service Start button clicked", Toast.LENGTH_SHORT).show()
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_SERVICE_START
                context.startService(this)
            }
        }) {
            Text("Empezar a rastrear ubicación")
        }

        Spacer(modifier = Modifier.padding(12.dp))

        Button(onClick = {
            Toast.makeText(context, "Service Stop button clicked", Toast.LENGTH_SHORT).show()
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_SERVICE_STOP
                context.startService(this)
            }
        }) {
            Text("Dejar de rastear ubicación")
        }
    }
}
