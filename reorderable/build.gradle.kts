plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")

}

android {
    namespace = "sh.calvin.reorderable"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    // Activa Compose
    buildFeatures {
        compose = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Usa BOM para alinear versiones de Compose
    api(platform("androidx.compose:compose-bom:2024.10.00"))

    // Expón los artefactos base de Compose a quien consuma tu librería
    api("androidx.compose.runtime:runtime")
    api("androidx.compose.ui:ui")
    api("androidx.compose.foundation:foundation")

    // Si tu código usa esto, añádelos (pueden ser implementation si no los expones en APIs)
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-util")

    // (Opcional) tooling solo debug
    debugImplementation("androidx.compose.ui:ui-tooling")
}