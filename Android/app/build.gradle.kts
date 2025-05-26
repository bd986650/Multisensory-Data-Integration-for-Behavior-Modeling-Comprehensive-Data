plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.eventcycles"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eventcycles"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.gms:play-services-fitness:21.1.0")
    implementation("com.google.android.gms:play-services-auth:20.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation("org.osmdroid:osmdroid-android:6.1.10")
    implementation("com.google.code.gson:gson:2.8.9")    // Библиотека для работы с JSON
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Конвертер GSON (для JSON <-> Kotlin)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp (включает логирование запросов, если надо)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation(libs.play.services.location)
    implementation(libs.androidx.constraintlayout) // WorkManager для фоновых задач
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}