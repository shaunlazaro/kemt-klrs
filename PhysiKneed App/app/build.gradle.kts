plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.chaquo.python")
}

android {
    namespace = "com.example.physiokneeds_v3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.physiokneeds_v3"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    flavorDimensions += "pyVersion"
    productFlavors {
        create("py310") { dimension = "pyVersion" }
        create("py311") { dimension = "pyVersion" }
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        mlModelBinding = true
    }
}

chaquopy {
    productFlavors {
        getByName("py310") { version = "3.10" }
        getByName("py311") { version = "3.11" }
    }
    defaultConfig {
        pip {
            install("numpy")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.pose.detection.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.tasks.vision)
    implementation(libs.coil)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.dotsindicator)
    implementation(libs.androidx.fragment)

    implementation(libs.mpandroidchart)

    implementation(libs.androidx.cardview)

    implementation("androidx.credentials:credentials:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

}