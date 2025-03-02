pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            // Versions
            version("agp", "8.2.2")
            version("kotlin", "1.9.22")
            version("composeCompiler", "1.5.8")
            version("composeBom", "2024.02.00")
            version("camerax", "1.3.1")
            version("accompanist", "0.32.0")
            version("datastore", "1.0.0")
            version("gson", "2.10.1")
            version("mlkitBarcode", "17.2.0")
            version("coreKtx", "1.12.0")
            version("lifecycle", "2.7.0")
            version("activityCompose", "1.8.2")

            // Plugins
            plugin("android-application", "com.android.application").versionRef("agp")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").versionRef("kotlin")

            // Dependencies
            library("core-ktx", "androidx.core", "core-ktx").versionRef("coreKtx")
            library("lifecycle-runtime-ktx", "androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("lifecycle")
            library("activity-compose", "androidx.activity", "activity-compose").versionRef("activityCompose")
            
            // Compose BOM
            library("compose-bom", "androidx.compose", "compose-bom").versionRef("composeBom")
            
            // Compose (using BOM, so no version needed)
            library("ui", "androidx.compose.ui", "ui").withoutVersion()
            library("ui-graphics", "androidx.compose.ui", "ui-graphics").withoutVersion()
            library("ui-tooling", "androidx.compose.ui", "ui-tooling").withoutVersion()
            library("ui-tooling-preview", "androidx.compose.ui", "ui-tooling-preview").withoutVersion()
            library("ui-test-manifest", "androidx.compose.ui", "ui-test-manifest").withoutVersion()
            library("ui-test-junit4", "androidx.compose.ui", "ui-test-junit4").withoutVersion()
            library("material3", "androidx.compose.material3", "material3").withoutVersion()
            library("material-icons-core", "androidx.compose.material", "material-icons-core").withoutVersion()
            library("material-icons-extended", "androidx.compose.material", "material-icons-extended").withoutVersion()

            // CameraX
            library("camera-camera2", "androidx.camera", "camera-camera2").versionRef("camerax")
            library("camera-lifecycle", "androidx.camera", "camera-lifecycle").versionRef("camerax")
            library("camera-view", "androidx.camera", "camera-view").versionRef("camerax")

            // DataStore
            library("datastore-preferences", "androidx.datastore", "datastore-preferences").versionRef("datastore")

            // ML Kit
            library("mlkit-barcode-scanning", "com.google.mlkit", "barcode-scanning").versionRef("mlkitBarcode")

            // Accompanist
            library("accompanist-permissions", "com.google.accompanist", "accompanist-permissions").versionRef("accompanist")

            // Gson
            library("gson", "com.google.code.gson", "gson").versionRef("gson")

            // Testing
            library("junit", "junit", "junit").version("4.13.2")
            library("androidx-test-junit", "androidx.test.ext", "junit").version("1.1.5")
            library("androidx-test-espresso", "androidx.test.espresso", "espresso-core").version("3.5.1")
        }
    }
}

rootProject.name = "BarcodeScannerHonda"
include(":app")
 