package pl.Smoluch.barcodescannerhonda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.Smoluch.barcodescannerhonda.data.Category
import pl.Smoluch.barcodescannerhonda.data.BarcodeCount
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySettingsScreen(
    categories: List<Category>,
    onCategoryUpdated: (Category) -> Unit,
    onCategoryDeleted: (Category) -> Unit,
    onNavigateBack: () -> Unit,
    barcodes: List<BarcodeCount> = emptyList()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var editedCategoryName by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<Category>()) }
    val context = LocalContext.current

    fun shareCategories(categoriesToShare: Set<Category>) {
        val shareText = buildString {
            categoriesToShare.forEach { category ->
                appendLine(category.name)
                
                // Get barcodes for this category
                val categoryBarcodes = barcodes.filter { it.categoryId == category.id }
                if (categoryBarcodes.isNotEmpty()) {
                    categoryBarcodes.forEach { barcode: BarcodeCount ->
                        val countText = if (barcode.count > 1) " x${barcode.count}" else ""
                        appendLine("${barcode.value}$countText")
                    }
                }
                appendLine() // Add empty line between categories
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        context.startActivity(Intent.createChooser(sendIntent, "Share Categories"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (selectedCategories.isNotEmpty()) {
                        IconButton(
                            onClick = { shareCategories(selectedCategories) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share selected categories"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { 
                                    selectedCategories = if (selectedCategories.contains(category)) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedCategories.contains(category)) {
                                        Icons.Default.CheckBox
                                    } else {
                                        Icons.Default.CheckBoxOutlineBlank
                                    },
                                    contentDescription = "Select category",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    selectedCategory = category
                                    editedCategoryName = category.name
                                    showEditDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit category",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedCategory = category
                                    showDeleteDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete category",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(
                                onClick = { shareCategories(setOf(category)) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share category",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Category") },
            text = {
                OutlinedTextField(
                    value = editedCategoryName,
                    onValueChange = { editedCategoryName = it },
                    label = { Text("Category Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedCategoryName.isNotBlank()) {
                            onCategoryUpdated(selectedCategory!!.copy(name = editedCategoryName))
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete category '${selectedCategory!!.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCategoryDeleted(selectedCategory!!)
                        selectedCategories = selectedCategories - selectedCategory!!
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 