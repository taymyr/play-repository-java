@file:Suppress("UnstableApiUsage")

import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    id("org.jetbrains.dokka") version Versions.dokka
    id("org.jlleitschuh.gradle.ktlint") version Versions.`ktlint-plugin`
    `maven-publish`
    signing
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=all", "-Xjsr305=strict")
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=all", "-Xjsr305=strict")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(project(":play-repository-api-java"))
    compileOnly("com.typesafe.play", "play-java-jpa_$scalaBinaryVersion", playVersion)
    implementation("org.hibernate", "hibernate-entitymanager", Versions.hibernateVersion)

    testImplementation("com.typesafe.play", "play-java-jpa_$scalaBinaryVersion", playVersion)
    testImplementation("io.kotlintest", "kotlintest-runner-junit5", Versions.kotlintest)
    testImplementation("com.typesafe.play", "play-test_$scalaBinaryVersion", playVersion)
    testImplementation("com.typesafe.play", "play-jdbc-evolutions_$scalaBinaryVersion", playVersion)
    testImplementation("com.h2database", "h2", Versions.h2)
}

ktlint {
    version.set(Versions.ktlint)
    outputToConsole.set(true)
    reporters.set(setOf(ReporterType.CHECKSTYLE))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

tasks.dokka {
    outputFormat = "javadoc"
    outputDirectory = "${layout.buildDirectory}/javadoc"
    configuration {
        jdkVersion = 8
        reportUndocumented = false
    }
    impliedPlatforms = mutableListOf("JVM")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "${project.name}_$scalaBinaryVersion"
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJar)
            pom(Publishing.pom)
        }
    }
}

signing {
    useGpgCmd()
    isRequired = isRelease
    sign(publishing.publications["maven"])
}
