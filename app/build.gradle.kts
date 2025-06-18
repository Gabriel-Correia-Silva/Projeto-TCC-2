plugins {

    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.android)

    id("org.jetbrains.kotlin.plugin.compose")

    id("com.google.devtools.ksp")

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.projeto_ttc2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projeto_ttc2"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {

        kotlinCompilerExtensionVersion = "1.1.1"
    }
}

dependencies {
    // Play Services Auth
    implementation(libs.play.services.auth.v2050)

    // Firebase Auth via BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)

    implementation (libs.google.firebase.auth)
    implementation (libs.firebase.firestore)

    // AndroidX Browser (Custom Tabs)
    implementation(libs.androidx.browser)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt for DI
    implementation(libs.hilt.android.v251)
    implementation(libs.support.annotations)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.firebase.crashlytics.buildtools)
    ksp(libs.hilt.compiler)


    // Room persistence
    implementation(libs.androidx.room.runtime.v271)
    implementation(libs.androidx.room.ktx.v271)
    ksp(libs.androidx.room.compiler)

    // Charts library for Compose (stable)
    implementation(libs.charts.android)

    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)


    implementation(platform(libs.androidx.compose.bom.v20250400))
    implementation(libs.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.ui.tooling.preview)



    // Compose tooling (debug only)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Unit tests
    testImplementation(libs.junit)

    // Instrumented Android tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core.v360)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)

    // Health connect
    implementation (libs.androidx.connect.client)


    // Coil for image loading
    implementation ("io.coil-kt:coil-compose:2.4.0")

    implementation ("androidx.compose.runtime:runtime:1.5.4")
    implementation ("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation ("androidx.health.connect:connect-client:1.1.0-rc02")

    implementation("androidx.compose.material3:material3:1.4.0-alpha15")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha15")
}


configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}
