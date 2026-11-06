@file:Suppress("UnstableApiUsage", "DEPRECATION")

import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.masum.cipher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.masum.cipher"
        minSdk = 26
        targetSdk = 37
        versionCode = 60
        versionName = "6.3.0-rc06"
        androidResources {
            localeFilters += listOf("en", "hi", "bn", "es", "fr", "de", "ja", "pl")
        }

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appName"] = "@string/app_name"
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val properties = Properties()
            FileInputStream(keystorePropertiesFile).use { properties.load(it) }
            create("releaseConfig") {
                storeFile = file(properties.getProperty("storeFile"))
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            val releaseSigning = signingConfigs.findByName("releaseConfig")
            if (releaseSigning != null) {
                signingConfig = releaseSigning
            } else {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-DEBUG"
            }
            isDebuggable = true
            manifestPlaceholders["appName"] = if (releaseSigning != null) "@string/app_name" else "Cipher (Debug)"
        }
        release {
            val releaseSigning = signingConfigs.findByName("releaseConfig")
            if (releaseSigning != null) {
                signingConfig = releaseSigning
            }
            manifestPlaceholders["appName"] = "@string/app_name"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/**/LICENSE*",
                "META-INF/**/NOTICE*",
                "META-INF/**/*.txt",
                "META-INF/**/*.version",
                "META-INF/DEPENDENCIES",
                "**/*.kotlin_builtins",
                "**/*.kotlin_metadata"
            )
        }
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += setOf("NonObservableLocale", "UseOfNonLambdaOffsetOverload", "ModifierParameter", "UnusedAttribute", "InlinedApi", "ProtectedPermissions")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)

    implementation(libs.hilt.android)
    ksp(libs.kotlin.metadata.jvm)
    ksp(libs.hilt.compiler)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.sqlcipher)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)
    
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    implementation(libs.lucide.icons)
    
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.profileinstaller)

    baselineProfile(project(":baselineprofile"))
    
    implementation(libs.play.app.update)
    implementation(libs.play.billing)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
