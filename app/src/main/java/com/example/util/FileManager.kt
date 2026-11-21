package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileManager {

  private const val BASE_CATALOG_DIR = "catalogs"

  fun sanitizeFolderName(rawName: String): String {
    val trimmed = rawName.trim()
    val sanitized = trimmed.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    return if (sanitized.isBlank()) "list_${System.currentTimeMillis()}" else sanitized
  }

  fun sanitizeFileName(rawName: String): String {
    val trimmed = rawName.trim()
    val sanitized = trimmed.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    return if (sanitized.isBlank()) "item_${System.currentTimeMillis()}" else sanitized
  }

  fun getCategoryDirectory(context: Context, folderName: String): File {
    val dir = File(context.filesDir, "$BASE_CATALOG_DIR/$folderName")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir
  }

  fun createTempCameraUri(context: Context): Pair<Uri, File> {
    val tempDir = File(context.cacheDir, "camera_captures")
    if (!tempDir.exists()) {
      tempDir.mkdirs()
    }
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val tempFile = File(tempDir, "TEMP_CAPTURE_$timeStamp.jpg")
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, tempFile)
    return Pair(uri, tempFile)
  }

  /**
   * Saves or copies captured/picked image into the category folder.
   * Names the file after the item's name (e.g. "बर्तन.jpg" or "बर्तन (2).jpg").
   * Returns Pair(SavedFile, savedFileName).
   */
  fun saveItemPhoto(
    context: Context,
    sourceFile: File,
    folderName: String,
    itemName: String
  ): Pair<File, String>? {
    if (!sourceFile.exists() || sourceFile.length() == 0L) return null
    val targetDir = getCategoryDirectory(context, folderName)
    val safeBase = sanitizeFileName(itemName)

    var candidateName = "$safeBase.jpg"
    var targetFile = File(targetDir, candidateName)
    var counter = 2
    while (targetFile.exists()) {
      candidateName = "$safeBase ($counter).jpg"
      targetFile = File(targetDir, candidateName)
      counter++
    }

    return try {
      sourceFile.copyTo(targetFile, overwrite = true)
      // Remove temporary file
      sourceFile.delete()
      Pair(targetFile, candidateName)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * Saves image stream from Gallery Uri into the category folder.
   */
  fun saveItemPhotoFromUri(
    context: Context,
    sourceUri: Uri,
    folderName: String,
    itemName: String
  ): Pair<File, String>? {
    val targetDir = getCategoryDirectory(context, folderName)
    val safeBase = sanitizeFileName(itemName)

    var candidateName = "$safeBase.jpg"
    var targetFile = File(targetDir, candidateName)
    var counter = 2
    while (targetFile.exists()) {
      candidateName = "$safeBase ($counter).jpg"
      targetFile = File(targetDir, candidateName)
      counter++
    }

    return try {
      context.contentResolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }
      Pair(targetFile, candidateName)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun getItemFile(context: Context, folderName: String, fileName: String): File {
    return File(getCategoryDirectory(context, folderName), fileName)
  }

  fun deleteItemPhoto(context: Context, folderName: String, fileName: String): Boolean {
    val file = getItemFile(context, folderName, fileName)
    return if (file.exists()) file.delete() else true
  }

  fun deleteCategoryDirectory(context: Context, folderName: String): Boolean {
    val dir = getCategoryDirectory(context, folderName)
    return if (dir.exists()) dir.deleteRecursively() else true
  }
}
