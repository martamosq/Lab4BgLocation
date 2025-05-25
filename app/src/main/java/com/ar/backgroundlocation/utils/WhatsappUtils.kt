package com.ar.backgroundlocation.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import androidx.core.net.toUri

fun sendMessageViaWhatsapp(context: Context, phone: String, message: String) {
    try {
        val uri = ("https://wa.me/${phone.replace("+", "").replace(" ", "")}" +
                "?text=${URLEncoder.encode(message, "UTF-8")}").toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al enviar mensaje: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
