// app/build.gradle.kts

plugins {
    // Plugin Android Application (via Version Catalog)
    alias(libs.plugins.android.application)
    // Plugin Kotlin Android (mantém a versão em libs.versions.toml)
    alias(libs.plugins.kotlin.android)
    // Plugin Compose Compiler Gradle – necessário em Kotlin ≥2.0
    id("org.jetbrains.kotlin.plugin.compose")
    // Kotlin Symbol Processing (KSP) – para Hilt, Room, etc.
    id("com.google.devtools.ksp")
    // Google Services (Firebase)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.projeto_ttc2"      // namespace da aplicação
    compileSdk = 35                              // API de compilação

    defaultConfig {
        applicationId = "com.example.projeto_ttc2"
        minSdk = 29                              // SDK mínimo suportado
        targetSdk = 35                           // SDK alvo
        versionCode = 1                          // código interno de versão
        versionName = "1.0"                      // nome da versão visível
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"  // runner para testes instrumentados
    }

    buildTypes {
        release {
            // desativa minificação em release (habilitar se usar R8/ProGuard)
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"             // regras ProGuard customizadas
            )
        }
    }

    compileOptions {
        // compatibilidade com Java 17
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // JVM target para Kotlin
        jvmTarget = "17"
    }

    buildFeatures {
        // habilita Jetpack Compose
        compose = true
    }
    composeOptions {
        // versão do Compose Compiler (estável)
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // Play Services Auth
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    // Firebase Auth via BOM
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // AndroidX Browser (Custom Tabs)
    implementation("androidx.browser:browser:1.6.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // Hilt for DI
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-compiler:2.51")

    // Room persistence
    val roomVersion = "2.7.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Charts library for Compose (stable)
    implementation("io.github.dautovicharis:charts-android:2.0.0")

    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM to align UI/Material/Foundation/Runtime versions
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")             // UI core
    implementation("androidx.compose.foundation:foundation") // Foundation (layouts, gestures)
    implementation("androidx.compose.material:material")     // Material components
    implementation("androidx.compose.runtime:runtime")       // Compose runtime
    implementation("androidx.compose.ui:ui-tooling-preview") // Preview in IDE

    // Material3 (stable)
    implementation("androidx.compose.material3:material3:1.3.2")

    // Compose tooling (debug only)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit tests
    testImplementation("junit:junit:4.13.2")

    // Instrumented Android tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Health connect
    implementation ("androidx.health.connect:connect-client:1.1.0-alpha10")
}

// Exclui conflitos de anotações do IntelliJ em todas as configurações
configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}
