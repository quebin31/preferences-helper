plugins {
    alias(libs.plugins.android.lib) apply false
    kotlin("android") version libs.versions.kotlin.get() apply false
}
