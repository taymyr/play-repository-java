@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    id("org.jetbrains.dokka") version Versions.dokka
    id("org.jlleitschuh.gradle.ktlint") version Versions.`ktlint-plugin`
    signing
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
compileKotlin.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=all", "-Xjsr305=strict")

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "1.8"
compileTestKotlin.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=all", "-Xjsr305=strict")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinCoroutines)
    api(project(":play-repository-api-java"))
    compileOnly("com.typesafe.play", "play-java-jpa_$scalaBinaryVersion", playVersion)
    implementation("org.hibernate", "hibernate-entitymanager", Versions.hibernateVersion)

    testImplementation("io.kotest", "kotest-runner-junit5", Versions.kotest)
    testImplementation("io.kotest", "kotest-assertions-core", Versions.kotest)
    testImplementation("io.kotest", "kotest-property", Versions.kotest)
    testImplementation("com.typesafe.play", "play-test_$scalaBinaryVersion", playVersion)
    testImplementation("com.typesafe.play", "play-jdbc-evolutions_$scalaBinaryVersion", playVersion)
    testImplementation("com.h2database", "h2", Versions.h2)
}

configurations {
    testCompile.get().extendsFrom(compileOnly.get())
}

// Need for Hibernate can find persistence.xml in classpath
sourceSets.test {
    output.setResourcesDir(this.output.classesDirs.files.find { it.path.endsWith("kotlin${File.separator}test") }!!)
}

ktlint {
    version.set(Versions.ktlint)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
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
    outputDirectory = "$buildDir/javadoc"
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
    isRequired = isRelease
    sign(publishing.publications["maven"])
}
