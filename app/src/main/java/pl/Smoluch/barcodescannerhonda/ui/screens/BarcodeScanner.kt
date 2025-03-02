package pl.Smoluch.barcodescannerhonda.ui.screens

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.Camera
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import pl.Smoluch.barcodescannerhonda.data.BarcodeCount
import pl.Smoluch.barcodescannerhonda.data.Category

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            
            // Scanner window size (60% of the smaller dimension)
            val scannerSize = minOf(width, height) * 0.6f
            val left = (width - scannerSize) / 2
            val top = (height - scannerSize) / 2
            
            // Draw semi-transparent overlay for the entire screen except the scanning area
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(0f, 0f),
                size = Size(width, top) // Top section
            )
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(0f, top + scannerSize),
                size = Size(width, height - (top + scannerSize)) // Bottom section
            )
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(0f, top),
                size = Size(left, scannerSize) // Left section
            )
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(left + scannerSize, top),
                size = Size(width - (left + scannerSize), scannerSize) // Right section
            )
            
            // Draw scanner window border with a darker color
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            drawRect(
                color = Color(0xFF2196F3),  // Material Blue color
                topLeft = Offset(left, top),
                size = Size(scannerSize, scannerSize),
                style = Stroke(width = 4f, pathEffect = dashPathEffect)
            )
            
            // Draw corner markers
            val cornerLength = scannerSize * 0.1f
            val strokeWidth = 6f
            
            // Top-left corner
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left, top),
                end = Offset(left + cornerLength, top),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left, top),
                end = Offset(left, top + cornerLength),
                strokeWidth = strokeWidth
            )
            
            // Top-right corner
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left + scannerSize, top),
                end = Offset(left + scannerSize - cornerLength, top),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left + scannerSize, top),
                end = Offset(left + scannerSize, top + cornerLength),
                strokeWidth = strokeWidth
            )
            
            // Bottom-left corner
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left, top + scannerSize),
                end = Offset(left + cornerLength, top + scannerSize),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left, top + scannerSize),
                end = Offset(left, top + scannerSize - cornerLength),
                strokeWidth = strokeWidth
            )
            
            // Bottom-right corner
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left + scannerSize, top + scannerSize),
                end = Offset(left + scannerSize - cornerLength, top + scannerSize),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(left + scannerSize, top + scannerSize),
                end = Offset(left + scannerSize, top + scannerSize - cornerLength),
                strokeWidth = strokeWidth
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    onAddCategory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = category.id == selectedCategoryId,
                            onClick = { 
                                Log.d("CategorySelector", "Selected category: ${category.name} (${category.id})")
                                onCategorySelected(if (category.id == selectedCategoryId) null else category.id)
                            },
                            label = { 
                                Text(
                                    text = category.name,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
            
            // Add category button
            IconButton(
                onClick = onAddCategory,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add category",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = "Category settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScanner(
    categories: List<Category>,
    onCategoryAdded: (Category) -> Unit,
    onNavigateToList: (List<BarcodeCount>) -> Unit,
    onNavigateToSettings: (List<BarcodeCount>) -> Unit,
    initialBarcodes: List<BarcodeCount> = emptyList()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var scannedBarcodes by remember { mutableStateOf(initialBarcodes) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val toneGen = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    var isScanning by remember { mutableStateOf(true) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var cameraExecutor by remember { mutableStateOf<ExecutorService?>(null) }
    var hasFlash by remember { mutableStateOf(false) }
    var lastScannedBarcode by remember { mutableStateOf<String?>(null) }
    var showPopup by remember { mutableStateOf(false) }

    // Monitor categories for changes
    LaunchedEffect(categories) {
        Log.d("BarcodeScanner", "Categories updated: ${categories.map { "${it.name} (${it.id})" }}")
        
        // If the selected category was deleted, reset the selection
        if (selectedCategoryId != null && !categories.any { it.id == selectedCategoryId }) {
            Log.d("BarcodeScanner", "Selected category no longer exists, resetting selection")
            selectedCategoryId = null
        }

        // Update barcodes if any of their categories were deleted
        val updatedBarcodes = scannedBarcodes.map { barcode ->
            if (barcode.categoryId != null && !categories.any { it.id == barcode.categoryId }) {
                Log.d("BarcodeScanner", "Resetting category for barcode: ${barcode.value}")
                barcode.copy(categoryId = null)
            } else {
                barcode
            }
        }
        if (updatedBarcodes != scannedBarcodes) {
            Log.d("BarcodeScanner", "Updating barcodes after category changes")
            scannedBarcodes = updatedBarcodes
        }
    }

    // Monitor scanned barcodes for changes
    LaunchedEffect(scannedBarcodes) {
        Log.d("BarcodeScanner", "Scanned barcodes updated: ${scannedBarcodes.size} items")
        scannedBarcodes.forEach { barcode ->
            Log.d("BarcodeScanner", "Barcode: ${barcode.value}, Category: ${barcode.categoryId}, Count: ${barcode.count}")
        }
    }

    DisposableEffect(Unit) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        onDispose {
            toneGen.release()
            cameraExecutor?.shutdown()
            camera?.cameraControl?.enableTorch(false)
        }
    }

    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            val newCategory = Category(name = newCategoryName)
                            Log.d("BarcodeScanner", "Adding new category: ${newCategory.name} (${newCategory.id})")
                            onCategoryAdded(newCategory)
                            selectedCategoryId = newCategory.id // Automatically select the new category
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CategorySelector(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { newCategoryId ->
                    Log.d("BarcodeScanner", "Category selected: ${newCategoryId ?: "none"}")
                    selectedCategoryId = newCategoryId
                },
                onAddCategory = { showAddCategoryDialog = true },
                onOpenSettings = { onNavigateToSettings(scannedBarcodes) }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { 
                            Log.d("BarcodeScanner", "Navigating to list with ${scannedBarcodes.size} barcodes")
                            onNavigateToList(scannedBarcodes) 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Scanned List")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!cameraPermissionState.status.isGranted) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Camera permission is required to use the barcode scanner")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        AndroidView(
                            factory = { context ->
                                PreviewView(context).apply {
                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build()
                                    preview.setSurfaceProvider(previewView.surfaceProvider)

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    val barcodeScanner = BarcodeScanning.getClient()

                                    imageAnalysis.setAnalyzer(cameraExecutor!!) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null && isScanning) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )

                                            barcodeScanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        when (barcode.valueType) {
                                                            Barcode.TYPE_TEXT,
                                                            Barcode.TYPE_ISBN,
                                                            Barcode.TYPE_PRODUCT -> {
                                                                barcode.rawValue?.let { value ->
                                                                    if (isScanning) {
                                                                        isScanning = false
                                                                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                                                        scannedBarcodes = updateBarcodeList(scannedBarcodes, value, selectedCategoryId)
                                                                        lastScannedBarcode = value
                                                                        showPopup = true
                                                                        coroutineScope.launch {
                                                                            delay(200)
                                                                            isScanning = true
                                                                            delay(2000)
                                                                            showPopup = false
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("BarcodeScanner", "Barcode scanning failed", e)
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                        hasFlash = camera?.cameraInfo?.hasFlashUnit() ?: false
                                    } catch (e: Exception) {
                                        Log.e("BarcodeScanner", "Camera binding failed", e)
                                        Toast.makeText(
                                            context,
                                            "Failed to initialize camera: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("BarcodeScanner", "Camera provider failed", e)
                                    Toast.makeText(
                                        context,
                                        "Failed to initialize camera: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }
                        
                        // Add the scanner overlay
                        ScannerOverlay(modifier = Modifier.zIndex(2f))
                        
                        // Flashlight toggle button
                        if (hasFlash) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .zIndex(3f),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                IconButton(
                                    onClick = {
                                        camera?.let { cam ->
                                            isFlashlightOn = !isFlashlightOn
                                            cam.cameraControl.enableTorch(isFlashlightOn)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                ) {
                                    Icon(
                                        imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = if (isFlashlightOn) "Turn off flashlight" else "Turn on flashlight",
                                        tint = if (isFlashlightOn) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Popup for scanned barcode
                        if (showPopup && lastScannedBarcode != null) {
                            Card(
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(0.8f)
                                    .align(Alignment.Center)
                                    .zIndex(4f)
                                    .animateContentSize(),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Scanned Barcode",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = lastScannedBarcode ?: "",
                                        style = MaterialTheme.typography.bodyLarge
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

private fun updateBarcodeList(
    currentList: List<BarcodeCount>,
    newBarcode: String,
    categoryId: String?
): List<BarcodeCount> {
    val mutableList = currentList.toMutableList()
    
    Log.d("BarcodeScanner", "Updating barcode list - New barcode: $newBarcode, Category ID: $categoryId")
    Log.d("BarcodeScanner", "Current list size: ${currentList.size}")
    
    // First, check if the exact same barcode exists (same value and category)
    val exactMatch = mutableList.find { 
        it.value == newBarcode && it.categoryId == categoryId 
    }
    
    // Then check if the barcode exists with a different category
    val differentCategoryMatch = if (exactMatch == null) {
        mutableList.find { it.value == newBarcode }
    } else null
    
    when {
        exactMatch != null -> {
            // Update existing barcode count
            val index = mutableList.indexOf(exactMatch)
            mutableList[index] = exactMatch.copy(count = exactMatch.count + 1)
            Log.d("BarcodeScanner", "Updated existing barcode count: ${exactMatch.value} (${exactMatch.categoryId}) -> ${exactMatch.count + 1}")
        }
        differentCategoryMatch != null -> {
            // Create new entry with new category but keep the old one
            mutableList.add(BarcodeCount(
                value = newBarcode,
                categoryId = categoryId,
                count = 1
            ))
            Log.d("BarcodeScanner", "Added new category entry for existing barcode: $newBarcode (${categoryId})")
        }
        else -> {
            // Add completely new barcode
            mutableList.add(BarcodeCount(
                value = newBarcode,
                categoryId = categoryId,
                count = 1
            ))
            Log.d("BarcodeScanner", "Added new barcode: $newBarcode (${categoryId})")
        }
    }
    
    Log.d("BarcodeScanner", "Updated list size: ${mutableList.size}")
    return mutableList
} 