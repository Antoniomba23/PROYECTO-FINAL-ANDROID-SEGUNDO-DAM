plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.studybro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.studybro"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation("com.github.bumptech.glide:glide:4.16.0") //importamos el glid
    implementation("com.squareup.retrofit2:retrofit:3.0.0")//Dependencia de retrofit
    implementation("com.squareup.okhttp3:okhttp:5.3.0")//Dependencia de okhttp
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.0")//Dependencia de logging
    implementation("com.google.code.gson:gson:2.13.2")//Dependencia de gson
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")//Dependencia de converter gson
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}