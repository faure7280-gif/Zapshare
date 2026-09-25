package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.ui.theme.*

@Composable
fun FileCategoryTabs(
    selectedCategory: FileCategory,
    onCategorySelected: (FileCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Triple(FileCategory.APPS, "Apps", Icons.Default.Android),
        Triple(FileCategory.PHOTOS, "Fotos", Icons.Default.Image),
        Triple(FileCategory.VIDEOS, "Videos", Icons.Default.Movie),
        Triple(FileCategory.MUSIC, "Música", Icons.Default.MusicNote),
        Triple(FileCategory.DOCUMENTS, "Documentos", Icons.Default.Description)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (cat, label, icon) ->
            val isSelected = (selectedCategory == cat)
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) DarkBackground else TextSecondary
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = DarkSurface,
                    labelColor = TextPrimary,
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = DarkBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = DarkCardBorder,
                    selectedBorderColor = NeonCyan
                ),
                modifier = Modifier.testTag("tab_${cat.name.lowercase()}")
            )
        }
    }
}
