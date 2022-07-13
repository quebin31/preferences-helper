plugins {
    alias(libs.plugins.android.lib)
    kotlin("android")
}

android {
    namespace = "com.quebin31.preferenceshelper"
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
        freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=kotlin.RequiresOptIn")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.coroutines.core)
    api(libs.androidx.datastore)

    testImplementation(libs.junit4)
    testImplementation(libs.strikt)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.core.test)
}