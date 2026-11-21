package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.CategoryEntity
import com.example.data.ItemEntity
import com.example.ui.components.CaptureNamingDialog
import com.example.ui.components.ItemDetailDialog
import com.example.ui.theme.BrandGradient
import com.example.util.FileManager
import com.example.viewmodel.CatalogViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
  category: CategoryEntity,
  viewModel: CatalogViewModel,
  onBack: () -> Unit,
) {
  BackHandler { onBack() }

  val context = LocalContext.current
  val items by viewModel.currentItems.collectAsState()
  val itemSearchQuery by viewModel.itemSearchQuery.collectAsState()
  val pendingCapture by viewModel.pendingCapture.collectAsState()

  var selectedItemForDetail by remember { mutableStateOf<ItemEntity?>(null) }
  var isSearchVisible by remember { mutableStateOf(false) }

  // State to hold temporary camera capture file
  var currentTempCameraFile by remember { mutableStateOf<File?>(null) }

  // Camera Launcher
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success ->
    if (success && currentTempCameraFile != null) {
      viewModel.onCameraPhotoCaptured(currentTempCameraFile!!)
    }
  }

  // Gallery Picker Launcher
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.onGalleryPhotoPicked(uri)
    }
  }

  val launchCamera = {
    val (uri, file) = FileManager.createTempCameraUri(context)
    currentTempCameraFile = file
    cameraLauncher.launch(uri)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = category.name,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${items.size} ${if (items.size == 1) "item" else "items"} cataloged",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          // Search toggle
          IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = if (isSearchVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Share ZIP action button with vibrant styling
          Button(
            onClick = { viewModel.prepareZipExport(category) },
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier
              .padding(end = 8.dp)
              .testTag("share_category_zip_button")
          ) {
            Icon(
              Icons.Default.Archive,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share ZIP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Gallery Button
        FilledTonalIconButton(
          onClick = {
            galleryLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .size(54.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("pick_gallery_fab")
        ) {
          Icon(Icons.Default.PhotoLibrary, contentDescription = "Choose from gallery", modifier = Modifier.size(24.dp))
        }

        // Camera FAB (Primary continuous workflow)
        ExtendedFloatingActionButton(
          onClick = launchCamera,
          icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(22.dp)) },
          text = { Text("Take Photo", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = RoundedCornerShape(18.dp),
          elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp),
          modifier = Modifier.testTag("take_photo_fab")
        )
      }
    }
  ) { paddingValues ->
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 160.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // List description banner inside the list
      if (category.description.isNotBlank()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          FolderInfoBanner(
            category = category
          )
        }
      }

      // Search bar if toggled
      if (isSearchVisible) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          OutlinedTextField(
            value = itemSearchQuery,
            onValueChange = { viewModel.itemSearchQuery.value = it },
            placeholder = { Text("Filter items in this list...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
              if (itemSearchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.itemSearchQuery.value = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
              .fillMaxWidth()
              .shadow(2.dp, RoundedCornerShape(20.dp))
              .testTag("item_search_input")
          )
        }
      }

      if (items.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          EmptyItemsView(
            categoryName = category.name,
            onCameraClick = launchCamera,
            onGalleryClick = {
              galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
          )
        }
      } else {
        items(items, key = { it.id }) { itemEntity ->
          ItemCard(
            item = itemEntity,
            categoryFolderName = category.folderName,
            onClick = { selectedItemForDetail = itemEntity }
          )
        }
      }

      // Bottom clearance for FABs
      item(span = { GridItemSpan(maxLineSpan) }) {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }

  // Modal dialog when user captures a photo, prompting for the item name with auto-keyboard
  // After name is given, immediately reopens camera for the next photo!
  if (pendingCapture != null) {
    CaptureNamingDialog(
      tempFile = pendingCapture!!.tempFile,
      galleryUri = pendingCapture!!.galleryUri,
      categoryName = category.name,
      onDismiss = { viewModel.dismissPendingCapture() },
      onSave = { name, notes, quantity, openNextCamera ->
        viewModel.saveCapturedItem(name, notes, quantity) {
          if (openNextCamera) {
            // Automatically open camera for next photo
            launchCamera()
          }
        }
      }
    )
  }

  // Item Detail / Edit / Delete Dialog
  if (selectedItemForDetail != null) {
    val currentItem = selectedItemForDetail!!
    ItemDetailDialog(
      item = currentItem,
      categoryFolderName = category.folderName,
      onDismiss = { selectedItemForDetail = null },
      onUpdate = { newName, notes, quantity ->
        viewModel.updateItem(currentItem, newName, notes, quantity)
        selectedItemForDetail = null
      },
      onDelete = {
        viewModel.deleteItem(currentItem)
        selectedItemForDetail = null
      }
    )
  }
}

@Composable
private fun FolderInfoBanner(
  category: CategoryEntity,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
      Column {
        Text(
          text = "Description",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = category.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun ItemCard(
  item: ItemEntity,
  categoryFolderName: String,
  onClick: () -> Unit,
) {
  val context = LocalContext.current
  val photoFile = remember(item.imageFileName) {
    FileManager.getItemFile(context, categoryFolderName, item.imageFileName)
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag("item_card_${item.id}")
  ) {
    Column {
      // Photo container with dark bottom gradient for high contrast
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        if (photoFile.exists()) {
          Image(
            painter = rememberAsyncImagePainter(model = photoFile),
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(32.dp)
          )
        }

        // Quantity badge (top-right)
        if (item.quantity > 1) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
          ) {
            Text(
              text = "x${item.quantity}",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Details section
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Location / File name chip
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ) {
          Text(
            text = item.imageFileName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        if (item.notes.isNotBlank()) {
          Text(
            text = item.notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyItemsView(
  categoryName: String,
  onCameraClick: () -> Unit,
  onGalleryClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AddAPhoto,
          contentDescription = null,
          modifier = Modifier.size(36.dp),
          tint = MaterialTheme.colorScheme.primary
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "'$categoryName' is Empty",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Take a photo of any item. The keyboard will open to name it, and the camera will automatically open for the next photo.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
          onClick = onCameraClick,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("empty_state_camera_button")
        ) {
          Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Take Photo", fontWeight = FontWeight.Bold)
        }

        FilledTonalButton(
          onClick = onGalleryClick,
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("From Gallery")
        }
      }
    }
  }
}
