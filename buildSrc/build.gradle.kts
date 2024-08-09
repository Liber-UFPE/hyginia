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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("commons-codec:commons-codec:1.17.1")

    implementation("com.lordcodes.turtle:turtle:0.10.0")

    // Manually adding commons-compress due to https://devhub.checkmarx.com/cve-details/CVE-2024-26308/
    implementation("org.apache.commons:commons-compress:1.27.0")

    // Manually adding apache-mime4j-dom due to https://github.com/advisories/GHSA-jw7r-rxff-gv24.
    // It can be removed when updating Apache Tika to a newer version
    implementation("org.apache.james:apache-mime4j-dom:0.8.11")
    implementation("org.apache.tika:tika-core:2.9.2")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.2")
}
