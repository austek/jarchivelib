plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.org.apache.commons.commons.compress)
    api(libs.org.tukaani.xz)

    testImplementation(platform(libs.junit.bom))

    testImplementation(libs.assertj.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)

    testRuntimeOnly(libs.junit.platform.launcher)

}

group = "org.archive4j"
version = "1.3.0-SNAPSHOT"
description = "Java archiving library"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

spotless {
    ratchetFrom("origin/main")
    java {
        toggleOffOn()
        palantirJavaFormat().formatJavadoc(true)
        licenseHeaderFile(rootProject.file(".config/spotless/copyright.java"))
    }
}
