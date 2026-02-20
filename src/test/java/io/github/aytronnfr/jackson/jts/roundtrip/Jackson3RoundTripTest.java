package io.github.aytronnfr.jackson.jts.roundtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aytronnfr.jackson.jts.GeometryType;
import io.github.aytronnfr.jackson.jts.IncludeBoundingBox;
import io.github.aytronnfr.jackson.jts.JtsModule;
import io.github.aytronnfr.jackson.jts.support.GeometryFixtures;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;

class Jackson3RoundTripTest {
  private final GeometryFactory geometryFactory = GeometryFixtures.defaultFactory();

  private final JsonMapper mapper = JsonMapper.builder()
      .addModule(new JtsModule(geometryFactory))
      .build();

  @Test
  void geometryRoundTrip_serializationAndDeserialization() throws Exception {
    for (Geometry input : GeometryFixtures.sampleGeometries(geometryFactory)) {
      String json = mapper.writeValueAsString(input);
      Geometry output = mapper.readValue(json, Geometry.class);

      assertEquals(input.getGeometryType(), output.getGeometryType());
      assertTrue(output.equalsExact(input, 1e-8), "Round-trip differs for " + input.getGeometryType());
    }
  }

  @Test
  void includeBoundingBox_writesBboxWhenEnabled() throws Exception {
    JsonMapper mapperWithBbox = JsonMapper.builder()
        .addModule(new JtsModule(geometryFactory, IncludeBoundingBox.forTypes(GeometryType.POINT), 8))
        .build();

    Point point = geometryFactory.createPoint(new Coordinate(1, 2));
    String json = mapperWithBbox.writeValueAsString(point);

    assertTrue(json.contains("\"bbox\""));
  }

  @Test
  void deserializeGeometryCollection() throws Exception {
    Geometry geometry = mapper.readValue(GeometryFixtures.geometryCollectionJson(), Geometry.class);

    assertTrue(geometry instanceof GeometryCollection);
    assertEquals(2, ((GeometryCollection) geometry).getNumGeometries());
  }
}
