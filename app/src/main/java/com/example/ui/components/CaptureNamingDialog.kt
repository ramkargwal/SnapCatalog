package com.example.ui.components

import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun CaptureNamingDialog(
  tempFile: File?,
  galleryUri: Uri?,
  categoryName: String,
  onDismiss: () -> Unit,
  onSave: (name: String, notes: String, quantity: Int, openNextCamera: Boolean) -> Unit,
) {
  var itemName by remember { mutableStateOf("") }
  var itemNotes by remember { mutableStateOf("") }
  var quantity by remember { mutableIntStateOf(1) }
  var isError by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Automatically request focus and open soft keyboard when dialog appears
  LaunchedEffect(Unit) {
    delay(150)
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val imageModel = tempFile ?: galleryUri

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Name This Item",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Photo preview thumbnail with high-contrast frame
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          if (imageModel != null) {
            Image(
              painter = rememberAsyncImagePainter(model = imageModel),
              contentDescription = "Captured photo",
              modifier = Modifier.matchParentSize(),
              contentScale = ContentScale.Crop
            )
          }
        }

        // Folder badge
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Folder: $categoryName",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }

        // Item Name input (Automatically focused with keyboard open)
        OutlinedTextField(
          value = itemName,
          onValueChange = {
            itemName = it
            if (it.isNotBlank()) isError = false
          },
          label = { Text("Item Name") },
          placeholder = { Text("e.g. Utensil, Mug, Gas Stove, Pan") },
          singleLine = true,
          isError = isError,
          shape = RoundedCornerShape(14.dp),
          supportingText = {
            if (isError) {
              Text("Please enter an item name")
            } else {
              val displayName = if (itemName.isNotBlank()) itemName.trim() else "name"
              Text("File saved as: $displayName.jpg")
            }
          },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              if (itemName.trim().isBlank()) {
                isError = true
              } else {
                keyboardController?.hide()
                onSave(itemName.trim(), itemNotes.trim(), quantity, true)
              }
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("item_name_input")
        )

        // Notes / Extra details
        OutlinedTextField(
          value = itemNotes,
          onValueChange = { itemNotes = it },
          label = { Text("Notes / Description (Optional)") },
          placeholder = { Text("e.g. Stainless steel, 2-burner") },
          shape = RoundedCornerShape(14.dp),
          maxLines = 2,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("item_notes_input")
        )

        // Quantity selector pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Quantity / Count:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
              IconButton(
                onClick = { if (quantity > 1) quantity-- },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
              }
              Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp)
              )
              IconButton(
                onClick = { quantity++ },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FilledTonalButton(
          onClick = {
            if (itemName.trim().isBlank()) {
              isError = true
            } else {
              keyboardController?.hide()
              onSave(itemName.trim(), itemNotes.trim(), quantity, false)
            }
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("save_only_button")
        ) {
          Text("Save Only", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Button(
          onClick = {
            if (itemName.trim().isBlank()) {
              isError = true
            } else {
              keyboardController?.hide()
              onSave(itemName.trim(), itemNotes.trim(), quantity, true)
            }
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("save_item_button")
        ) {
          Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save & Next", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("cancel_capture_button")
      ) {
        Text("Cancel")
      }
    }
  )
}
