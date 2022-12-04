import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.7.10"
    id("application")
    id("java-library")
    id("maven-publish")
}

group = "cc.polyfrost.plugin"

repositories {
    mavenCentral {
        mavenContent {
            releasesOnly()
        }
    }
    maven("https://maven.reposilite.com/releases") {
        mavenContent {
            releasesOnly()
        }
    }
    maven("https://maven.reposilite.com/snapshots") {
        mavenContent {
            snapshotsOnly()
        }
    }
    maven("https://repo.polyfrost.cc/releases")
}

dependencies {
    compileOnly("com.reposilite:reposilite-backend:3.2.0")
    testImplementation("com.reposilite:reposilite-backend:3.2.0")

    val junit = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit")
}

java {
    withJavadocJar()
    withSourcesJar()
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.7"
        freeCompilerArgs = listOf("-Xjvm-default=all") // For generating default methods in interfaces
    }
}

tasks.withType<ShadowJar> {
    archiveFileName.set("polyapi-plugin.jar")
    mergeServiceFiles()
}

publishing {
    repositories {
        maven {
            name = "polyfrost"
            url = uri("https://repo.polyfrost.cc/${if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"}")
            credentials {
                username = System.getenv("MAVEN_NAME")
                password = System.getenv("MAVEN_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("library") {
            from(components.getByName("java"))
            pom.withXml {
                val repositories = asNode().appendNode("repositories")
                project.repositories.findAll(closureOf<Any> {
                    if (this is MavenArtifactRepository && this.url.toString().startsWith("https")) {
                        val repository = repositories.appendNode("repository")
                        repository.appendNode("id", this.url.toString().replace("https://", "").replace(".", "-").replace("/", "-"))
                        repository.appendNode("url", this.url.toString())
                    }
                })
            }
        }
    }
}

tasks.withType<Test> {
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }

    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2)
        .takeIf { it > 0 }
        ?: 1

    useJUnitPlatform()
}