plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.proyecto_adroid"
    // Subimos a 35 para que las librerías modernas no den error de AAR metadata
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyecto_adroid"
        minSdk = 24
        // TargetSdk debe coincidir con compileSdk para evitar advertencias
        targetSdk = 35
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
        // Mantenemos Java 11, que es el estándar para compatibilidad en Android
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Librerías base del sistema
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Material Design (Solo una vez)
    implementation(libs.material)

    // Componentes de Navegación para el Sidebar
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Pruebas y Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}