import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackBundler
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension

buildscript {
    repositories {
        jcenter()
        maven {
            url = project.uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-frontend-plugin:0.0.37")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin")
    }
}

plugins.apply(org.jetbrains.kotlin.gradle.plugin.Kotlin2JsPluginWrapper::class.java)
plugins.apply(org.jetbrains.kotlin.gradle.frontend.FrontendPlugin::class.java)


dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-js")
    compile("org.jetbrains.kotlinx:kotlinx-html-js:0.6.11")

    testCompile("org.jetbrains.kotlin:kotlin-test-js")
}

kotlinFrontend {
    downloadNodeJsVersion = "8.11.3"
    sourceMaps = true

    // In the groovy DSL this would just be "webpackBundle"
    // KotlinFrontendExtension overrides dynamic Groovy's missing method behavior to turn "[bundler]Bundle()"
    // into an invocation to "bundle([bundler])" which is a real method
    //
    // Jetbrains, this is so unnecessary. Why did you make me spend time figuring this out just to write a buildscript
    bundle<WebPackExtension>(WebPackBundler.bundlerId) {
        this as WebPackExtension
        mode = "development"
    }
}
npm {
    devDependency("qunit", "2.6.1")
    devDependency("karma", "2.0.4")
}

tasks {
    withType<KotlinJsCompile> {
        kotlinOptions {
            sourceMap = true
            moduleKind = "umd"
            sourceMapEmbedSources = "always"
        }
    }


}

// The kotlin-frontend plugin adds webpack-bundle in afterEvaluate... so we have to register a subsequent callback
// In order to make it depend upon the tasks that produce the things it bundles
// I'm baffled as to why the kotlin-frontend plugin doesn't do that already
// TODO: File bug on kotlin frontend plugin
afterEvaluate {
    tasks {
        named("webpack-bundle").configure {
            dependsOn(named("compileKotlin2Js"), named("processResources"))
        }
    }
}
