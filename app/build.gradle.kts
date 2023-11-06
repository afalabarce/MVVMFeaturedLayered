plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = BuildVersion.environment.applicationId
    compileSdk = 33

    defaultConfig {
        applicationId = BuildVersion.environment.applicationId
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = BuildVersion.testEnvironment.instrumentationRunner
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = BuildVersion.environment.javaVersion
        targetCompatibility = BuildVersion.environment.javaVersion
    }
    kotlinOptions {
        jvmTarget = BuildVersion.environment.jvmTarget
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.bundles.layer.ui)
    // Don't remove or modify this line!!
    // [Feature-Manager dependencies]

    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}