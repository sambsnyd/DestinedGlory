import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    kotlin("jvm")
    id("application")
}

// So that we can safely depend on a task from that project that produces the javascript bundle we want to serve
evaluationDependsOn(":frontend")

val ktorVersion = "1.0.0-rc"
val spekVersion = "1.1.5"
val junitVersion = "5.3.1"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")

    implementation("org.slf4j:slf4j-simple:1.7.25")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")

    testRuntimeOnly(kotlin("reflect"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
}

// Load API keys from .gitignore'd secrets.properties file, if one exists
val secretProps: Properties by lazy {
    val secretPropsFile = file("secrets.properties")
    val secretProps = Properties()
    if(secretPropsFile.exists()) {
        secretPropsFile.reader().use {
            secretProps.load(it)
        }
    }
     secretProps
}
val bungieApiKeyPropName = "bungieapikey"

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek")
        }
        if(secretProps.containsKey(bungieApiKeyPropName)) {
            environment[bungieApiKeyPropName] = secretProps[bungieApiKeyPropName]
        }
    }

    /**
     * The run JavaExec task provided by the application plugin should be passed the api key from the secrets.properties
     * if such a file exists
     */
    named("run").configure{
        this as JavaExec
        if(secretProps.containsKey(bungieApiKeyPropName)) {
            environment[bungieApiKeyPropName] = secretProps[bungieApiKeyPropName]
        }
    }

    // Bundle the outputs of the frontend build into the resources of the backend build
    // So the server can serve the javascript
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
