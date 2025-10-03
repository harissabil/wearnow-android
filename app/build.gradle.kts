plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "id.harissabil.wearnow"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.harissabil.wearnow"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Support for Java 8 features
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Desugar for Java APIs
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Icon Extended
    implementation(libs.androidx.material.icons.extended)

    // Coil
    implementation(libs.coil.compose)

    // Navigation Compose
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.camera.view)

    // Exif Interface
    implementation(libs.androidx.exifinterface)

    // AWS Amplify dependencies
    implementation("com.amplifyframework.ui:authenticator:1.7.0")
    implementation("com.amplifyframework:aws-api:2.30.1")
    implementation("com.amplifyframework:aws-storage-s3:2.30.1")
    implementation("com.amplifyframework:aws-auth-cognito:2.30.1")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}