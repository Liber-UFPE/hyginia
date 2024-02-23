// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.
plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.apache.logging.log4j:log4j-core:2.23.0")
    implementation("commons-codec:commons-codec:1.16.1")

    implementation("com.lordcodes.turtle:turtle:0.9.0")

    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
}
