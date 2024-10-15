@file:Suppress("UnstableApiUsage")

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
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.compileKotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjvm-default=all", "-Xjsr305=strict")
    }
}

tasks.compileTestKotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjvm-default=all", "-Xjsr305=strict")
    }
}

dependencies {
    api(project(":play-repository-api-java"))
    compileOnly("org.playframework", "play-java-jpa_$scalaBinaryVersion", Versions.play)
    implementation("org.hibernate.orm", "hibernate-core", Versions.hibernateVersion)

    testImplementation("org.playframework", "play-java-jpa_$scalaBinaryVersion", Versions.play)
    testImplementation("io.kotlintest", "kotlintest-runner-junit5", Versions.kotlintest)
    testImplementation("org.playframework", "play-test_$scalaBinaryVersion", Versions.play)
    testImplementation("org.playframework", "play-jdbc-evolutions_$scalaBinaryVersion", Versions.play)
    testImplementation("com.h2database", "h2", Versions.h2)
}

ktlint {
    version.set(Versions.ktlint)
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
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    dependsOn(tasks.dokkaJavadoc)
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
