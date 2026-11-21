package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.CategoryWithItemCount
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.HeroCardGradient
import com.example.util.FileManager
import com.example.viewmodel.CatalogViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
  viewModel: CatalogViewModel,
  onOpenCategory: (categoryId: Long) -> Unit,
) {
  val categories by viewModel.filteredCategories.collectAsState()
  val searchQuery by viewModel.categorySearchQuery.collectAsState()

  var showCreateDialog by remember { mutableStateOf(false) }
  var categoryToDelete by remember { mutableStateOf<CategoryWithItemCount?>(null) }
  var showInfoDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Gradient Brand Badge
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrandGradient),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Column {
              Text(
                text = "SnapCatalog",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Photo Catalog & ZIP Exporter",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        },
        actions = {
          IconButton(
            onClick = { showInfoDialog = true },
            modifier = Modifier.testTag("app_info_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "How it works",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showCreateDialog = true },
        icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
        text = { Text("New List", fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("create_list_fab")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Sleek Hero Dashboard Card
      item {
        HeroDashboardBanner(
          totalCategories = categories.size,
          totalItems = categories.sumOf { it.itemCount },
          onCreateClick = { showCreateDialog = true }
        )
      }

      // Modern Pill Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.categorySearchQuery.value = it },
          placeholder = { Text("Search items here") },
          leadingIcon = {
            Icon(
              Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
              IconButton(onClick = { viewModel.categorySearchQuery.value = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .testTag("category_search_input")
        )
      }

      // Section Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Your Catalogs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
          ) {
            Text(
              text = "${categories.size} lists",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      if (categories.isEmpty()) {
        item {
          EmptyCategoriesView(onNewCategoryClick = { showCreateDialog = true })
        }
      } else {
        items(categories, key = { it.id }) { cat ->
          CategoryCard(
            category = cat,
            onOpen = { onOpenCategory(cat.id) },
            onShareZip = {
              viewModel.selectCategory(cat.id)
              val entity = com.example.data.CategoryEntity(
                id = cat.id,
                name = cat.name,
                folderName = cat.folderName,
                colorHex = cat.colorHex,
                iconName = cat.iconName,
                description = cat.description,
                createdAt = cat.createdAt
              )
              viewModel.prepareZipExport(entity)
            },
            onDelete = { categoryToDelete = cat }
          )
        }
      }

      // Bottom spacing for FAB
      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }

  // Create Category Dialog
  if (showCreateDialog) {
    CreateCategoryDialog(
      onDismiss = { showCreateDialog = false },
      onCreate = { name, desc, colorHex ->
        viewModel.createCategory(name, desc, colorHex)
        showCreateDialog = false
      }
    )
  }

  // Delete Confirm Dialog
  if (categoryToDelete != null) {
    val target = categoryToDelete!!
    AlertDialog(
      onDismissRequest = { categoryToDelete = null },
      title = { Text("Delete List?") },
      text = {
        Text("Are you sure you want to delete '${target.name}' and all ${target.itemCount} photos inside it?")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteCategory(target)
            categoryToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { categoryToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Info Dialog
  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("How SnapCatalog Works", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          FeatureHelpRow(number = "1", title = "Create a List", subtitle = "Organize items into folders (e.g. 'Kitchen Items').")
          FeatureHelpRow(number = "2", title = "Snap Photos Rapidly", subtitle = "Tap 'Take Photo' to open camera. The keyboard pops up automatically to name the item.")
          FeatureHelpRow(number = "3", title = "Continuous Capture", subtitle = "Hit Enter/Done on the keyboard to save and immediately open the camera for the next item.")
          FeatureHelpRow(number = "4", title = "One-Click ZIP Share", subtitle = "Export all photos named after their items + data.json in a single ZIP file.")
        }
      },
      confirmButton = {
        Button(onClick = { showInfoDialog = false }) {
          Text("Got It")
        }
      }
    )
  }
}

@Composable
private fun FeatureHelpRow(number: String, title: String, subtitle: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )
    }
    Column {
      Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun HeroDashboardBanner(
  totalCategories: Int,
  totalItems: Int,
  onCreateClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    modifier = Modifier
      .fillMaxWidth()
      .shadow(8.dp, RoundedCornerShape(24.dp))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(HeroCardGradient)
        .padding(20.dp)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Catalog Vault",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Auto-naming • Continuous Camera • ZIP",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )
          }

          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = Color(0xFF2DD4BF),
              modifier = Modifier.size(26.dp)
            )
          }
        }

        // Metrics Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          MetricTile(
            title = "Total Lists",
            count = "$totalCategories",
            color = Color(0xFF2DD4BF),
            modifier = Modifier.weight(1f)
          )

          MetricTile(
            title = "Items Cataloged",
            count = "$totalItems",
            color = Color(0xFF60A5FA),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun MetricTile(
  title: String,
  count: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White.copy(alpha = 0.08f))
      .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFCBD5E1)
      )
      Text(
        text = count,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}

@Composable
private fun CategoryCard(
  category: CategoryWithItemCount,
  onOpen: () -> Unit,
  onShareZip: () -> Unit,
  onDelete: () -> Unit,
) {
  val context = LocalContext.current
  val parsedColor = remember(category.colorHex) {
    try {
      Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
      Color(0xFF0F766E)
    }
  }

  val latestFile = remember(category.latestImageFileName) {
    if (category.latestImageFileName != null) {
      FileManager.getItemFile(context, category.folderName, category.latestImageFileName)
    } else null
  }

  // Soft tinted background gradient using category accent color
  val cardBackground = Brush.horizontalGradient(
    colors = listOf(
      parsedColor.copy(alpha = 0.12f),
      parsedColor.copy(alpha = 0.05f)
    )
  )

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, parsedColor.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
      .clickable { onOpen() }
      .testTag("category_card_${category.id}")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(cardBackground)
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Thumbnail or Category Icon
        if (latestFile != null && latestFile.exists()) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(1.5.dp, parsedColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          ) {
            Image(
              painter = rememberAsyncImagePainter(model = latestFile),
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }
        } else {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(parsedColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = parsedColor,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Info Column
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = parsedColor.copy(alpha = 0.18f)
          ) {
            Text(
              text = "${category.itemCount} ${if (category.itemCount == 1) "item" else "items"}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = parsedColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Action Icons: Share Icon + Delete Icon
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Share icon button with soft rounded pill/circle
          IconButton(
            onClick = onShareZip,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(parsedColor.copy(alpha = 0.16f))
              .testTag("share_category_zip_icon_${category.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share ZIP",
              tint = parsedColor,
              modifier = Modifier.size(18.dp)
            )
          }

          // Delete icon button
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_category_icon_${category.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete list",
              tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyCategoriesView(onNewCategoryClick: () -> Unit) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.FolderOpen,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
          tint = MaterialTheme.colorScheme.primary
        )
      }
      Text(
        text = "No Catalogs Yet",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Create your first category list (e.g. Kitchen Items) to start cataloging photos.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Button(
        onClick = onNewCategoryClick,
        shape = RoundedCornerShape(14.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create First List")
      }
    }
  }
}

@Composable
fun CreateCategoryDialog(
  onDismiss: () -> Unit,
  onCreate: (name: String, description: String, colorHex: String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedColorHex by remember { mutableStateOf("#0F766E") }
  var isError by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    delay(150)
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val colorOptions = listOf(
    "#0F766E", "#2563EB", "#10B981", "#F59E0B", "#F43F5E", "#6366F1", "#8B5CF6"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BrandGradient),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "New Category List",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            if (it.isNotBlank()) isError = false
          },
          label = { Text("List Name") },
          placeholder = { Text("e.g. Kitchen Items, Warehouse Stock") },
          singleLine = true,
          isError = isError,
          shape = RoundedCornerShape(14.dp),
          supportingText = {
            if (isError) Text("Please enter a name") else Text("An internal folder with this name will be created")
          },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("new_list_name_input")
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (Optional)") },
          placeholder = { Text("e.g. Pots, pans, appliances, tools") },
          shape = RoundedCornerShape(14.dp),
          maxLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = "Accent Color:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          colorOptions.forEach { hex ->
            val color = Color(android.graphics.Color.parseColor(hex))
            val isSelected = selectedColorHex == hex
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { selectedColorHex = hex },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.trim().isBlank()) {
            isError = true
          } else {
            keyboardController?.hide()
            onCreate(name.trim(), description.trim(), selectedColorHex)
          }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_create_list_button")
      ) {
        Text("Create List", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Cancel")
      }
    }
  )
}
