// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

}
buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url ="https://maven.google.com")
    }
    dependencies {
        // Correct Kotlin DSL syntax for AGP
        classpath(libs.gradle)

    }
}

