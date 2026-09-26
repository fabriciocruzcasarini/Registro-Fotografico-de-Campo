package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.data.model.FieldActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object PdfReportExporter {

    // Standard A4 dimensions in points (72 points/inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 28f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    /**
     * Generates a corporate, engineering-grade PDF report.
     * - Maintains the main header.
     * - Renders all photos for a record in a single horizontal row (even with 3 photos).
     * - Does not draw text/tag overlays over the photos (preserving the crisp built-in watermarks).
     * - Places subsequent records on the lines below, paginating seamlessly onto subsequent pages.
     */
    fun exportAndSharePdf(context: Context, activities: List<FieldActivity>): Boolean {
        if (activities.isEmpty()) {
            Toast.makeText(context, "Nenhum registro selecionado para exportação.", Toast.LENGTH_SHORT).show()
            return false
        }

        val pdfDocument = PdfDocument()
        val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val emissionDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Paints initialization
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A") // Deep Slate/Navy
            style = Paint.Style.FILL
        }

        val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerSubtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val headerRightTopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8") // Sky blue
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val headerRightBottomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val cardHeaderBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }

        val cardHeaderTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val cardHeaderDatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val photoFrameBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
        }

        val photoFrameBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val noPhotoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val footerDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val footerPagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        fun drawPageHeader(canvas: Canvas) {
            val headerTop = 20f
            val headerHeight = 44f
            val headerRect = RectF(MARGIN, headerTop, MARGIN + CONTENT_WIDTH, headerTop + headerHeight)
            canvas.drawRoundRect(headerRect, 6f, 6f, headerBgPaint)

            canvas.drawText("RELATÓRIO TÉCNICO DE APONTAMENTO EM CAMPO", MARGIN + 12f, headerTop + 18f, headerTitlePaint)
            canvas.drawText("Sinalização Viária & Conservação Rodoviária • Total de Registros: ${activities.size}", MARGIN + 12f, headerTop + 32f, headerSubtitlePaint)

            canvas.drawText("EMISSÃO OFICIAL", MARGIN + CONTENT_WIDTH - 12f, headerTop + 18f, headerRightTopPaint)
            canvas.drawText(emissionDateStr, MARGIN + CONTENT_WIDTH - 12f, headerTop + 32f, headerRightBottomPaint)
        }

        fun drawPageFooter(canvas: Canvas, pageNum: Int) {
            val footerY = PAGE_HEIGHT - 16f
            canvas.drawLine(MARGIN, footerY - 10f, MARGIN + CONTENT_WIDTH, footerY - 10f, footerDividerPaint)
            canvas.drawText("FotodeCampo • Relatório de Campo Gerado em $emissionDateStr", MARGIN, footerY, footerTextPaint)
            canvas.drawText("Página $pageNum", MARGIN + CONTENT_WIDTH, footerY, footerPagePaint)
        }

        try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            drawPageHeader(canvas)
            var currentY = 72f

            for (index in activities.indices) {
                val act = activities[index]

                // Check how many photos exist for this activity
                val hasBefore = act.photoBeforePath.isNotBlank()
                val hasDuring = act.photoDuringPath.isNotBlank() && (File(act.photoDuringPath).exists() || act.photoDuringPath.startsWith("content://"))
                val hasAfter = act.photoAfterPath.isNotBlank()

                val photosList = mutableListOf<String>()
                if (hasBefore) photosList.add(act.photoBeforePath)
                if (hasDuring) photosList.add(act.photoDuringPath)
                if (hasAfter) photosList.add(act.photoAfterPath)

                val numPhotos = max(1, photosList.size)

                // Calculate dimensions for single-line photos
                // If 2 photos: width = (CONTENT_WIDTH - 20 - 10) / 2 = ~250pt, height = ~150pt
                // If 3 photos: width = (CONTENT_WIDTH - 20 - 20) / 3 = ~165pt, height = ~120pt
                val photoSpacing = 8f
                val innerPaddingX = 10f
                val availablePhotoWidth = CONTENT_WIDTH - (innerPaddingX * 2) - (photoSpacing * (numPhotos - 1))
                val photoBoxWidth = availablePhotoWidth / numPhotos
                val photoBoxHeight = when (numPhotos) {
                    1 -> 180f
                    2 -> 155f
                    else -> 125f
                }

                // Info block lines
                val hasObs = act.observations.isNotBlank()
                val infoLinesHeight = if (hasObs) 52f else 38f
                val cardHeaderHeight = 22f
                val totalCardHeight = cardHeaderHeight + infoLinesHeight + photoBoxHeight + 16f // 16f bottom margin inside card

                // Check if this activity fits on the current page
                if (currentY + totalCardHeight > PAGE_HEIGHT - 32f) {
                    // Close current page
                    drawPageFooter(canvas, currentPageNumber)
                    pdfDocument.finishPage(page)

                    // Start next page
                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    drawPageHeader(canvas)
                    currentY = 72f
                }

                // 1. Draw Card Background & Border
                val cardRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + totalCardHeight)
                canvas.drawRoundRect(cardRect, 6f, 6f, cardBgPaint)
                canvas.drawRoundRect(cardRect, 6f, 6f, cardBorderPaint)

                // 2. Draw Card Header Bar
                val cardHeaderRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + cardHeaderHeight)
                canvas.drawRoundRect(cardHeaderRect, 6f, 6f, cardHeaderBarPaint)
                canvas.drawRect(MARGIN, currentY + 10f, MARGIN + CONTENT_WIDTH, currentY + cardHeaderHeight, cardHeaderBarPaint)

                val activityTitle = buildString {
                    append("#${act.id} - ${act.activityType.uppercase()}")
                    if (act.studType.isNotBlank() && act.studType != "Selecione o Tipo de Tacha") {
                        append(" (${act.studType.uppercase()})")
                    }
                    if (act.plateType.isNotBlank() && act.plateType != "Selecione o Tipo de Placa") {
                        append(" (${act.plateType.uppercase()}${if (act.plateCode.isNotBlank()) " - ${act.plateCode}" else ""})")
                    }
                }
                canvas.drawText(activityTitle, MARGIN + innerPaddingX, currentY + 14.5f, cardHeaderTitlePaint)

                val itemDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(act.timestamp))
                canvas.drawText(itemDateStr, MARGIN + CONTENT_WIDTH - innerPaddingX, currentY + 14.5f, cardHeaderDatePaint)

                // 3. Technical Attributes Lines
                var infoY = currentY + cardHeaderHeight + 12f
                val col1X = MARGIN + innerPaddingX
                val col2X = MARGIN + (CONTENT_WIDTH * 0.40f)
                val col3X = MARGIN + (CONTENT_WIDTH * 0.72f)

                fun drawField(label: String, value: String, x: Float, y: Float) {
                    val labelText = "$label: "
                    canvas.drawText(labelText, x, y, labelPaint)
                    val labelWidth = labelPaint.measureText(labelText)
                    canvas.drawText(value, x + labelWidth, y, valuePaint)
                }

                // Line 1: Rodovia & Trecho & Encarregado
                drawField("Rodovia", "${act.highway} (${act.direction} • ${act.laneType})", col1X, infoY)
                drawField("Trecho", "KM ${act.kmStart}${if (act.kmEnd.isNotBlank()) " ao ${act.kmEnd}" else ""}", col2X, infoY)
                drawField("Encarregado", act.operatorName, col3X, infoY)

                // Line 2: Faixa/Eixo/Cadência & GPS & Status
                infoY += 13f
                val laneDisplay = when {
                    act.lane.isBlank() -> "-"
                    act.lane.equals("LEGENDA", ignoreCase = true) && act.legendDescription.isNotBlank() -> "LEGENDA (${act.legendDescription})"
                    else -> act.lane
                }
                val specText = buildString {
                    append("Faixa: $laneDisplay")
                    if (act.eixo.isNotBlank()) append(" | Eixo: ${act.eixo}")
                    if (act.cadence.isNotBlank()) append(" | Cadência: ${act.cadence}")
                    if (act.plateText.isNotBlank()) append(" | Texto: \"${act.plateText}\"")
                }
                drawField("Local", specText, col1X, infoY)
                drawField("GPS", String.format(Locale.US, "%.5f, %.5f", act.latitude, act.longitude), col2X, infoY)
                drawField("Status", if (act.isSent) "Enviado WhatsApp" else "Pendente", col3X, infoY)

                if (hasObs) {
                    infoY += 13f
                    drawField("Observações", act.observations.take(85), col1X, infoY)
                }

                // 4. Photos Row (All photos of this activity side-by-side in 1 single horizontal line)
                val photosY = infoY + 8f

                for (pIdx in 0 until numPhotos) {
                    val pLeft = MARGIN + innerPaddingX + (pIdx * (photoBoxWidth + photoSpacing))
                    val pRight = pLeft + photoBoxWidth
                    val pBottom = photosY + photoBoxHeight
                    val photoBoxRect = RectF(pLeft, photosY, pRight, pBottom)

                    // Draw clean container frame without distracting overlay tags
                    canvas.drawRoundRect(photoBoxRect, 4f, 4f, photoFrameBgPaint)
                    canvas.drawRoundRect(photoBoxRect, 4f, 4f, photoFrameBorderPaint)

                    val photoPath = if (pIdx < photosList.size) photosList[pIdx] else ""
                    val bitmap = loadOptimizedBitmap(context, photoPath, (photoBoxWidth * 2).toInt(), (photoBoxHeight * 2).toInt())

                    if (bitmap != null) {
                        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                        val innerImgRect = RectF(pLeft + 1f, photosY + 1f, pRight - 1f, pBottom - 1f)
                        canvas.drawBitmap(bitmap, srcRect, innerImgRect, null)
                        bitmap.recycle()
                    } else {
                        canvas.drawText("Sem foto", pLeft + (photoBoxWidth / 2f), photosY + (photoBoxHeight / 2f) + 3f, noPhotoTextPaint)
                    }
                }

                currentY += totalCardHeight + 10f // Spacing before next activity on the line below
            }

            // Draw final footer and finish document
            drawPageFooter(canvas, currentPageNumber)
            pdfDocument.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports").apply {
                if (!exists()) mkdirs()
            }

            val pdfFile = File(reportsDir, "Relatorio_Campo_$timestampStr.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Share PDF via FileProvider
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório Técnico de Campo - ${activities.size} Registros")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Segue em anexo o Relatório Técnico de Apontamentos em Campo (${activities.size} registros)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Relatório em PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            try { pdfDocument.close() } catch (_: Exception) {}
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    /**
     * Safely loads and samples large images to prevent memory issues during PDF generation.
     */
    private fun loadOptimizedBitmap(context: Context, pathOrUri: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (pathOrUri.isNullOrBlank()) return null

        val resolvedFile = PhotoFileResolver.resolveFile(context, pathOrUri)
        val filePath = resolvedFile?.absolutePath ?: when {
            pathOrUri.startsWith("file://") -> pathOrUri.removePrefix("file://")
            !pathOrUri.startsWith("content://") -> pathOrUri
            else -> ""
        }

        if (filePath.isNotBlank()) {
            val file = File(filePath)
            if (file.exists() && file.length() > 0) {
                return try {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, options)

                    if (options.outWidth <= 0 || options.outHeight <= 0) return null

                    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                    options.inJustDecodeBounds = false
                    options.inPreferredConfig = Bitmap.Config.RGB_565 // Memory optimization

                    val decodedBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

                    var orientation = ExifInterface.ORIENTATION_NORMAL
                    try {
                        val exif = ExifInterface(file.absolutePath)
                        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                    }

                    if (!matrix.isIdentity) {
                        val rotatedBitmap = Bitmap.createBitmap(
                            decodedBitmap,
                            0,
                            0,
                            decodedBitmap.width,
                            decodedBitmap.height,
                            matrix,
                            true
                        )
                        if (rotatedBitmap != decodedBitmap) {
                            decodedBitmap.recycle()
                        }
                        rotatedBitmap
                    } else {
                        decodedBitmap
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        // Fallback to PhotoFileResolver for content URIs
        return try {
            PhotoFileResolver.decodeSampledBitmap(context, pathOrUri, reqWidth, reqHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }
}
