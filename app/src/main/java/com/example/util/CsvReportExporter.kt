package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FieldActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvReportExporter {

    /**
     * Generates a structured CSV file with UTF-8 BOM and semicolon delimiters (standard for Excel in Brazil)
     * and triggers the Android share sheet.
     */
    fun exportAndShareCsv(context: Context, activities: List<FieldActivity>): Boolean {
        if (activities.isEmpty()) {
            Toast.makeText(context, "Nenhum registro selecionado para exportação.", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            val reportsDir = File(context.cacheDir, "reports").apply {
                if (!exists()) mkdirs()
            }

            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val csvFile = File(reportsDir, "Relatorio_Campo_$timestampStr.csv")

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

            FileOutputStream(csvFile).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // Write UTF-8 BOM for Microsoft Excel compatibility
                    writer.write("\uFEFF")

                    // Header line
                    val headers = listOf(
                        "ID",
                        "Data e Hora",
                        "Encarregado",
                        "Rodovia",
                        "Sentido",
                        "Tipo de Pista",
                        "KM Inicial",
                        "KM Final",
                        "Atividade",
                        "Tipo de Tacha",
                        "Tipo de Placa",
                        "Código da Placa",
                        "Texto da Placa",
                        "Faixa",
                        "Legenda (Descrição)",
                        "Eixo",
                        "Cadência",
                        "Latitude",
                        "Longitude",
                        "Status Envio",
                        "Observações",
                        "Foto Antes (Nome)",
                        "Foto Durante (Nome)",
                        "Foto Depois (Nome)"
                    )
                    writer.write(headers.joinToString(";") { escapeCsv(it) })
                    writer.write("\r\n")

                    // Rows
                    for (activity in activities) {
                        val dateStr = dateFormat.format(Date(activity.timestamp))
                        val statusStr = if (activity.isSent) "Enviado WhatsApp" else "Pendente"
                        val photoBeforeName = if (activity.photoBeforePath.isNotBlank()) File(activity.photoBeforePath).name else ""
                        val photoDuringName = if (activity.photoDuringPath.isNotBlank()) File(activity.photoDuringPath).name else ""
                        val photoAfterName = if (activity.photoAfterPath.isNotBlank()) File(activity.photoAfterPath).name else ""

                        val row = listOf(
                            activity.id.toString(),
                            dateStr,
                            activity.operatorName,
                            activity.highway,
                            activity.direction,
                            activity.laneType,
                            activity.kmStart,
                            activity.kmEnd,
                            activity.activityType,
                            activity.studType,
                            activity.plateType,
                            activity.plateCode,
                            activity.plateText,
                            activity.lane,
                            activity.legendDescription,
                            activity.eixo,
                            activity.cadence,
                            String.format(Locale.US, "%.6f", activity.latitude),
                            String.format(Locale.US, "%.6f", activity.longitude),
                            statusStr,
                            activity.observations,
                            photoBeforeName,
                            photoDuringName,
                            photoAfterName
                        )

                        writer.write(row.joinToString(";") { escapeCsv(it) })
                        writer.write("\r\n")
                    }
                    writer.flush()
                }
            }

            // Share via FileProvider
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, csvFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório de Campo CSV - ${activities.size} Registros")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Segue em anexo a planilha analítica dos apontamentos de campo de sinalização viária (${activities.size} registros)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Planilha CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao exportar CSV: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains(";") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }
}
