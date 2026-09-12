plugins {
    id("java")
    id("maven-publish")
}

group = "net.jandie1505"
version = "1.1-RELEASE"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("org.json:json:20260814")
    compileOnly("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.json:json:20260814")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

// gradle publish{PUBLICATION_NAME}To{REPOSITORY_NAME}Repository
// in this case: publishMavenToChaosSquadRepository
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "chaossquad"
            url = uri(if (version.toString().endsWith("RELEASE")) {
                "https://maven.chaossquad.net/releases"
            } else {
                "https://maven.chaossquad.net/snapshots"
            })

            credentials {
                username = findProperty("chaossquad-repository.username") as String?
                password = findProperty("chaossquad-repository.password") as String?
            }
        }
    }
}