package pl.Smoluch.barcodescannerhonda.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.Smoluch.barcodescannerhonda.data.BarcodeCount
import pl.Smoluch.barcodescannerhonda.data.Category
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeListScreen(
    barcodes: List<BarcodeCount>,
    categories: List<Category>,
    onBackClick: () -> Unit,
    onDeleteBarcode: (BarcodeCount) -> Unit,
    onUpdateBarcodeCategory: (BarcodeCount, String?) -> Unit,
    onUpdateBarcodeCount: (BarcodeCount, Int) -> Unit
) {
    val context = LocalContext.current
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Scanned Barcodes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { shareBarcodesText(context, barcodes, categories) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share barcodes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { saveBarcodeList(context, barcodes, categories) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text("Save to File")
                    }
                    Button(
                        onClick = { copyToClipboard(context, barcodes, categories) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text("Copy to Clipboard")
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Category filter
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = if (selectedCategoryId == category.id) null else category.id },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    val filteredBarcodes = if (selectedCategoryId != null) {
                        barcodes.filter { it.categoryId == selectedCategoryId }
                    } else {
                        barcodes
                    }

                    items(filteredBarcodes) { barcodeCount ->
                        var isExpanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = barcodeCount.value,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = categories.find { it.id == barcodeCount.categoryId }?.name ?: "No Category",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { 
                                                if (barcodeCount.count > 1) {
                                                    onUpdateBarcodeCount(barcodeCount, barcodeCount.count - 1)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease quantity",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = barcodeCount.count.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        IconButton(
                                            onClick = { 
                                                onUpdateBarcodeCount(barcodeCount, barcodeCount.count + 1)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase quantity",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteBarcode(barcodeCount) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete barcode",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Category dropdown
                                ExposedDropdownMenuBox(
                                    expanded = isExpanded,
                                    onExpandedChange = { isExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = categories.find { it.id == barcodeCount.categoryId }?.name ?: "No Category",
                                        onValueChange = { },
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        label = { Text("Category") },
                                        leadingIcon = { 
                                            Icon(
                                                imageVector = Icons.Outlined.Category,
                                                contentDescription = "Category",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    
                                    DropdownMenu(
                                        expanded = isExpanded,
                                        onDismissRequest = { isExpanded = false },
                                        modifier = Modifier.exposedDropdownSize()
                                    ) {
                                        categories.forEach { category ->
                                            DropdownMenuItem(
                                                text = { Text(category.name) },
                                                onClick = { 
                                                    onUpdateBarcodeCategory(barcodeCount, category.id)
                                                    isExpanded = false
                                                },
                                                colors = MenuDefaults.itemColors(
                                                    textColor = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun saveBarcodeList(context: Context, barcodes: List<BarcodeCount>, categories: List<Category>) {
    try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "barcodes_$timestamp.txt"
        val file = File(context.getExternalFilesDir(null), filename)
        
        FileOutputStream(file).use { fos ->
            barcodes.forEach { barcodeCount ->
                val category = categories.find { it.id == barcodeCount.categoryId }?.name ?: "No Category"
                val countText = if (barcodeCount.count > 1) " x${barcodeCount.count}" else ""
                val line = "${barcodeCount.value}${countText} (${category})\n"
                fos.write(line.toByteArray())
            }
        }
        
        Toast.makeText(
            context,
            "Saved to ${file.absolutePath}",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error saving file: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun copyToClipboard(context: Context, barcodes: List<BarcodeCount>, categories: List<Category>) {
    try {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = generateBarcodeText(barcodes, categories)
        val clip = ClipData.newPlainText("Barcodes", text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error copying to clipboard: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareBarcodesText(context: Context, barcodes: List<BarcodeCount>, categories: List<Category>) {
    try {
        val text = generateBarcodeText(barcodes, categories)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share barcodes")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing barcodes: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun generateBarcodeText(barcodes: List<BarcodeCount>, categories: List<Category>): String {
    return buildString {
        // Group barcodes by category
        val groupedBarcodes = barcodes.groupBy { barcode ->
            categories.find { it.id == barcode.categoryId }?.name ?: "No Category"
        }
        
        // For each category, list all its barcodes
        groupedBarcodes.forEach { (categoryName, categoryBarcodes) ->
            // Print category name
            appendLine(categoryName)
            
            // Print each barcode in the category
            categoryBarcodes.forEach { barcode ->
                val countText = if (barcode.count > 1) " x${barcode.count}" else ""
                appendLine("${barcode.value}$countText")
            }
            appendLine() // Add empty line between categories
        }
    }
} 