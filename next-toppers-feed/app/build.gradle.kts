import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(
        FileInputStream(keystorePropertiesFile)
    )
}

android {

    namespace = "com.nexttoppers.feed"
    compileSdk = 35

    defaultConfig {

        applicationId = "com.nexttoppers.feed"

        minSdk = 26
        targetSdk = 35

        versionCode = 12
        versionName = "2.0.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "APP_NAME",
            "\"Next Toppers Feed\""
        )

        buildConfigField(
            "String",
            "APP_VERSION",
            "\"2.0.0\""
        )

        buildConfigField(
            "String",
            "FIREBASE_PROJECT",
            "\"aarambh26-27\""
        )

        buildConfigField(
            "String",
            "SUPPORT_EMAIL",
            "\"support@nexttoppers.in\""
        )

        buildConfigField(
            "boolean",
            "ENABLE_CRASH_LOGGING",
            "true"
        )
    }

    signingConfigs {

        create("release") {

            storeFile =
                rootProject.file("../nexttoppers-release.jks")

            storePassword =
                keystoreProperties["storePassword"] as String

            keyAlias =
                keystoreProperties["keyAlias"] as String

            keyPassword =
                keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {

        getByName("release") {

            signingConfig =
                signingConfigs.getByName("release")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            buildConfigField(
                "boolean",
                "IS_DEBUG",
                "false"
            )

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }

        getByName("debug") {

            signingConfig =
                signingConfigs.getByName("release")

            isDebuggable = true

            versionNameSuffix = "-debug"

            buildConfigField(
                "boolean",
                "IS_DEBUG",
                "true"
            )
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    kotlinOptions {

        jvmTarget = "17"

        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {

        compose = true
        buildConfig = true
    }

    bundle {

        language {
            enableSplit = true
        }

        density {
            enableSplit = true
        }

        abi {
            enableSplit = true
        }
    }

    packaging {

        resources {

            excludes +=
                "/META-INF/{AL2.0,LGPL2.1}"

            excludes +=
                "/META-INF/DEPENDENCIES"

            excludes +=
                "DebugProbesKt.bin"
        }
    }

    lint {

        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )

    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.splash.screen
    )

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(libs.androidx.ui)

    implementation(
        libs.androidx.ui.graphics
    )

    implementation(
        libs.androidx.ui.tooling.preview
    )

    implementation(
        libs.androidx.material3
    )

    implementation(
        libs.androidx.material.icons.extended
    )

    implementation(
        libs.androidx.navigation.compose
    )

    debugImplementation(
        libs.androidx.ui.tooling
    )

    implementation(libs.hilt.android)

    kapt(
        libs.hilt.android.compiler
    )

    implementation(
        libs.hilt.navigation.compose
    )

    implementation(
        platform(libs.firebase.bom)
    )

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    implementation(
        libs.google.play.services.auth
    )

    implementation(
        libs.credentials.manager
    )

    implementation(
        libs.credentials.play.services.auth
    )

    implementation(
        libs.google.identity
    )

    implementation(
        libs.datastore.preferences
    )

    implementation(
        libs.coil.compose
    )

    implementation(
        libs.lottie.compose
    )

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    implementation(
        libs.kotlinx.coroutines.android
    )

    implementation(
        libs.kotlinx.coroutines.play.services
    )
}

kapt {
    correctErrorTypes = true
}
