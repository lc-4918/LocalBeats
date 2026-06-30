import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.cll.localmusic"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cll.localmusic"
        minSdk = 26
        targetSdk = 34
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "dev"
        vectorDrawables { useSupportLibrary = true }
    }

    // On ne se base plus sur la variable "CI" (peu fiable selon le runner/daemon).
    // On se base directement sur la présence de KEYSTORE_PATH : si elle est définie
    // (ex: injectée par release.yml), on signe. Sinon (ex: ci.yml en assembleDebug),
    // on ne configure rien et aucune erreur n'est levée.
    val keystorePath = System.getenv("KEYSTORE_PATH")
    val hasReleaseSigning = !keystorePath.isNullOrEmpty()

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(keystorePath!!)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            // On n'attache la signingConfig que si elle a réellement été configurée.
            // Sinon : null -> pas de require() qui plante un assembleDebug,
            // et un assembleRelease sans secrets échouera plus tard avec un message
            // Android clair plutôt qu'une exception de configuration cryptique.
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Media3 (ExoPlayer + MediaSession) -> lecture audio en arriere-plan
    val media3 = "1.4.1"
    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-common:$media3")

    // Room -> playlists & historique en local
    val room = "2.6.1"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    // DataStore -> parametres (theme, etc.)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil -> chargement des pochettes (image du dossier + art embarque)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Permissions runtime en Compose
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
}