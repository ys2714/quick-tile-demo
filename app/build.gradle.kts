import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing credentials are kept out of version control in keystore.properties.
val keystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        load(propertiesFile.inputStream())
    }
}

android {
    namespace = "com.example.quicktile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quicktile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Builds the signed release APK, renames it with the current git tag, and
// copies it to the project root so it's easy to find and distribute.
tasks.register("releaseApk") {
    group = "release"
    description = "Builds, signs, renames and copies the release APK to the project root"
    dependsOn("assembleRelease")

    doLast {
        val tag = providers.exec {
            commandLine("git", "describe", "--tags", "--always")
        }.standardOutput.asText.get().trim()

        val signedApk = layout.buildDirectory
            .file("outputs/apk/release/app-release.apk")
            .get()
            .asFile

        check(signedApk.exists()) {
            "Signed release APK not found at ${signedApk.absolutePath}. " +
                "Make sure keystore.properties points to a valid release key."
        }

        val destination = rootProject.layout.projectDirectory
            .file("quick-tile-demo-$tag.apk")
            .asFile

        signedApk.copyTo(destination, overwrite = true)
        println("Release APK copied to: ${destination.absolutePath}")
    }
}
