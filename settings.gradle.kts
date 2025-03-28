plugins {
    id("io.micronaut.platform.catalog") version "4.5.1"
}

rootProject.name = "hyginia"

// Use stable configuration-cache:
// https://docs.gradle.org/8.6/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
