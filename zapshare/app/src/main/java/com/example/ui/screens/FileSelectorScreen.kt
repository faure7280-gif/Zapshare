package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.TransferItem
import com.example.ui.components.FileCategoryTabs
import com.example.ui.components.FileItemRow
import com.example.ui.theme.*

@Composable
fun FileSelectorScreen(
    selectedCategory: FileCategory,
    availableFiles: List<FileItem>,
    isLoadingFiles: Boolean,
    selectedFiles: Map<Uri, FileItem>,
    onCategorySelected: (FileCategory) -> Unit,
    onToggleSelectFile: (FileItem) -> Unit,
    onFilesPicked: (List<Uri>) -> Unit,
    onSendFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onFilesPicked(uris)
        }
    }

    val selectedCount = selectedFiles.size
    val totalSelectedBytes = selectedFiles.values.sumOf { it.size }
    val formattedTotalSize = TransferItem.formatBytes(totalSelectedBytes)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Category Tabs
            FileCategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )

            // Subheader with SAF picker button and count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${availableFiles.size} archivos encontrados",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Pick from storage
                OutlinedButton(
                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(NeonCyan.copy(0.6f), ElectricViolet.copy(0.6f)))
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("pick_from_storage_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Explorador libre", fontSize = 12.sp)
                }
            }

            // Files List
            if (isLoadingFiles) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else if (availableFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron archivos en esta categoría",
                        color = TextTertiary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = if (selectedCount > 0) 108.dp else 84.dp)
                ) {
                    items(availableFiles, key = { it.uri.toString() }) { file ->
                        val isSelected = selectedFiles.containsKey(file.uri)
                        FileItemRow(
                            item = file,
                            isSelected = isSelected,
                            onToggleSelect = { onToggleSelectFile(file) }
                        )
                    }
                }
            }
        }

        // Floating Bottom Send Bar if files are selected (Acrylic Glassmorphism)
        if (selectedCount > 0) {
            AcrylicCard(
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xDD111827),
                borderBrush = Brush.linearGradient(
                    listOf(NeonCyan, Color.White.copy(0.4f), ElectricViolet)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("send_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$selectedCount archivos seleccionados",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "$formattedTotalSize • Envío dual simultáneo",
                            fontSize = 12.sp,
                            color = SpeedEmerald
                        )
                    }

                    Button(
                        onClick = onSendFiles,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("send_selected_files_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ENVIAR AHORA",
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkBackground,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
