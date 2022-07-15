plugins {
    alias(libs.plugins.android.lib)
    kotlin("android")
    signing
    `maven-publish`
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

    publishing {
        multipleVariants {
            allVariants()
            withJavadocJar()
            withSourcesJar()
        }
    }
}

signing {
    useInMemoryPgpKeys(project.signingKey, project.signingPassword)
    sign(publishing.publications)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("default") {
                groupId = "com.quebin31"
                artifactId = "preferences-helper"
                version = "1.0.0"

                from(components["default"])

                pom {
                    name.set("preferences-helper")
                    description.set("Minimal add-on library which provides a nicer API  for Preferences Datastore")
                    url.set("https://github.com/quebin31/preferences-helper")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://github.com/quebin31/preferences-helper/blob/main/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            name.set("Kevin Del Castillo")
                            id.set("quebin31")
                            email.set("quebin31@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/quebin31/preferences-helper")
                        developerConnection.set("scm:git:ssh://github.com/quebin31/preferences-helper")
                        url.set("https://github.com/quebin31/preferences-helper/tree/main")
                    }
                }
            }
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