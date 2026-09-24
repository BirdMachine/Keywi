import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.4.20"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility", "-opt-in=kotlin.RequiresOptIn")
    }
}

val keywiDebugStoreFile = providers.environmentVariable("KEYWI_DEBUG_STORE_FILE").orNull
val keywiDebugStorePassword = providers.environmentVariable("KEYWI_DEBUG_STORE_PASSWORD").orNull
val keywiDebugKeyAlias = providers.environmentVariable("KEYWI_DEBUG_KEY_ALIAS").orNull
val keywiDebugKeyPassword = providers.environmentVariable("KEYWI_DEBUG_KEY_PASSWORD").orNull
val keywiCiVersionCode = providers.environmentVariable("KEYWI_VERSION_CODE").orNull?.toIntOrNull()

val keywiIconSource = rootProject.file("assets/file_000000003e3481f5acdf9449f6204a26.png")
val generatedKeywiIconResDir = layout.buildDirectory.dir("generated/keywiIcon/res")

val generateKeywiLauncherIcon by tasks.registering(Copy::class) {
    from(keywiIconSource)
    into(generatedKeywiIconResDir.map { it.dir("mipmap-nodpi") })
    rename { "keywi_launcher.png" }
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "com.birdmachine.keywi"
        minSdk = 24
        targetSdk = 37
        versionCode = keywiCiVersionCode ?: 186
        versionName = "5.1.16-keywi.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    signingConfigs {
        if (
            keywiDebugStoreFile != null &&
            keywiDebugStorePassword != null &&
            keywiDebugKeyAlias != null &&
            keywiDebugKeyPassword != null
        ) {
            create("keywiDebug") {
                storeFile = file(keywiDebugStoreFile)
                storePassword = keywiDebugStorePassword
                keyAlias = keywiDebugKeyAlias
                keyPassword = keywiDebugKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }

        if (project.hasProperty("RELEASE_STORE_FILE")) {
            create("release") {
                storeFile = file(project.property("RELEASE_STORE_FILE")!!)
                storePassword = project.property("RELEASE_STORE_PASSWORD") as String?
                keyAlias = project.property("RELEASE_KEY_ALIAS") as String?
                keyPassword = project.property("RELEASE_KEY_PASSWORD") as String?
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            if (project.hasProperty("RELEASE_STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " (DEBUG)"
            if (signingConfigs.names.contains("keywiDebug")) {
                signingConfig = signingConfigs.getByName("keywiDebug")
            }
        }
    }

    lint {
        disable += "MissingTranslation"
        disable += "KtxExtensionAvailable"
        disable += "UseKtx"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets["main"].res.srcDir(generatedKeywiIconResDir)
    namespace = "com.dessalines.thumbkey"
}

tasks.named("preBuild").configure {
    dependsOn(generateKeywiLauncherIcon)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation("junit:junit:4.13.2")

    implementation("com.github.woheller69:FreeDroidWarn:V1.14")
    implementation("com.github.dessalines:room-db-export-import:0.1.1")

    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.runtime:runtime-livedata:1.12.1")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.navigation:navigation-compose:2.10.1")
    implementation("androidx.emoji2:emoji2-emojipicker:1.6.0")
    implementation("com.github.jeziellago:compose-markdown:0.7.2")
    implementation("me.zhanghai.compose.preference:library:1.1.1")
    implementation("com.louiscad.splitties:splitties-systemservices:3.0.0")
    implementation("com.louiscad.splitties:splitties-views:3.0.0")

    ksp("androidx.room:room-compiler:2.8.5")
    implementation("androidx.room:room-runtime:2.8.5")
    annotationProcessor("androidx.room:room-compiler:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")

    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.charleskorn.kaml:kaml:0.104.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.4.20")
    implementation("io.arrow-kt:arrow-optics:2.2.3")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:2.2.3")
}
