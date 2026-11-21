package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.CategoryEntity
import com.example.data.ItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

  fun generateCatalogJson(category: CategoryEntity, items: List<ItemEntity>): String {
    val root = JSONObject()
    root.put("app_name", "SnapCatalog")
    root.put("list_name", category.name)
    root.put("folder_name", category.folderName)
    root.put("category_description", category.description)
    root.put("list_created_at", dateFormat.format(Date(category.createdAt)))
    root.put("exported_at", dateFormat.format(Date()))
    root.put("total_items", items.size)

    val itemsArray = JSONArray()
    items.forEachIndexed { index, item ->
      val itemObj = JSONObject()
      itemObj.put("index", index + 1)
      itemObj.put("id", item.id)
      itemObj.put("name", item.name)
      itemObj.put("file_name", item.imageFileName)
      // Folder location as requested: e.g. "रसोई का सामान/बर्तन.jpg"
      itemObj.put("folder_location", "${category.folderName}/${item.imageFileName}")
      itemObj.put("quantity", item.quantity)
      itemObj.put("notes", item.notes)
      itemObj.put("created_at", dateFormat.format(Date(item.createdAt)))
      itemsArray.put(itemObj)
    }

    root.put("items", itemsArray)
    return root.toString(2)
  }

  /**
   * Creates a ZIP file containing:
   * 1. data.json (with metadata and folder locations)
   * 2. [category.folderName]/[item.imageFileName] (each photo named after the item)
   */
  fun createCatalogZip(
    context: Context,
    category: CategoryEntity,
    items: List<ItemEntity>
  ): Result<File> {
    return try {
      val exportDir = File(context.cacheDir, "exports")
      if (!exportDir.exists()) {
        exportDir.mkdirs()
      }

      val safeZipName = "${FileManager.sanitizeFolderName(category.name)}_catalog.zip"
      val zipFile = File(exportDir, safeZipName)
      if (zipFile.exists()) {
        zipFile.delete()
      }

      val jsonContent = generateCatalogJson(category, items)

      ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
        // 1. Add data.json at root of ZIP
        val jsonEntry = ZipEntry("data.json")
        zos.putNextEntry(jsonEntry)
        zos.write(jsonContent.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // 2. Add each item's photo into [folder_name]/[photo_name.jpg]
        val categoryDir = FileManager.getCategoryDirectory(context, category.folderName)
        for (item in items) {
          val photoFile = File(categoryDir, item.imageFileName)
          if (photoFile.exists() && photoFile.isFile) {
            // Include folder name in zip path
            val entryPath = "${category.folderName}/${item.imageFileName}"
            val photoEntry = ZipEntry(entryPath)
            zos.putNextEntry(photoEntry)
            FileInputStream(photoFile).use { fis ->
              fis.copyTo(zos)
            }
            zos.closeEntry()
          }
        }
      }

      Result.success(zipFile)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  fun shareZipFile(context: Context, zipFile: File, categoryName: String, totalItems: Int) {
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, zipFile)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/zip"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(
        Intent.EXTRA_SUBJECT,
        "$categoryName - Photo Catalog & Data (ZIP)"
      )
      putExtra(
        Intent.EXTRA_TEXT,
        "Attached is the ZIP archive for '$categoryName' containing $totalItems item photos and data.json."
      )
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "Share $categoryName ZIP")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }
}
