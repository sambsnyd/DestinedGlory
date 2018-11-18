plugins {
    id("com.gradle.build-scan").version("1.16")

    // The different kotlin plugins are all in the same jar so this DRYly specifies the version of
    // all the kotlin plugins
    kotlin("jvm").version("1.3.10").apply(false)
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
}

allprojects {
    repositories {
        jcenter()
    }
}