plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// CI injects a semver build like "0.32.1+<commit-sha>" (the "+" preserves
// version precedence) via TERMUX_STYLING_APP_BUILD__APP_VERSION_NAME. Locally
// it stays empty and the default versionName below is used.
val appVersionName = providers.environmentVariable("TERMUX_STYLING_APP_BUILD__APP_VERSION_NAME").orNull

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
        versionName = "1.0.0"

        if (appVersionName != null) {
            validateVersionName(appVersionName)
            versionName = appVersionName
        } else {
            validateVersionName(versionName = "1.0.0")
        }
    }

    signingConfigs {
        // Untrusted debug-only key so the add-on can be installed alongside a
        // debug build of Termux that is signed with the shared Termux key.
        // Release signing must come from CI/secrets, never from the repo.
        getByName("debug") {
            storeFile = file("testkey_untrusted.jks")
            storePassword = "xrj45yWGLbsO7W0v"
            keyAlias = "alias"
            keyPassword = "xrj45yWGLbsO7W0v"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
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

dependencies {
    implementation(project(":core:termux"))
    implementation(project(":core:theme-engine"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}