plugins {
    alias(libs.plugins.android.lib)
    kotlin("android")
}

android {
    namespace = "com.example.datastorehelper"
    compileSdk = 32

    defaultConfig {
        minSdk = 23
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val defaultProguardFile = getDefaultProguardFile("proguard-android-optimize.txt")
            proguardFiles(defaultProguardFile, "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    api(libs.androidx.datastore)

    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
}