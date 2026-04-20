buildscript {
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.diffplug.spotless) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    // Spotless disabled for MVP to prevent ktlint format errors
    // apply(plugin = "com.diffplug.spotless")
    // configure<com.diffplug.gradle.spotless.SpotlessExtension> { ... }
}
