package pl.Smoluch.barcodescannerhonda

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.Smoluch.barcodescannerhonda.ui.theme.BarcodeScannerHondaTheme
import pl.Smoluch.barcodescannerhonda.ui.screens.BarcodeListScreen
import pl.Smoluch.barcodescannerhonda.ui.screens.BarcodeScanner
import pl.Smoluch.barcodescannerhonda.ui.screens.CategorySettingsScreen
import pl.Smoluch.barcodescannerhonda.ui.screens.SplashScreen
import pl.Smoluch.barcodescannerhonda.utils.AppDataStore
import pl.Smoluch.barcodescannerhonda.data.Category
import pl.Smoluch.barcodescannerhonda.data.BarcodeCount

enum class Screen {
    Scanner,
    List,
    Settings
}

class MainActivity : ComponentActivity() {
    private lateinit var dataStore: AppDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = AppDataStore(this)

        setContent {
            BarcodeScannerHondaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    var isLoading by remember { mutableStateOf(true) }
                    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
                    var scannedBarcodes by remember { mutableStateOf<List<BarcodeCount>>(emptyList()) }
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Scanner) }
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    // Show splash screen for 1 second and load data
                    LaunchedEffect(Unit) {
                        try {
                            // Load data first
                            Log.d("MainActivity", "Loading data from DataStore...")
                            categories = dataStore.loadCategories().also {
                                Log.d("MainActivity", "Loaded categories: ${it.map { cat -> "${cat.name} (${cat.id})" }}")
                            }
                            scannedBarcodes = dataStore.loadBarcodes().also {
                                Log.d("MainActivity", "Loaded barcodes: ${it.size}")
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error loading data", e)
                            Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            // Wait for splash screen minimum time
                            delay(1000)
                            showSplash = false
                            isLoading = false
                        }
                    }

                    // Auto-save categories whenever they change
                    LaunchedEffect(categories) {
                        if (!isLoading && categories.isNotEmpty()) {
                            Log.d("MainActivity", "Auto-saving categories: ${categories.size}")
                            scope.launch {
                                try {
                                    dataStore.saveCategories(categories)
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error saving categories", e)
                                    Toast.makeText(context, "Error saving categories: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    // Auto-save barcodes whenever they change
                    LaunchedEffect(scannedBarcodes) {
                        if (!isLoading && scannedBarcodes.isNotEmpty()) {
                            Log.d("MainActivity", "Auto-saving barcodes: ${scannedBarcodes.size}")
                            scope.launch {
                                try {
                                    dataStore.saveBarcodes(scannedBarcodes)
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error saving barcodes", e)
                                    Toast.makeText(context, "Error saving barcodes: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    // Save data when activity is destroyed or paused
                    DisposableEffect(Unit) {
                        onDispose {
                            scope.launch {
                                Log.d("MainActivity", "Saving data on dispose...")
                                try {
                                    dataStore.saveCategories(categories)
                                    dataStore.saveBarcodes(scannedBarcodes)
                                    Log.d("MainActivity", "Data saved successfully on dispose")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error saving data on dispose", e)
                                }
                            }
                        }
                    }

                    if (showSplash) {
                        SplashScreen()
                    } else {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            when (currentScreen) {
                                Screen.Scanner -> {
                                    BarcodeScanner(
                                        categories = categories,
                                        onCategoryAdded = { newCategory: Category ->
                                            Log.d("MainActivity", "Adding category: ${newCategory.name} (${newCategory.id})")
                                            categories = (categories + newCategory).also {
                                                Log.d("MainActivity", "Updated categories: ${it.map { cat -> "${cat.name} (${cat.id})" }}")
                                            }
                                        },
                                        onNavigateToList = { updatedBarcodes: List<BarcodeCount> ->
                                            Log.d("MainActivity", "Navigating to list with ${updatedBarcodes.size} barcodes")
                                            scannedBarcodes = updatedBarcodes
                                            currentScreen = Screen.List
                                        },
                                        onNavigateToSettings = { currentBarcodes: List<BarcodeCount> ->
                                            Log.d("MainActivity", "Navigating to settings, preserving ${currentBarcodes.size} barcodes")
                                            scannedBarcodes = currentBarcodes
                                            currentScreen = Screen.Settings
                                        },
                                        initialBarcodes = scannedBarcodes
                                    )
                                }
                                Screen.List -> {
                                    BarcodeListScreen(
                                        barcodes = scannedBarcodes,
                                        categories = categories,
                                        onBackClick = {
                                            currentScreen = Screen.Scanner
                                        },
                                        onDeleteBarcode = { barcodeToDelete: BarcodeCount ->
                                            scannedBarcodes = scannedBarcodes.filter { it.id != barcodeToDelete.id }
                                        },
                                        onUpdateBarcodeCategory = { barcode: BarcodeCount, newCategoryId: String? ->
                                            scannedBarcodes = scannedBarcodes.map {
                                                if (it.id == barcode.id) it.copy(categoryId = newCategoryId) else it
                                            }
                                        },
                                        onUpdateBarcodeCount = { barcode: BarcodeCount, newCount: Int ->
                                            scannedBarcodes = scannedBarcodes.map {
                                                if (it.id == barcode.id) it.copy(count = newCount) else it
                                            }
                                        }
                                    )
                                }
                                Screen.Settings -> {
                                    CategorySettingsScreen(
                                        categories = categories,
                                        onCategoryUpdated = { updatedCategory: Category ->
                                            Log.d("MainActivity", "Updating category from settings: ${updatedCategory.name}")
                                            categories = categories.map { category -> 
                                                if (category.id == updatedCategory.id) updatedCategory else category 
                                            }
                                        },
                                        onCategoryDeleted = { categoryToDelete: Category ->
                                            Log.d("MainActivity", "Deleting category from settings: ${categoryToDelete.name}")
                                            categories = categories.filter { it.id != categoryToDelete.id }
                                            scannedBarcodes = scannedBarcodes.map { barcode ->
                                                if (barcode.categoryId == categoryToDelete.id) {
                                                    barcode.copy(categoryId = null)
                                                } else {
                                                    barcode
                                                }
                                            }
                                        },
                                        onNavigateBack = {
                                            currentScreen = Screen.Scanner
                                        },
                                        barcodes = scannedBarcodes
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