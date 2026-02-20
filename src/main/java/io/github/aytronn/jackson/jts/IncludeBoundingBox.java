package io.github.aytronn.jackson.jts;

import java.util.EnumSet;
import java.util.Objects;

@FunctionalInterface
public interface IncludeBoundingBox {
  boolean shouldIncludeBoundingBoxFor(GeometryType type);

  static IncludeBoundingBox never() {
    return type -> false;
  }

  static IncludeBoundingBox always() {
    return type -> true;
  }

  static IncludeBoundingBox forTypes(GeometryType... types) {
    var set = EnumSet.noneOf(GeometryType.class);
    if (types != null) {
      for (var t : types) {
        if (t != null) set.add(t);
      }
    }
    return type -> set.contains(Objects.requireNonNull(type));
  }
}
