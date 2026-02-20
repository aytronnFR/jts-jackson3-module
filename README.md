# jts-jackson3-module

Jackson 3 module for JTS geometries (GeoJSON serialization/deserialization).

Compatible with:
- Spring Boot 4.x
- Jackson 3 (`tools.jackson.*`)
- JTS (`org.locationtech.jts`)

## Dependency

```kotlin
dependencies {
  implementation("io.github.aytronnfr:jts-jackson3-module:0.1.0")
}
```

## Usage

```java
import io.github.aytronnfr.jackson.jts.JtsModule;
import tools.jackson.databind.json.JsonMapper;

JsonMapper mapper = JsonMapper.builder()
    .addModule(new JtsModule())
    .build();
```

With bbox enabled:

```java
import io.github.aytronnfr.jackson.jts.GeometryType;
import io.github.aytronnfr.jackson.jts.IncludeBoundingBox;
import io.github.aytronnfr.jackson.jts.JtsModule;

var module = new JtsModule(IncludeBoundingBox.forTypes(GeometryType.POINT), 8);
```

## Build

```bash
./gradlew test
```

## Tests

- `src/test/java/io/github/aytronnfr/jackson/jts/roundtrip`: tests de round-trip Jackson 3
- `src/test/java/io/github/aytronnfr/jackson/jts/compat`: comparaison de comportement avec l'implémentation legacy `org.n52.jackson:jackson-datatype-jts:2.0.0`
- `src/test/java/io/github/aytronnfr/jackson/jts/support`: fixtures partagées

## Publish to GitHub Packages

```bash
./gradlew publish
```

Set credentials with either:
- env vars: `GITHUB_ACTOR`, `GITHUB_TOKEN`
- or Gradle properties: `gpr.user`, `gpr.key`

## GitHub Actions

- CI (`.github/workflows/ci.yml`): build + tests on push/PR
- Publish (`.github/workflows/publish.yml`):
  - automatic publish on tag `v*` (example: `v0.1.0`)
  - manual publish via `workflow_dispatch` with `version`

For GitHub Packages publish from Actions, `GITHUB_TOKEN` is used automatically with:
- `permissions.contents: read`
- `permissions.packages: write`

## Publish to Maven Central

Maven Central publication is configured with `com.vanniktech.maven.publish`.

Local command:

```bash
./gradlew publishAndReleaseToMavenCentral
```

GitHub Actions workflow:
- `.github/workflows/publish-central.yml`
- trigger on tag `v*` or manual dispatch with `version`

Required repository secrets:
- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY` (ASCII-armored private key)
- `MAVEN_GPG_PASSPHRASE`

For public consumption from Maven Central (no authentication needed):

```kotlin
repositories {
  mavenCentral()
}
```
