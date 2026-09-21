plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.neonbytecode.neon.termux"

    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":core:theme-engine"))
    testImplementation(libs.junit)
}