package pl.Smoluch.barcodescannerhonda.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.Smoluch.barcodescannerhonda.data.BarcodeCount
import pl.Smoluch.barcodescannerhonda.data.Category

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "barcode_scanner_data")

class AppDataStore(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .serializeNulls() // Important for categoryId which can be null
        .setPrettyPrinting() // Makes JSON more readable in logs
        .create()
    
    private val storageManager = StorageManager(context)

    companion object {
        private val BARCODES_KEY = stringPreferencesKey("barcodes")
        private val CATEGORIES_KEY = stringPreferencesKey("categories")
        private const val BARCODES_FILE = "barcodes.json"
        private const val CATEGORIES_FILE = "categories.json"
    }

    suspend fun saveBarcodes(barcodes: List<BarcodeCount>) {
        try {
            // Validate barcodes before saving
            val validBarcodes = barcodes.filter { barcode ->
                if (barcode.value.isBlank()) {
                    Log.w("AppDataStore", "Skipping barcode with empty value")
                    false
                } else {
                    true
                }
            }

            val json = gson.toJson(validBarcodes)
            Log.d("AppDataStore", "Saving barcodes: $json")
            
            // Save to both DataStore and file
            context.dataStore.edit { preferences ->
                preferences[BARCODES_KEY] = json
            }
            storageManager.saveDataFile(json, BARCODES_FILE)

            // Verify the save by immediately reading back
            val savedBarcodes = loadBarcodes()
            Log.d("AppDataStore", "Verified saved barcodes: ${savedBarcodes.size} items")
            if (savedBarcodes.size != validBarcodes.size) {
                Log.w("AppDataStore", "Barcode count mismatch after save: expected ${validBarcodes.size}, got ${savedBarcodes.size}")
            }

            // Verify category IDs
            val categories = loadCategories()
            val categoryIds = categories.map { it.id }.toSet()
            savedBarcodes.forEach { barcode ->
                if (barcode.categoryId != null && !categoryIds.contains(barcode.categoryId)) {
                    Log.w("AppDataStore", "Barcode ${barcode.value} references non-existent category ${barcode.categoryId}")
                }
            }
        } catch (e: Exception) {
            Log.e("AppDataStore", "Error saving barcodes", e)
            throw e
        }
    }

    suspend fun saveCategories(categories: List<Category>) {
        try {
            // Validate categories before saving
            val validCategories = categories.filter { category ->
                if (category.name.isBlank()) {
                    Log.w("AppDataStore", "Skipping category with empty name")
                    false
                } else {
                    true
                }
            }

            val json = gson.toJson(validCategories)
            Log.d("AppDataStore", "Saving categories: $json")
            
            // Save to both DataStore and file
            context.dataStore.edit { preferences ->
                preferences[CATEGORIES_KEY] = json
            }
            storageManager.saveDataFile(json, CATEGORIES_FILE)

            // Verify the save by immediately reading back
            val savedCategories = loadCategories()
            Log.d("AppDataStore", "Verified saved categories: ${savedCategories.map { "${it.name} (${it.id})" }}")
            if (savedCategories.size != validCategories.size) {
                Log.w("AppDataStore", "Category count mismatch after save: expected ${validCategories.size}, got ${savedCategories.size}")
            }

            // Update barcodes to handle deleted categories
            val barcodes = loadBarcodes()
            val categoryIds = savedCategories.map { it.id }.toSet()
            val updatedBarcodes = barcodes.map { barcode ->
                if (barcode.categoryId != null && !categoryIds.contains(barcode.categoryId)) {
                    barcode.copy(categoryId = null)
                } else {
                    barcode
                }
            }
            if (updatedBarcodes != barcodes) {
                Log.d("AppDataStore", "Updating barcodes to handle deleted categories")
                saveBarcodes(updatedBarcodes)
            }
        } catch (e: Exception) {
            Log.e("AppDataStore", "Error saving categories", e)
            throw e
        }
    }

    suspend fun loadBarcodes(): List<BarcodeCount> {
        val type = object : TypeToken<List<BarcodeCount>>() {}.type
        return try {
            // Try to load from file first, fall back to DataStore
            val json = storageManager.readDataFile(BARCODES_FILE) ?: context.dataStore.data
                .map { preferences -> preferences[BARCODES_KEY] ?: "[]" }
                .first()
            
            Log.d("AppDataStore", "Loading barcodes: $json")
            val barcodes = gson.fromJson<List<BarcodeCount>>(json, type)
            Log.d("AppDataStore", "Loaded ${barcodes.size} barcodes")
            barcodes.forEach { barcode ->
                Log.d("AppDataStore", "Loaded barcode: ${barcode.value}, Category: ${barcode.categoryId}, Count: ${barcode.count}")
            }
            barcodes
        } catch (e: Exception) {
            Log.e("AppDataStore", "Error loading barcodes", e)
            emptyList()
        }
    }

    suspend fun loadCategories(): List<Category> {
        val type = object : TypeToken<List<Category>>() {}.type
        return try {
            // Try to load from file first, fall back to DataStore
            val json = storageManager.readDataFile(CATEGORIES_FILE) ?: context.dataStore.data
                .map { preferences -> preferences[CATEGORIES_KEY] ?: "[]" }
                .first()
            
            Log.d("AppDataStore", "Loading categories: $json")
            val categories = gson.fromJson<List<Category>>(json, type)
            Log.d("AppDataStore", "Loaded categories: ${categories.map { "${it.name} (${it.id})" }}")
            categories
        } catch (e: Exception) {
            Log.e("AppDataStore", "Error loading categories", e)
            emptyList()
        }
    }

    suspend fun clearAll() {
        try {
            context.dataStore.edit { preferences ->
                preferences[BARCODES_KEY] = "[]"
                preferences[CATEGORIES_KEY] = "[]"
            }
            storageManager.saveDataFile("[]", BARCODES_FILE)
            storageManager.saveDataFile("[]", CATEGORIES_FILE)
            Log.d("AppDataStore", "Cleared all data")
        } catch (e: Exception) {
            Log.e("AppDataStore", "Error clearing data", e)
            throw e
        }
    }
} 