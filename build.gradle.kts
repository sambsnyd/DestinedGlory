import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.build-scan") version("1.16")
    kotlin("multiplatform") version("1.3.10")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
}

repositories {
    jcenter()
}

val backendJvm = "backendJvm"
val frontendJs = "frontendJs"

kotlin {
    targets {
        // The fromPreset() method mentioned in JetBrains examples is exposed on a private inner class which is
        // added dynamically via an internal object not meant to be part of Gradle's public API
        // This doesn't hold with Kotlin or Gradle best practices and it's harder than any normal, supported way
        add(presets.getByName("jvm").createTarget(backendJvm))
        add(presets.getByName("js").createTarget(frontendJs))
    }

    sourceSets {
        // the dependencies task knows about these dependencies...
        // But the build scan says no dependencies were resolved for this build
        // TODO: File bug on build scan plugin and/or Kotlin multi-platform plugin
        getByName("commonMain") {
            dependencies {
                //implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }
        getByName("${backendJvm}Main") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("io.ktor:ktor-server-netty:1.0.0-rc")
                implementation("org.slf4j:slf4j-simple:1.7.25")
            }
        }
        getByName("${frontendJs}Main") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true

        }
    }

    // Bundle the outputs of the frontend build into the resources of the backend build
    // So the server has the javascript available to serve
    named("${backendJvm}ProcessResources").configure {
        this as ProcessResources
        from(named("compileKotlin${frontendJs.capitalize()}")) {
            into("staticassets")
        }
    }

    val backendJarTask = named("${backendJvm}Jar") as TaskProvider<Jar>

    // The application plugin isn't interoperable by default with the kotlin multiplatform plugin
    // So just manually create a JavaExec task that's appropriately wired up
    val run by registering(JavaExec::class) {
        dependsOn(backendJarTask)
        classpath =  files(configurations.backendJvmRuntimeClasspath, backendJarTask.get().archivePath)
        main = "com.github.sambsnyd.destinedglory.DestinedGloryServerKt"
    }

}
