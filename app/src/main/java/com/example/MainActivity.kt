package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ZipExportDialog
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.CategoryListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CatalogViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          SnapCatalogApp()
        }
      }
    }
  }
}

@Composable
fun SnapCatalogApp(
  viewModel: CatalogViewModel = viewModel(),
) {
  val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
  val currentCategory by viewModel.currentCategory.collectAsState()
  val zipExportState by viewModel.zipExportState.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearToast()
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { paddingValues ->
    Crossfade(
      targetState = selectedCategoryId,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      label = "screen_transition"
    ) { catId ->
      if (catId == null || currentCategory == null) {
        CategoryListScreen(
          viewModel = viewModel,
          onOpenCategory = { id -> viewModel.selectCategory(id) }
        )
      } else {
        CategoryDetailScreen(
          category = currentCategory!!,
          viewModel = viewModel,
          onBack = { viewModel.selectCategory(null) }
        )
      }
    }

    // ZIP export & share dialog
    if (zipExportState != null) {
      ZipExportDialog(
        state = zipExportState!!,
        onDismiss = { viewModel.dismissZipExport() },
        onShare = { viewModel.shareZipNow() }
      )
    }
  }
}
