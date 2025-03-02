package pl.Smoluch.barcodescannerhonda.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

class StorageManager(private val context: Context) {
    private val appFolder: File by lazy {
        File(context.getExternalFilesDir(null), "BarcodeScanner").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val dataFolder: File by lazy {
        File(appFolder, "data").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val exportFolder: File by lazy {
        File(appFolder, "exports").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun saveExportFile(content: String, prefix: String = "export"): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportFolder, "${prefix}_$timestamp.txt")
        
        FileOutputStream(file).use { fos ->
            fos.write(content.toByteArray())
        }
        
        Log.d("StorageManager", "Saved export file: ${file.absolutePath}")
        return file
    }

    fun saveDataFile(content: String, filename: String) {
        val file = File(dataFolder, filename)
        FileOutputStream(file).use { fos ->
            fos.write(content.toByteArray())
        }
        Log.d("StorageManager", "Saved data file: ${file.absolutePath}")
    }

    fun readDataFile(filename: String): String? {
        val file = File(dataFolder, filename)
        return if (file.exists()) {
            FileInputStream(file).use { fis ->
                String(fis.readBytes())
            }
        } else {
            null
        }
    }

    fun getExportFiles(): List<File> {
        return exportFolder.listFiles()?.toList() ?: emptyList()
    }

    fun deleteExportFile(filename: String): Boolean {
        val file = File(exportFolder, filename)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun clearExports() {
        exportFolder.listFiles()?.forEach { it.delete() }
        Log.d("StorageManager", "Cleared all exports")
    }

    fun getAppFolderPath(): String = appFolder.absolutePath
    fun getDataFolderPath(): String = dataFolder.absolutePath
    fun getExportFolderPath(): String = exportFolder.absolutePath
} 