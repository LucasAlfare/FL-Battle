/**
 * Not to many dependencies, version catalog is not needed for now.
 */
plugins {
  kotlin("jvm") version "2.2.0"
  application
  `maven-publish`
}

group = "com.lucasalfare"
version = "1.0-alpha-1"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("ch.qos.logback:logback-classic:1.5.18")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}

/**
 * Helper block to configure Maven Publishing.
 */
publishing {
  publications {
    create<MavenPublication>("Maven") {
      from(components["kotlin"])
    }
  }
}