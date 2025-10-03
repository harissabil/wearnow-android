package id.harissabil.wearnow.ui.screen.home.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageCompressionUtils {

    private const val TAG = "ImageCompression"

    // Compression settings
    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024
    private const val JPEG_QUALITY = 85 // 85% quality for good balance between size and quality
    private const val MAX_FILE_SIZE_KB = 500 // Target max file size in KB

    /**
     * Compress an image file and save it to a new location
     */
    fun compressImageFile(
        context: Context,
        sourceFile: File,
        targetFile: File = sourceFile,
    ): File {
        try {
            Log.d(TAG, "Starting compression for: ${sourceFile.name}")
            Log.d(TAG, "Original file size: ${sourceFile.length() / 1024}KB")

            // Read the original bitmap
            val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
                ?: throw IllegalArgumentException("Failed to decode image file")

            Log.d(TAG, "Original dimensions: ${originalBitmap.width}x${originalBitmap.height}")

            // Get image orientation from EXIF data
            val exif = ExifInterface(sourceFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // Rotate bitmap if needed
            val rotatedBitmap = rotateImageIfRequired(originalBitmap, orientation)

            // Resize the bitmap
            val resizedBitmap = resizeBitmap(rotatedBitmap, MAX_WIDTH, MAX_HEIGHT)

            Log.d(TAG, "Resized dimensions: ${resizedBitmap.width}x${resizedBitmap.height}")

            // Compress and save
            var quality = JPEG_QUALITY
            var compressedData: ByteArray

            do {
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedData = outputStream.toByteArray()

                val fileSizeKB = compressedData.size / 1024
                Log.d(TAG, "Compressed size at quality $quality: ${fileSizeKB}KB")

                if (fileSizeKB <= MAX_FILE_SIZE_KB) {
                    break
                }

                quality -= 10 // Reduce quality by 10% each iteration
            } while (quality > 50)

            // Save the compressed image
            FileOutputStream(targetFile).use { fileOutput ->
                fileOutput.write(compressedData)
            }

            // Clean up bitmaps
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            resizedBitmap.recycle()

            Log.d(TAG, "Compression completed")
            Log.d(TAG, "Final file size: ${targetFile.length() / 1024}KB")
            Log.d(TAG, "Compression ratio: ${String.format("%.1f", (sourceFile.length().toFloat() / targetFile.length()) * 100)}%")

            return targetFile

        } catch (e: Exception) {
            Log.e(TAG, "Image compression failed", e)
            throw e
        }
    }

    /**
     * Compress an image from URI and save to file
     */
    fun compressImageFromUri(
        context: Context,
        uri: Uri,
        targetFile: File,
    ): File {
        try {
            Log.d(TAG, "Starting compression from URI: $uri")

            // Open input stream from URI
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Failed to open input stream from URI")

            // Decode bitmap from input stream
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: throw IllegalArgumentException("Failed to decode image from URI")

            inputStream.close()

            Log.d(TAG, "Original dimensions: ${originalBitmap.width}x${originalBitmap.height}")

            // Try to get orientation from URI if it's a content URI
            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                Log.w(TAG, "Could not read EXIF data from URI", e)
                ExifInterface.ORIENTATION_NORMAL
            }

            // Rotate bitmap if needed
            val rotatedBitmap = rotateImageIfRequired(originalBitmap, orientation)

            // Resize the bitmap
            val resizedBitmap = resizeBitmap(rotatedBitmap, MAX_WIDTH, MAX_HEIGHT)

            Log.d(TAG, "Resized dimensions: ${resizedBitmap.width}x${resizedBitmap.height}")

            // Compress and save
            var quality = JPEG_QUALITY
            var compressedData: ByteArray

            do {
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedData = outputStream.toByteArray()

                val fileSizeKB = compressedData.size / 1024
                Log.d(TAG, "Compressed size at quality $quality: ${fileSizeKB}KB")

                if (fileSizeKB <= MAX_FILE_SIZE_KB) {
                    break
                }

                quality -= 10
            } while (quality > 50)

            // Save the compressed image
            FileOutputStream(targetFile).use { fileOutput ->
                fileOutput.write(compressedData)
            }

            // Clean up bitmaps
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            resizedBitmap.recycle()

            Log.d(TAG, "Compression completed")
            Log.d(TAG, "Final file size: ${targetFile.length() / 1024}KB")

            return targetFile

        } catch (e: Exception) {
            Log.e(TAG, "Image compression from URI failed", e)
            throw e
        }
    }

    /**
     * Resize bitmap while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate the scaling factor
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scaleFactor = minOf(scaleWidth, scaleHeight, 1f) // Don't upscale

        // If no scaling needed, return original
        if (scaleFactor >= 1f) {
            return bitmap
        }

        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        return bitmap.scale(newWidth, newHeight)
    }

    /**
     * Rotate image based on EXIF orientation
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, orientation: Int): Bitmap {
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
                matrix.postRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap // No rotation needed
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Get image file size in a human-readable format
     */
    fun getFileSizeString(file: File): String {
        val sizeInKB = file.length() / 1024
        return if (sizeInKB < 1024) {
            "${sizeInKB}KB"
        } else {
            String.format("%.1fMB", sizeInKB / 1024f)
        }
    }
}
