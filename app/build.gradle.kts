plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.about.libraries.android)
}

android {
    namespace = "com.nkot117.smartgo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nkot117.smartgo"
        minSdk = 28
        targetSdk = 36
        versionCode = 5
        versionName = "0.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("internal") {
            val storeFilePath =
                System.getenv("SIGNING_STORE_FILE") ?: "keystore/smartgo-internal.jks"

            storeFile = rootProject.file(storeFilePath)
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }

        create("release") {
            storeFile = rootProject.file(
                System.getenv("RELEASE_STORE_FILE") ?: "keystore/smartgo-release.jks"
            )
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "SmartGo (Debug)")
            isDebuggable = true
        }

        create("internal") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            resValue("string", "app_name", "SmartGo (Internal)")
            applicationIdSuffix = ".internal"
            versionNameSuffix = "-internal"
            signingConfig = signingConfigs.getByName("internal")

            isDebuggable = true
            isMinifyEnabled = false
        }

        release {
            resValue("string", "app_name", "SmartGo")
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

androidComponents {
    // CIから内部向けビルドのバージョンコードを設定する
    onVariants(selector().withBuildType("internal")) { variant ->
        val vc = project.findProperty("INTERNAL_VERSION_CODE") as String?
        if (vc != null) {
            variant.outputs.forEach { output ->
                output.versionCode.set(vc.toInt())
            }
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    implementation(project(":core:notification"))
    implementation(project(":feature:home"))
    implementation(project(":feature:checklist"))
    implementation(project(":feature:items"))
    implementation(project(":feature:done"))
    implementation(project(":feature:settings"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Navigation3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Icons
    implementation(libs.material.icons.core)
}
