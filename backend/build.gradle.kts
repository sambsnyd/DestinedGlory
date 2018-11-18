import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("application")
}

evaluationDependsOn(":frontend")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-server-netty:1.0.0-rc")
    implementation("org.slf4j:slf4j-simple:1.7.25")
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
    named("processResources").configure {
        this as ProcessResources
        from(project(":frontend").tasks.named("webpack-bundle")) {
            into("staticassets")
        }
    }
}

application {
    mainClassName = "com.github.sambsnyd.destinedglory.DestinedGloryServerKt"
}
