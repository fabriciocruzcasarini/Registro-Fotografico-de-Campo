package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FieldActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareUtil {

    /**
     * Shares both watermarked photos (Antes/Depois) to WhatsApp or standard share sheet.
     */
    fun shareActivityToWhatsApp(context: Context, activity: FieldActivity): Boolean {
        val imageUris = ArrayList<Uri>()

        // Get FileProvider URIs for watermarked photos (Antes, Durante se houver, e Depois)
        getFileUri(context, activity.photoBeforePath)?.let { imageUris.add(it) }
        if (activity.photoDuringPath.isNotBlank()) {
            getFileUri(context, activity.photoDuringPath)?.let { imageUris.add(it) }
        }
        getFileUri(context, activity.photoAfterPath)?.let { imageUris.add(it) }

        if (imageUris.isEmpty()) {
            Toast.makeText(context, "Fotos não encontradas para envio.", Toast.LENGTH_SHORT).show()
            return false
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Try direct WhatsApp launch first
        val whatsappIntent = Intent(shareIntent).apply {
            setPackage("com.whatsapp")
        }

        return try {
            if (isPackageInstalled("com.whatsapp", context)) {
                context.startActivity(whatsappIntent)
                true
            } else if (isPackageInstalled("com.whatsapp.w4b", context)) { // WhatsApp Business
                whatsappIntent.setPackage("com.whatsapp.w4b")
                context.startActivity(whatsappIntent)
                true
            } else {
                // Fallback to chooser
                val chooser = Intent.createChooser(shareIntent, "Compartilhar Fotos do Relatório")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val chooser = Intent.createChooser(shareIntent, "Compartilhar Fotos do Relatório")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "Erro ao abrir aplicativo de compartilhamento", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun getFileUri(context: Context, filePath: String): Uri? {
        return PhotoFileResolver.resolveShareableUri(context, filePath)
    }


    private fun isPackageInstalled(packageName: String, context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
