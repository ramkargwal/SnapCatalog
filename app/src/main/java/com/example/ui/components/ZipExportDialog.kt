package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CatalogViewModel

@Composable
fun ZipExportDialog(
  state: CatalogViewModel.ZipExportState,
  onDismiss: () -> Unit,
  onShare: () -> Unit,
) {
  val clipboardManager = LocalClipboardManager.current
  var selectedTab by remember { mutableIntStateOf(0) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Archive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
        Text(
          text = if (state.isExporting) "Preparing ZIP..." else "ZIP Archive Ready",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      if (state.isExporting) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
          Text(
            text = "Compressing photos and packaging data.json file...",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      } else {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = { Text("Archive Details", fontWeight = FontWeight.SemiBold) },
              icon = { Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = { Text("JSON Data", fontWeight = FontWeight.SemiBold) },
              icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
          }

          if (selectedTab == 0) {
            // ZIP details
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // File Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = state.zipFile?.name ?: "catalog.zip",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold
                    )
                    val fileSizeKb = (state.zipFile?.length() ?: 0L) / 1024
                    Text(
                      text = "Size: $fileSizeKb KB",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.outline
                    )
                  }
                }

                // Photos summary
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "${state.totalItems} Photos (Named after items)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                  )
                }

                // JSON Data summary
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "data.json (Item names & folder paths included)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                  )
                }

                // Destination folder indication
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "Folder: ${state.categoryName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }

            Text(
              text = "This ZIP archive can be shared directly via WhatsApp, Gmail, Google Drive, or any file sharing app.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            // JSON Code Preview
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "data.json File Content:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(state.jsonContent))
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 240.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF0F172A))
                  .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                  .padding(12.dp)
                  .verticalScroll(rememberScrollState())
              ) {
                Text(
                  text = state.jsonContent,
                  color = Color(0xFF38BDF8),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (!state.isExporting) {
        Button(
          onClick = onShare,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("share_zip_confirm_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Share ZIP", fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("close_zip_dialog_button")
      ) {
        Text("Close")
      }
    }
  )
}
