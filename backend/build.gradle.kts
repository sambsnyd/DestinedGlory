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

// Create a separate sourceSet and dependency Configuration for integration tests
lateinit var testIntegrationSourceSet: SourceSet
lateinit var mainSourceSet: SourceSet
java {
    sourceSets {
        mainSourceSet = getByName("main")
        testIntegrationSourceSet = create("testIntegration") {
            allSource.srcDirs(listOf(file("src/testIntegration/kotlin")))
            compileClasspath += mainSourceSet.output
        }
    }
}

configurations {
    val implementation = getByName("implementation")
    getByName("testIntegrationImplementation") {
        extendsFrom(implementation)
    }
}

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
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")

    testRuntimeOnly(kotlin("reflect"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")

    "testIntegrationImplementation"(kotlin("test"))
    "testIntegrationImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    "testIntegrationImplementation"("org.jetbrains.spek:spek-api:$spekVersion")

    "testIntegrationRuntimeOnly"(kotlin("reflect"))
    "testIntegrationRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    "testIntegrationRuntimeOnly"("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
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

    val integrationTest = register("testIntegration", Test::class.java) {
        group = "Verification"
        description = "Runs the integration tests"
        testClassesDirs = testIntegrationSourceSet.output.classesDirs
        classpath = mainSourceSet.output + testIntegrationSourceSet.output + configurations.getByName("testRuntimeClasspath")
        if(secretProps.containsKey(bungieApiKeyPropName)) {
            environment[bungieApiKeyPropName] = secretProps[bungieApiKeyPropName]
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek")
        }
    }

    named("check").configure {
        dependsOn(integrationTest)
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
