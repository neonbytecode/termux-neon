// AGP 9.x ships built-in Kotlin (KGP 2.2.10+); the Compose compiler plugin
// (org.jetbrains.kotlin.plugin.compose, versioned in lockstep with Kotlin in
// gradle/libs.versions.toml) resolves the matching compiler automatically.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}