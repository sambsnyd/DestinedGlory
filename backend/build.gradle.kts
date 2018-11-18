import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("application")
}

// So that we can safely depend on a task from that project that produces the javascript bundle we want to serve
evaluationDependsOn(":frontend")

val ktorVersion = "1.0.0-rc"
val spekVersion = "2.0.0-rc.1"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:1.7.25")

    testImplementation(kotlin("test"))
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion") {
        // Avoid pulling in any kotlin dependencies whose versions might not match
        exclude(group = "org.jetbrians.kotlin")
    }
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.junit.platform")
    }

    // Used by spek, need to specify it manually since we excluded all of its transitive dependencies
    testRuntimeOnly(kotlin("reflect"))
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true

        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
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
