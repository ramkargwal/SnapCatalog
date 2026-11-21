package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CategoryEntity
import com.example.data.ItemEntity
import com.example.util.FileManager
import com.example.util.ZipExporter
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SnapCatalog", appName)
  }

  @Test
  fun `test json export generation with items and locations`() {
    val category = CategoryEntity(
      id = 1,
      name = "Kitchen Items",
      folderName = "Kitchen Items",
      description = "Cookware and appliances"
    )

    val items = listOf(
      ItemEntity(
        id = 10,
        categoryId = 1,
        name = "Utensil",
        imageFileName = "Utensil.jpg",
        relativeLocation = "Kitchen Items/Utensil.jpg",
        notes = "Stainless steel plate set",
        quantity = 2
      ),
      ItemEntity(
        id = 11,
        categoryId = 1,
        name = "Gas Stove",
        imageFileName = "Gas Stove.jpg",
        relativeLocation = "Kitchen Items/Gas Stove.jpg",
        notes = "2 burner stove",
        quantity = 1
      )
    )

    val jsonString = ZipExporter.generateCatalogJson(category, items)
    val jsonObject = JSONObject(jsonString)

    assertEquals("SnapCatalog", jsonObject.getString("app_name"))
    assertEquals("Kitchen Items", jsonObject.getString("list_name"))
    assertEquals("Kitchen Items", jsonObject.getString("folder_name"))
    assertEquals(2, jsonObject.getInt("total_items"))

    val itemsArray = jsonObject.getJSONArray("items")
    assertEquals(2, itemsArray.length())

    val firstItem = itemsArray.getJSONObject(0)
    assertEquals("Utensil", firstItem.getString("name"))
    assertEquals("Utensil.jpg", firstItem.getString("file_name"))
    assertEquals("Kitchen Items/Utensil.jpg", firstItem.getString("folder_location"))
    assertEquals(2, firstItem.getInt("quantity"))

    val secondItem = itemsArray.getJSONObject(1)
    assertEquals("Gas Stove", secondItem.getString("name"))
    assertEquals("Gas Stove.jpg", secondItem.getString("file_name"))
    assertEquals("Kitchen Items/Gas Stove.jpg", secondItem.getString("folder_location"))
  }

  @Test
  fun `test file manager sanitization and paths`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sanitizedFolder = FileManager.sanitizeFolderName("Kitchen Items/Special")
    assertEquals("Kitchen Items_Special", sanitizedFolder)

    val sanitizedFile = FileManager.sanitizeFileName("Gas:Stove*")
    assertEquals("Gas_Stove_", sanitizedFile)

    val catDir = FileManager.getCategoryDirectory(context, "Kitchen Items")
    assertTrue(catDir.exists())
  }
}
