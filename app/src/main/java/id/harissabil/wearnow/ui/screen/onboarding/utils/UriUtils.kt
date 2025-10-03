package id.harissabil.wearnow.ui.screen.onboarding.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun Uri.toFile(context: Context): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(this)
        val tempFile = File.createTempFile("upload_image", ".jpg", context.cacheDir)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
