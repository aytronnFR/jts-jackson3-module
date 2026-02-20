# jts-jackson3-module

Jackson 3 module for JTS geometries (GeoJSON serialization/deserialization).

Compatible with:
- Spring Boot 4.x
- Jackson 3 (`tools.jackson.*`)
- JTS (`org.locationtech.jts`)

## Dependency

```kotlin
dependencies {
  implementation("io.github.aytronn:jts-jackson3-module:0.1.0")
}
```

## Usage

```java
import io.github.aytronn.jackson.jts.JtsModule;
import tools.jackson.databind.json.JsonMapper;

JsonMapper mapper = JsonMapper.builder()
    .addModule(new JtsModule())
    .build();
```

With bbox enabled:

```java
import io.github.aytronn.jackson.jts.GeometryType;
import io.github.aytronn.jackson.jts.IncludeBoundingBox;
import io.github.aytronn.jackson.jts.JtsModule;

var module = new JtsModule(IncludeBoundingBox.forTypes(GeometryType.POINT), 8);
```

## Build

```bash
./gradlew test
```

## Tests

- `src/test/java/io/github/aytronn/jackson/jts/roundtrip`: tests de round-trip Jackson 3
- `src/test/java/io/github/aytronn/jackson/jts/compat`: comparaison de comportement avec l'implémentation legacy `org.n52.jackson:jackson-datatype-jts:2.0.0`
- `src/test/java/io/github/aytronn/jackson/jts/support`: fixtures partagées

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
