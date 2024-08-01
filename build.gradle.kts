import fr.brouillard.oss.jgitver.Strategies.MAVEN
import java.time.Duration

plugins {
    kotlin("jvm") version Versions.kotlin apply false
    kotlin("plugin.jpa") version Versions.kotlin apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("fr.brouillard.oss.gradle.jgitver") version "0.9.1"
}

allprojects {
    group = "org.taymyr.play"
    repositories {
        mavenCentral()
    }
}

jgitver {
    strategy(MAVEN)
}

nexusPublishing {
    packageGroup.set("org.taymyr")
    clientTimeout.set(Duration.ofMinutes(60))
    repositories {
        sonatype()
    }
}
