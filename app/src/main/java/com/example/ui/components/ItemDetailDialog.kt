package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.ItemEntity
import com.example.util.FileManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ItemDetailDialog(
  item: ItemEntity,
  categoryFolderName: String,
  onDismiss: () -> Unit,
  onUpdate: (newName: String, notes: String, quantity: Int) -> Unit,
  onDelete: () -> Unit,
) {
  val context = LocalContext.current
  val photoFile = remember(item.imageFileName) {
    FileManager.getItemFile(context, categoryFolderName, item.imageFileName)
  }

  var isEditing by remember { mutableStateOf(false) }
  var editedName by remember { mutableStateOf(item.name) }
  var editedNotes by remember { mutableStateOf(item.notes) }
  var editedQuantity by remember { mutableIntStateOf(item.quantity) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val dateStr = remember(item.createdAt) {
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.createdAt))
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      shape = RoundedCornerShape(24.dp),
      title = { Text("Delete Item?", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to delete '${item.name}' and its photo?") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDelete()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showDeleteConfirm = false },
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Cancel")
        }
      }
    )
    return
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isEditing) "Edit Item" else item.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Row {
          IconButton(onClick = { isEditing = !isEditing }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
          }
          IconButton(onClick = { showDeleteConfirm = true }) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Photo preview
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          if (photoFile.exists()) {
            Image(
              painter = rememberAsyncImagePainter(model = photoFile),
              contentDescription = item.name,
              modifier = Modifier.matchParentSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            Text("Photo unavailable", style = MaterialTheme.typography.bodySmall)
          }
        }

        if (isEditing) {
          OutlinedTextField(
            value = editedName,
            onValueChange = { editedName = it },
            label = { Text("Item Name") },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = editedNotes,
            onValueChange = { editedNotes = it },
            label = { Text("Notes / Description") },
            shape = RoundedCornerShape(14.dp),
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        } else {
          // Metadata badges
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Folder,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Location: ${item.relativeLocation}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Text(
                text = "File: ${item.imageFileName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Added: $dateStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
              )
              if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Notes: ${item.notes}",
                  style = MaterialTheme.typography.bodySmall
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (isEditing) {
        Button(
          onClick = {
            onUpdate(editedName, editedNotes, editedQuantity)
            isEditing = false
          },
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save", fontWeight = FontWeight.Bold)
        }
      } else {
        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("OK")
        }
      }
    },
    dismissButton = {
      if (isEditing) {
        OutlinedButton(
          onClick = { isEditing = false },
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Cancel")
        }
      }
    }
  )
}
