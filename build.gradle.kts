plugins {
  `java-library`
  `maven-publish`
  id("com.vanniktech.maven.publish") version "0.36.0"
}

group = property("group") as String
version = property("version") as String

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
  withJavadocJar()
  withSourcesJar()
}

repositories {
  mavenCentral()
}

dependencies {
  api("tools.jackson.core:jackson-databind:3.0.2")
  api("org.locationtech.jts:jts-core:1.20.0")

  testImplementation(platform("org.junit:junit-bom:5.13.4"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
  testImplementation("org.n52.jackson:jackson-datatype-jts:2.0.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}

mavenPublishing {
  publishToMavenCentral(automaticRelease = true)
  if (!providers.gradleProperty("skipSigning").isPresent) {
    signAllPublications()
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifactId = "jts-jackson3-module"

      pom {
        name.set("JTS Jackson 3 Module")
        description.set("Jackson 3 module for JTS Geometry (GeoJSON serialization/deserialization)")
        url.set("https://github.com/aytronnFR/jts-jackson3-module")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/license/mit")
          }
        }

        developers {
          developer {
            id.set("aytronnfr")
            name.set("aytronn")
          }
        }

        scm {
          url.set("https://github.com/aytronnFR/jts-jackson3-module")
          connection.set("scm:git:https://github.com/aytronnfr/jts-jackson3-module.git")
          developerConnection.set("scm:git:ssh://git@github.com/aytronnfr/jts-jackson3-module.git")
        }
      }
    }
  }

  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/aytronnfr/jts-jackson3-module")
      credentials {
        username = providers.gradleProperty("gpr.user")
          .orElse(providers.environmentVariable("GITHUB_ACTOR"))
          .orNull
        password = providers.gradleProperty("gpr.key")
          .orElse(providers.environmentVariable("GITHUB_TOKEN"))
          .orNull
      }
    }
  }
}
