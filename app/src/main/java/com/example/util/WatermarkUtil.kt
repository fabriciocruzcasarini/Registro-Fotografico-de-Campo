package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WatermarkUtil {

    enum class PhotoType(val label: String, val badgeColor: Int) {
        BEFORE("ANTES", Color.parseColor("#EAB308")), // Yellow/Amber
        DURING("DURANTE", Color.parseColor("#0284C7")), // Blue/Sky
        AFTER("DEPOIS", Color.parseColor("#22C55E")) // Green
    }

    /**
     * Applies a professional field watermark onto a photo bitmap and saves it to disk.
     */
    fun createWatermarkedPhoto(
        context: Context,
        sourceUri: Uri,
        photoType: PhotoType,
        operatorName: String,
        highway: String,
        direction: String,
        laneType: String,
        activityType: String = "Implantação Tacha",
        studType: String = "",
        plateType: String = "",
        plateCode: String = "",
        plateText: String = "",
        lane: String = "",
        legendDescription: String = "",
        eixo: String = "",
        cadence: String = "",
        observations: String = "",
        kmStart: String,
        kmEnd: String,
        latitude: Double,
        longitude: Double,
        timestampMs: Long = System.currentTimeMillis()
    ): String {
        val originalBitmap = loadAndCorrectBitmap(context, sourceUri)
            ?: createFallbackBitmap(photoType.label)

        val watermarkedBitmap = applyWatermarkToBitmap(
            bitmap = originalBitmap,
            photoType = photoType,
            operatorName = operatorName,
            highway = highway,
            direction = direction,
            laneType = laneType,
            activityType = activityType,
            studType = studType,
            plateType = plateType,
            plateCode = plateCode,
            plateText = plateText,
            lane = lane,
            legendDescription = legendDescription,
            eixo = eixo,
            cadence = cadence,
            observations = observations,
            kmStart = kmStart,
            kmEnd = kmEnd,
            latitude = latitude,
            longitude = longitude,
            timestampMs = timestampMs
        )

        val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date(timestampMs))
        val fileName = "WM_${photoType.name}_$timeStampStr.jpg"
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            ?: context.filesDir

        val outputFile = File(storageDir, fileName)
        FileOutputStream(outputFile).use { out ->
            watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        return outputFile.absolutePath
    }

    private fun applyWatermarkToBitmap(
        bitmap: Bitmap,
        photoType: PhotoType,
        operatorName: String,
        highway: String,
        direction: String,
        laneType: String,
        activityType: String,
        studType: String,
        plateType: String = "",
        plateCode: String = "",
        plateText: String = "",
        lane: String,
        legendDescription: String,
        eixo: String,
        cadence: String,
        observations: String,
        kmStart: String,
        kmEnd: String,
        latitude: Double,
        longitude: Double,
        timestampMs: Long
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)

        // 1. Draw Top Ribbon / Badge (Faixa no topo da foto com identificação ANTES / DURANTE / DEPOIS)
        val topBarHeight = (height * 0.057f).coerceIn(42f, 75f)
        val topBarPaint = Paint().apply {
            color = photoType.badgeColor // Yellow (#EAB308) for ANTES, Blue for DURANTE, Green (#22C55E) for DEPOIS
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, topBarPaint)

        val topText = buildString {
            append(photoType.label)
            if (activityType.isNotBlank()) {
                append(" • ")
                append(activityType.uppercase())
            }
            if (studType.isNotBlank() && studType != "Selecione o Tipo de Tacha") {
                append(" (${studType.uppercase()})")
            }
            if (plateType.isNotBlank() && plateType != "Selecione o Tipo de Placa") {
                append(" (${plateType.uppercase()}${if (plateCode.isNotBlank()) " - $plateCode" else ""})")
            }
        }

        val topTextPaint = Paint().apply {
            color = if (photoType == PhotoType.BEFORE) Color.BLACK else Color.WHITE
            textSize = (topBarHeight * 0.57f).coerceAtLeast(20f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val topTextY = (topBarHeight / 2f) - ((topTextPaint.descent() + topTextPaint.ascent()) / 2f)
        canvas.drawText(topText, width / 2f, topTextY, topTextPaint)

        // 2. Text formatting paint for bottom data lines (+10% increased frame and typography)
        val textSize = (height * 0.0253f).coerceIn(18f, 38f)
        val lineSpacing = textSize * 1.34f
        val verticalPadding = textSize * 0.88f
        val paddingLeft = (width * 0.022f).coerceAtLeast(18f)

        val textPaint = Paint().apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaintRegular = Paint(textPaint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.parseColor("#E2E8F0")
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(timestampMs))

        val line1 = "Encarregado: $operatorName | $dateStr"
        val line2 = buildString {
            append("Rodovia: $highway | $direction | $laneType")
            if (studType.isNotBlank() && studType != "Selecione o Tipo de Tacha") {
                append(" | Tipo: $studType")
            }
            if (plateType.isNotBlank() && plateType != "Selecione o Tipo de Placa") {
                append(" | Placa: $plateType")
                if (plateCode.isNotBlank()) append(" ($plateCode)")
                if (plateText.isNotBlank()) append(" - \"$plateText\"")
            }
        }
        val line3 = buildString {
            val laneDisplay = when {
                lane.isBlank() -> "-"
                lane.equals("LEGENDA", ignoreCase = true) && legendDescription.isNotBlank() -> "LEGENDA ($legendDescription)"
                else -> lane
            }
            append("Faixa: $laneDisplay")
            if (eixo.isNotBlank()) {
                append(" | Eixo: $eixo")
            }
            append(" | Cadência: ${if (cadence.isNotBlank()) cadence else "-"}")
        }
        val line4 = "Trecho: KM $kmStart ao KM $kmEnd"
        val line5 = String.format(Locale.US, "Geo: LAT %.6f, LONG %.6f", latitude, longitude)
        val hasObs = observations.isNotBlank()

        val lineCount = if (hasObs) 6 else 5
        // Calculate exact tight banner height to fit text and stick to bottom (+10% increased frame)
        val bannerHeight = (lineCount * lineSpacing) + (verticalPadding * 1.32f)
        val bannerTop = height.toFloat() - bannerHeight

        // Dark semi-transparent background for banner glued to the bottom
        val bgPaint = Paint().apply {
            color = Color.argb(225, 15, 23, 42) // Deep navy #0F172A with 88% opacity
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)

        var currentY = bannerTop + verticalPadding + textSize

        canvas.drawText(line1, paddingLeft, currentY, textPaint)
        currentY += lineSpacing

        canvas.drawText(line2, paddingLeft, currentY, textPaintRegular)
        currentY += lineSpacing

        canvas.drawText(line3, paddingLeft, currentY, textPaintRegular)
        currentY += lineSpacing

        canvas.drawText(line4, paddingLeft, currentY, textPaintRegular)
        currentY += lineSpacing

        canvas.drawText(line5, paddingLeft, currentY, textPaintRegular)

        if (hasObs) {
            currentY += lineSpacing
            val obsText = "OBS: ${observations.take(65)}${if (observations.length > 65) "..." else ""}"
            canvas.drawText(obsText, paddingLeft, currentY, textPaintRegular)
        }

        return workingBitmap
    }

    private fun loadAndCorrectBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            // 1. Decode bounds to compute optimal inSampleSize (HD Resolution: max 1280px)
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val maxDimension = 1280 // HD 720p standard (1280x720 / 1280x960)
            var inSampleSize = 1
            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth > maxDimension || rawHeight > maxDimension) {
                val halfWidth = rawWidth / 2
                val halfHeight = rawHeight / 2
                while ((halfWidth / inSampleSize) >= maxDimension || (halfHeight / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 2. Read EXIF Orientation
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Decode sampled bitmap into memory
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // 4. Apply transformation matrix based on EXIF orientation
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
            }

            val orientedBitmap = if (!matrix.isIdentity) {
                val rotated = Bitmap.createBitmap(
                    decodedBitmap,
                    0,
                    0,
                    decodedBitmap.width,
                    decodedBitmap.height,
                    matrix,
                    true
                )
                if (rotated != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                rotated
            } else {
                decodedBitmap
            }

            // 5. Ensure exact HD resolution (max 1280px on longest edge)
            val currentMax = maxOf(orientedBitmap.width, orientedBitmap.height)
            if (currentMax > maxDimension) {
                val scale = maxDimension.toFloat() / currentMax.toFloat()
                val targetW = (orientedBitmap.width * scale).toInt()
                val targetH = (orientedBitmap.height * scale).toInt()
                val scaled = Bitmap.createScaledBitmap(orientedBitmap, targetW, targetH, true)
                if (scaled != orientedBitmap) {
                    orientedBitmap.recycle()
                }
                scaled
            } else {
                orientedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a sample field photo bitmap for testing when physical camera/file is unavailable.
     */
    fun createFallbackBitmap(title: String): Bitmap {
        val width = 1280
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background field gradient / color
        val bgPaint = Paint().apply {
            color = Color.parseColor("#334155") // Slate background
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Grid lines to simulate field/road geometry
        val gridPaint = Paint().apply {
            color = Color.parseColor("#475569")
            strokeWidth = 4f
        }
        for (i in 0..width step 160) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), gridPaint)
        }
        for (j in 0..height step 120) {
            canvas.drawLine(0f, j.toFloat(), width.toFloat(), j.toFloat(), gridPaint)
        }

        // Road lanes drawing simulation
        val roadPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(200f, 300f, 1080f, 800f, roadPaint)

        val yellowLinePaint = Paint().apply {
            color = Color.parseColor("#F59E0B")
            strokeWidth = 12f
        }
        //canvas.drawLine(640f, 300f, 640f, 800f, yellowLinePaint)

        // Title Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        //canvas.drawText(title, width / 2f, height / 2f, titlePaint)

        return bitmap
    }
}
