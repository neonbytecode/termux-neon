// AGP 9.x ships built-in Kotlin (KGP 2.2.10+). Pin a newer KGP here so the
// Compose compiler plugin (versioned in lockstep with Kotlin) matches the
// Kotlin compiler version used across modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}