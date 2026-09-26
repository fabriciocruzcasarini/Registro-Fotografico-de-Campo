package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Robust photo resolver that handles varied photo path formats, relocated files,
 * content URIs, file:// schemes, and ensures photos always display correctly.
 */
object PhotoFileResolver {

    /**
     * Resolves a photo path or URI to an existing [File] on disk.
     * Looks in:
     * 1. Direct path / stripped file:// URI
     * 2. App internal filesDir / Pictures
     * 3. App external filesDir / Pictures
     * 4. App cacheDir
     * 5. Fallback search by filename in all subdirectories
     */
    fun resolveFile(context: Context, pathOrUri: String?): File? {
        if (pathOrUri.isNullOrBlank()) return null

        // 1. If it's a file:// URI, strip prefix
        val cleanPath = when {
            pathOrUri.startsWith("file://") -> {
                try {
                    Uri.parse(pathOrUri).path ?: pathOrUri.removePrefix("file://")
                } catch (_: Exception) {
                    pathOrUri.removePrefix("file://")
                }
            }
            else -> pathOrUri
        }

        // 2. Direct absolute check (if it's not a content URI)
        if (!cleanPath.startsWith("content://")) {
            val direct = File(cleanPath)
            if (direct.exists() && direct.length() > 0) {
                return direct
            }
        }

        // 3. Extract filename (handles both file paths and content:// URIs)
        val fileName = try {
            if (cleanPath.startsWith("content://")) {
                val uri = Uri.parse(cleanPath)
                uri.lastPathSegment?.substringAfterLast('/') ?: File(cleanPath).name
            } else {
                File(cleanPath).name
            }
        } catch (_: Exception) {
            File(cleanPath).name
        }

        if (fileName.isNotBlank()) {
            val candidates = mutableListOf<File>()

            // Internal Pictures (Primary)
            candidates.add(File(File(context.filesDir, "Pictures"), fileName))
            candidates.add(File(context.filesDir, fileName))

            // External Pictures
            try {
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)?.let {
                    candidates.add(File(it, fileName))
                }
                context.getExternalFilesDir(null)?.let {
                    candidates.add(File(it, fileName))
                    candidates.add(File(File(it, "Pictures"), fileName))
                }
            } catch (_: Exception) {}

            // Cache
            candidates.add(File(context.cacheDir, fileName))
            candidates.add(File(File(context.cacheDir, "Pictures"), fileName))

            for (candidate in candidates) {
                if (candidate.exists() && candidate.length() > 0) {
                    return candidate
                }
            }

            // Recursive search inside internal filesDir
            try {
                context.filesDir.listFiles()?.forEach { sub ->
                    if (sub.isDirectory) {
                        val nested = File(sub, fileName)
                        if (nested.exists() && nested.length() > 0) return nested
                    }
                }
            } catch (_: Exception) {}

            // Recursive search inside external filesDir
            try {
                context.getExternalFilesDir(null)?.listFiles()?.forEach { sub ->
                    if (sub.isDirectory) {
                        val nested = File(sub, fileName)
                        if (nested.exists() && nested.length() > 0) return nested
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Resolves a model suitable for Coil [AsyncImage] (either a [File], [Uri], or [String]).
     * For internal display, prefer [File] or [Uri.fromFile].
     */
    fun resolveModel(context: Context, pathOrUri: String?): Any? {
        return resolveDisplayModel(context, pathOrUri)
    }

    /**
     * Returns a Coil-loadable model (File, file Uri, or content Uri) guaranteed not to fail
     * with FileProvider permission issues.
     */
    fun resolveDisplayModel(context: Context, pathOrUri: String?): Any? {
        if (pathOrUri.isNullOrBlank()) return null

        // 1. If file exists on disk, File object is 100% safest and fastest for Coil
        val resolved = resolveFile(context, pathOrUri)
        if (resolved != null && resolved.exists()) {
            return resolved
        }

        // 2. If it's a content URI from photo picker (not our FileProvider)
        if (pathOrUri.startsWith("content://") && !pathOrUri.contains(".fileprovider")) {
            return try {
                Uri.parse(pathOrUri)
            } catch (_: Exception) {
                pathOrUri
            }
        }

        // 3. If file:// scheme
        if (pathOrUri.startsWith("file://")) {
            val clean = pathOrUri.removePrefix("file://")
            val f = File(clean)
            if (f.exists()) return f
            return try { Uri.parse(pathOrUri) } catch (_: Exception) { pathOrUri }
        }

        // 4. Raw file path check
        val f = File(pathOrUri)
        if (f.exists()) {
            return f
        }

        return pathOrUri
    }

    /**
     * Resolves a URI strictly for internal app UI and ViewModel operations (e.g. form editing).
     * Always returns `file://` or raw content URI. Never returns FileProvider URI.
     */
    fun resolveDisplayUri(context: Context, pathOrUri: String?): Uri? {
        if (pathOrUri.isNullOrBlank()) return null

        val file = resolveFile(context, pathOrUri)
        if (file != null && file.exists()) {
            return Uri.fromFile(file)
        }

        if (pathOrUri.startsWith("content://") && !pathOrUri.contains(".fileprovider")) {
            return try {
                Uri.parse(pathOrUri)
            } catch (_: Exception) {
                null
            }
        }

        if (pathOrUri.startsWith("file://")) {
            return try {
                Uri.parse(pathOrUri)
            } catch (_: Exception) {
                null
            }
        }

        val direct = File(pathOrUri)
        if (direct.exists()) {
            return Uri.fromFile(direct)
        }

        return null
    }

    /**
     * Resolves a shareable [Uri] strictly for sharing with external apps (WhatsApp, PDF, Email).
     * Uses FileProvider with FLAG_GRANT_READ_URI_PERMISSION.
     */
    fun resolveUri(context: Context, pathOrUri: String?): Uri? {
        return resolveShareableUri(context, pathOrUri)
    }

    /**
     * Resolves a shareable FileProvider URI for external intents.
     */
    fun resolveShareableUri(context: Context, pathOrUri: String?): Uri? {
        if (pathOrUri.isNullOrBlank()) return null

        val file = resolveFile(context, pathOrUri)
        if (file != null && file.exists()) {
            return try {
                val authority = "${context.packageName}.fileprovider"
                FileProvider.getUriForFile(context, authority, file)
            } catch (_: Exception) {
                Uri.fromFile(file)
            }
        }

        if (pathOrUri.startsWith("content://")) {
            return try {
                Uri.parse(pathOrUri)
            } catch (_: Exception) {
                null
            }
        }

        if (pathOrUri.startsWith("file://")) {
            val f = File(pathOrUri.removePrefix("file://"))
            if (f.exists()) {
                return try {
                    val authority = "${context.packageName}.fileprovider"
                    FileProvider.getUriForFile(context, authority, f)
                } catch (_: Exception) {
                    Uri.fromFile(f)
                }
            }
        }

        return null
    }

    /**
     * Decodes a scaled Bitmap from a path or content URI safely for previews and exports.
     */
    fun decodeSampledBitmap(
        context: Context,
        pathOrUri: String?,
        reqWidth: Int = 1024,
        reqHeight: Int = 1024
    ): Bitmap? {
        if (pathOrUri.isNullOrBlank()) return null

        val file = resolveFile(context, pathOrUri)
        if (file != null && file.exists()) {
            return decodeSampledBitmapFromFile(file.absolutePath, reqWidth, reqHeight)
        }

        if (pathOrUri.startsWith("content://")) {
            return try {
                val uri = Uri.parse(pathOrUri)
                decodeSampledBitmapFromUri(context, uri, reqWidth, reqHeight)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (pathOrUri.startsWith("file://")) {
            val clean = pathOrUri.removePrefix("file://")
            return decodeSampledBitmapFromFile(clean, reqWidth, reqHeight)
        }

        val direct = File(pathOrUri)
        if (direct.exists()) {
            return decodeSampledBitmapFromFile(direct.absolutePath, reqWidth, reqHeight)
        }

        return null
    }

    private fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            options.inSampleSize = inSampleSize.coerceAtLeast(1)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return null

            // Correct EXIF orientation if needed
            val rotation = getExifRotationFromFile(path)
            rotateBitmapIfNeeded(bitmap, rotation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            options.inSampleSize = inSampleSize.coerceAtLeast(1)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val rotation = getExifRotationFromUri(context, uri)
            rotateBitmapIfNeeded(bitmap, rotation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExifRotationFromFile(path: String): Float {
        return try {
            val exifInterface = ExifInterface(path)
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (_: Throwable) {
            0f
        }
    }

    private fun getExifRotationFromUri(context: Context, uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = ExifInterface(stream)
                when (exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (_: Throwable) {
            0f
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        return try {
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            rotated
        } catch (_: Throwable) {
            bitmap
        }
    }
}
