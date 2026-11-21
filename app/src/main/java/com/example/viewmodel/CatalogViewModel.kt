package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CategoryEntity
import com.example.data.CategoryWithItemCount
import com.example.data.ItemEntity
import com.example.util.FileManager
import com.example.util.ZipExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()

  // Category search & list
  val categorySearchQuery = MutableStateFlow("")
  private val allCategories = categoryDao.getAllCategoriesWithCount()

  val filteredCategories: StateFlow<List<CategoryWithItemCount>> =
    combine(allCategories, categorySearchQuery) { list, query ->
      if (query.isBlank()) list
      else list.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Currently selected category
  private val _selectedCategoryId = MutableStateFlow<Long?>(null)
  val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

  private val _currentCategory = MutableStateFlow<CategoryEntity?>(null)
  val currentCategory: StateFlow<CategoryEntity?> = _currentCategory.asStateFlow()

  // Items in current category with search query
  val itemSearchQuery = MutableStateFlow("")

  val currentItems: StateFlow<List<ItemEntity>> =
    _selectedCategoryId.flatMapLatest { catId ->
      if (catId == null) flowOf(emptyList())
      else {
        itemSearchQuery.flatMapLatest { query ->
          if (query.isBlank()) {
            itemDao.getItemsForCategory(catId)
          } else {
            itemDao.searchItemsInCategory(catId, query)
          }
        }
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Pending photo capture awaiting name input
  data class PendingCapture(
    val tempFile: File? = null,
    val galleryUri: Uri? = null,
  )

  private val _pendingCapture = MutableStateFlow<PendingCapture?>(null)
  val pendingCapture: StateFlow<PendingCapture?> = _pendingCapture.asStateFlow()

  // UI status / messages
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  // Export & ZIP status
  data class ZipExportState(
    val zipFile: File? = null,
    val jsonContent: String = "",
    val categoryName: String = "",
    val totalItems: Int = 0,
    val isExporting: Boolean = false,
  )

  private val _zipExportState = MutableStateFlow<ZipExportState?>(null)
  val zipExportState: StateFlow<ZipExportState?> = _zipExportState.asStateFlow()

  init {
    seedInitialCategoryIfNeeded()
  }

  private fun seedInitialCategoryIfNeeded() {
    viewModelScope.launch(Dispatchers.IO) {
      if (categoryDao.getCategoryCount() == 0) {
        val defaultCategory = CategoryEntity(
          name = "Kitchen Items",
          folderName = "Kitchen Items",
          colorHex = "#026A75",
          iconName = "kitchen",
          description = "Utensils, cookware, appliances and pantry items"
        )
        categoryDao.insertCategory(defaultCategory)
      }
    }
  }

  fun selectCategory(categoryId: Long?) {
    _selectedCategoryId.value = categoryId
    itemSearchQuery.value = ""
    if (categoryId != null) {
      viewModelScope.launch(Dispatchers.IO) {
        _currentCategory.value = categoryDao.getCategoryById(categoryId)
      }
    } else {
      _currentCategory.value = null
    }
  }

  fun createCategory(name: String, description: String, colorHex: String) {
    if (name.isBlank()) return
    viewModelScope.launch(Dispatchers.IO) {
      val folderName = FileManager.sanitizeFolderName(name)
      val newCategory = CategoryEntity(
        name = name.trim(),
        folderName = folderName,
        colorHex = colorHex,
        description = description.trim()
      )
      categoryDao.insertCategory(newCategory)
      // Ensure folder exists on disk
      FileManager.getCategoryDirectory(getApplication(), folderName)
      withContext(Dispatchers.Main) {
        _toastMessage.value = "List '$name' created"
      }
    }
  }

  fun deleteCategory(categoryWithCount: CategoryWithItemCount) {
    viewModelScope.launch(Dispatchers.IO) {
      val category = categoryDao.getCategoryById(categoryWithCount.id)
      if (category != null) {
        categoryDao.deleteCategory(category)
        FileManager.deleteCategoryDirectory(getApplication(), category.folderName)
        withContext(Dispatchers.Main) {
          if (_selectedCategoryId.value == category.id) {
            _selectedCategoryId.value = null
            _currentCategory.value = null
          }
          _toastMessage.value = "List '${category.name}' deleted"
        }
      }
    }
  }

  fun onCameraPhotoCaptured(tempFile: File) {
    _pendingCapture.value = PendingCapture(tempFile = tempFile)
  }

  fun onGalleryPhotoPicked(uri: Uri) {
    _pendingCapture.value = PendingCapture(galleryUri = uri)
  }

  fun dismissPendingCapture() {
    _pendingCapture.value?.tempFile?.delete()
    _pendingCapture.value = null
  }

  fun saveCapturedItem(
    itemName: String,
    notes: String,
    quantity: Int,
    onSuccess: (() -> Unit)? = null
  ) {
    val currentCat = _currentCategory.value ?: return
    val capture = _pendingCapture.value ?: return
    val safeName = itemName.trim().ifBlank { "New Item" }

    viewModelScope.launch(Dispatchers.IO) {
      val savedResult = if (capture.tempFile != null) {
        FileManager.saveItemPhoto(
          context = getApplication(),
          sourceFile = capture.tempFile,
          folderName = currentCat.folderName,
          itemName = safeName
        )
      } else if (capture.galleryUri != null) {
        FileManager.saveItemPhotoFromUri(
          context = getApplication(),
          sourceUri = capture.galleryUri,
          folderName = currentCat.folderName,
          itemName = safeName
        )
      } else null

      if (savedResult != null) {
        val (savedFile, savedFileName) = savedResult
        val itemEntity = ItemEntity(
          categoryId = currentCat.id,
          name = safeName,
          imageFileName = savedFileName,
          relativeLocation = "${currentCat.folderName}/$savedFileName",
          notes = notes.trim(),
          quantity = if (quantity > 0) quantity else 1
        )
        itemDao.insertItem(itemEntity)

        withContext(Dispatchers.Main) {
          _pendingCapture.value = null
          _toastMessage.value = "'$safeName' saved"
          onSuccess?.invoke()
        }
      } else {
        withContext(Dispatchers.Main) {
          _toastMessage.value = "Failed to save photo"
          _pendingCapture.value = null
        }
      }
    }
  }

  fun deleteItem(item: ItemEntity) {
    val currentCat = _currentCategory.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      itemDao.deleteItem(item)
      FileManager.deleteItemPhoto(getApplication(), currentCat.folderName, item.imageFileName)
      withContext(Dispatchers.Main) {
        _toastMessage.value = "'${item.name}' deleted"
      }
    }
  }

  fun updateItem(item: ItemEntity, newName: String, notes: String, quantity: Int) {
    val currentCat = _currentCategory.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val trimmedName = newName.trim().ifBlank { item.name }
      var finalFileName = item.imageFileName

      // If user renamed item, also rename the underlying file to match the new item name
      if (trimmedName != item.name) {
        val oldFile = FileManager.getItemFile(getApplication(), currentCat.folderName, item.imageFileName)
        if (oldFile.exists()) {
          val safeNewBase = FileManager.sanitizeFileName(trimmedName)
          var candidateName = "$safeNewBase.jpg"
          var targetFile = FileManager.getItemFile(getApplication(), currentCat.folderName, candidateName)
          var counter = 2
          while (targetFile.exists() && targetFile.absolutePath != oldFile.absolutePath) {
            candidateName = "$safeNewBase ($counter).jpg"
            targetFile = FileManager.getItemFile(getApplication(), currentCat.folderName, candidateName)
            counter++
          }
          if (oldFile.renameTo(targetFile)) {
            finalFileName = candidateName
          }
        }
      }

      val updated = item.copy(
        name = trimmedName,
        imageFileName = finalFileName,
        relativeLocation = "${currentCat.folderName}/$finalFileName",
        notes = notes.trim(),
        quantity = if (quantity > 0) quantity else 1
      )
      itemDao.updateItem(updated)
      withContext(Dispatchers.Main) {
        _toastMessage.value = "'$trimmedName' updated"
      }
    }
  }

  fun prepareZipExport(category: CategoryEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      _zipExportState.value = ZipExportState(
        isExporting = true,
        categoryName = category.name
      )
      val items = itemDao.getItemsForCategorySync(category.id)
      val json = ZipExporter.generateCatalogJson(category, items)
      val zipResult = ZipExporter.createCatalogZip(getApplication(), category, items)

      withContext(Dispatchers.Main) {
        if (zipResult.isSuccess) {
          _zipExportState.value = ZipExportState(
            zipFile = zipResult.getOrNull(),
            jsonContent = json,
            categoryName = category.name,
            totalItems = items.size,
            isExporting = false
          )
        } else {
          _zipExportState.value = null
          _toastMessage.value = "Failed to prepare ZIP file"
        }
      }
    }
  }

  fun dismissZipExport() {
    _zipExportState.value = null
  }

  fun shareZipNow() {
    val state = _zipExportState.value ?: return
    val zip = state.zipFile ?: return
    ZipExporter.shareZipFile(
      context = getApplication(),
      zipFile = zip,
      categoryName = state.categoryName,
      totalItems = state.totalItems
    )
  }

  fun clearToast() {
    _toastMessage.value = null
  }
}
