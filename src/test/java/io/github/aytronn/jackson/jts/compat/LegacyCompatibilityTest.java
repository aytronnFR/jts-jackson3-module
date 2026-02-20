package io.github.aytronn.jackson.jts.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aytronn.jackson.jts.support.GeometryFixtures;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import tools.jackson.databind.json.JsonMapper;

class LegacyCompatibilityTest {
  private final GeometryFactory geometryFactory = GeometryFixtures.defaultFactory();

  private final JsonMapper currentMapper = JsonMapper.builder()
      .addModule(new io.github.aytronn.jackson.jts.JtsModule(geometryFactory))
      .build();

  private final ObjectMapper legacyMapper = new ObjectMapper()
      .registerModule(new org.n52.jackson.datatype.jts.JtsModule(geometryFactory));

  @Test
  void serialization_isGeometryEquivalentToLegacyImplementation() throws Exception {
    for (Geometry geometry : GeometryFixtures.sampleGeometries(geometryFactory)) {
      String currentJson = currentMapper.writeValueAsString(geometry);
      String legacyJson = legacyMapper.writeValueAsString(geometry);

      Geometry currentGeometry = legacyMapper.readValue(currentJson, Geometry.class);
      Geometry legacyGeometry = legacyMapper.readValue(legacyJson, Geometry.class);
      assertEquals(legacyGeometry.getGeometryType(), currentGeometry.getGeometryType());
      assertTrue(legacyGeometry.equalsExact(currentGeometry, 1e-8), "Serialized geometry differs for " + geometry.getGeometryType());
    }
  }

  @Test
  void crossDeserialization_matchesLegacyBehavior() throws Exception {
    for (Geometry geometry : GeometryFixtures.sampleGeometries(geometryFactory)) {
      String currentJson = currentMapper.writeValueAsString(geometry);
      String legacyJson = legacyMapper.writeValueAsString(geometry);

      Geometry currentFromLegacy = currentMapper.readValue(legacyJson, Geometry.class);
      Geometry legacyFromCurrent = legacyMapper.readValue(currentJson, Geometry.class);

      assertTrue(currentFromLegacy.equalsExact(geometry, 1e-8), "Current impl cannot read legacy JSON for " + geometry.getGeometryType());
      assertTrue(legacyFromCurrent.equalsExact(geometry, 1e-8), "Legacy impl cannot read current JSON for " + geometry.getGeometryType());
    }
  }
}
