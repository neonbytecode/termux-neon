plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// CI injects a semver build like "0.32.1+<commit-sha>" (the "+" preserves
// version precedence) via TERMUX_STYLING_APP_BUILD__APP_VERSION_NAME. Locally
// it stays empty and the default versionName below is used.
// CI resolves this value through the `printVersionName` task.
val appVersionName = providers.environmentVariable("TERMUX_STYLING_APP_BUILD__APP_VERSION_NAME").orNull
val defaultVersionName = "1.0.0"
val resolvedVersionName = appVersionName ?: defaultVersionName

// https://semver.org/spec/v2.0.0.html#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
val semverPattern = Regex(
    """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$""",
)

fun validateVersionName(versionName: String) {
    require(semverPattern.matches(versionName)) {
        "The versionName '$versionName' is not a valid version as per semantic version '2.0.0' spec " +
            "in the format 'major.minor.patch(-prerelease)(+buildmetadata)'. https://semver.org/spec/v2.0.0.html."
    }
}

android {
    namespace = "com.termux.styling"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.termux.styling"
        minSdk = 28
        targetSdk = 37
        versionCode = 2000
        versionName = resolvedVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

validateVersionName(resolvedVersionName)

tasks.register("printVersionName") {
    doLast {
        println(resolvedVersionName)
    }
}

dependencies {
    implementation(project(":core:termux"))
    implementation(project(":core:theme-engine"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}