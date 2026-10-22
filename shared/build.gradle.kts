import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.0.0"
    id("app.cash.sqldelight") version "2.1.0"
}

val ktor = "2.3.11"
val sqldelight = "2.1.0"

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "pe.edu.upeu.pharmamobil.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            api(libs.koin.android)
            implementation("io.ktor:ktor-client-okhttp:${ktor}")
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
            implementation("app.cash.sqldelight:android-driver:${sqldelight}")
        }
        commonMain.dependencies {
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.ktor:ktor-client-core:${ktor}")
            implementation("io.ktor:ktor-client-content-negotiation:${ktor}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor}")
            implementation("io.ktor:ktor-client-logging:${ktor}")
            implementation("io.ktor:ktor-client-auth:${ktor}")
            implementation("app.cash.sqldelight:runtime:${sqldelight}")
            implementation("app.cash.sqldelight:coroutines-extensions:${sqldelight}")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        named("androidHostTest").dependencies {
            implementation("app.cash.sqldelight:sqlite-driver:${sqldelight}")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:${ktor}")
            implementation("app.cash.sqldelight:native-driver:${sqldelight}")
        }
    }
}

sqldelight {
    databases {
        create("PharmaDb") {
            packageName.set("pe.edu.upeu.pharmamobil.db")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
}