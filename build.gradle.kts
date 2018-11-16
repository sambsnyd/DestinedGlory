import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version("1.3.10")
}

repositories {
    jcenter()
}

kotlin {
    targets {
        // The fromPreset() method mentioned in JetBrains examples is exposed on a private inner class which is
        // added dynamically via an internal object not meant to be part of Gradle's public API
        // This doesn't hold with Kotlin or Gradle best practices and it's harder than any normal, supported way
        add(presets.getByName("jvm").createTarget("jvm"))
        add(presets.getByName("js").createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }
        }
        getByName("jsMain") {

        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
    }
}
