import org.gradle.api.Project

val Project.isRelease get() = this.version.toString().endsWith("SNAPSHOT")

val Project.scalaBinaryVersion get() = this.properties["scalaBinaryVersion"] as String? ?: Versions.scalaBinary
