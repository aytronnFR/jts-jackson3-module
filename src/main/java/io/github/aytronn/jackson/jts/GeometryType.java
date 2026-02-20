package io.github.aytronn.jackson.jts;

import java.util.Optional;

public enum GeometryType {
  POINT("Point"),
  MULTI_POINT("MultiPoint"),
  LINE_STRING("LineString"),
  MULTI_LINE_STRING("MultiLineString"),
  POLYGON("Polygon"),
  MULTI_POLYGON("MultiPolygon"),
  GEOMETRY_COLLECTION("GeometryCollection");

  private final String jsonName;

  GeometryType(String jsonName) {
    this.jsonName = jsonName;
  }

  @Override
  public String toString() {
    return jsonName;
  }

  static Optional<GeometryType> fromString(String value) {
    if (value == null) return Optional.empty();
    String v = value.trim();
    for (GeometryType t : values()) {
      if (t.jsonName.equals(v) || t.name().equalsIgnoreCase(v)) {
        return Optional.of(t);
      }
    }
    return Optional.empty();
  }
}
