plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace   = "com.example.tfg_parking"
    compileSdk  = 36

    defaultConfig {
        applicationId = "com.example.tfg_parking"
        minSdk        = 24
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ⚠️ Sustituye estos valores por los tuyos reales
        buildConfigField("String", "SUPABASE_URL",      "\"https://vahugdvzyxqqcedqzqig.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_sNCUP-nBL0Ul7O89h-ui8g_IXelmDjB\"")
        // ⚠️ Sustituye por tu clave real de Google Maps
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = "AIzaSyB0rjUXUHbLs2HaAi_wyrcaRVRQ1EGnuhk"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

dependencies {
    // ── Core AndroidX ──────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    // AppCompat + Fragment KTX necesarios para SupportMapFragment
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)

    // ── Compose BOM ────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // ── Navegación ─────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

// ── Supabase ───────────────────────────────────────────────────────────
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)

    // ── Ktor ───────────────────────────────────────────────────────────────
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // ── Google Maps SDK (nativo, sin maps-compose) ─────────────────────────
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // ── Coroutines ─────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // ── Coil ───────────────────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── DataStore ──────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Tests ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
