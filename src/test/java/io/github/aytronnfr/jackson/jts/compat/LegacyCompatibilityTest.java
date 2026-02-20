package io.github.aytronnfr.jackson.jts.compat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aytronnfr.jackson.jts.JtsModule;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.databind.json.JsonMapper;
import java.util.logging.Logger;

class LegacyCompatibilityTest {
  private final Logger log = Logger.getLogger(LegacyCompatibilityTest.class.getName());
  private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

  private final JsonMapper currentMapper = JsonMapper.builder()
      .addModule(new JtsModule(geometryFactory))
      .build();

  private final ObjectMapper legacyMapper = new ObjectMapper()
      .registerModule(new org.n52.jackson.datatype.jts.JtsModule(geometryFactory));

  @Test
  void pointSerialization_matchesJacksonV2GeoJson() throws Exception {
    Point point = geometryFactory.createPoint(new Coordinate(1.234567891, 2.345678912));
    assertGeoJsonEquality(point);
  }

  @Test
  void lineStringSerialization_matchesJacksonV2GeoJson() throws Exception {
    LineString lineString = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1.1, 2.2)});
    assertGeoJsonEquality(lineString);
  }

  @Test
  void polygonSerialization_matchesJacksonV2GeoJson() throws Exception {
    Polygon polygon = geometryFactory.createPolygon(new Coordinate[] {
        new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)
    });
    assertGeoJsonEquality(polygon);
  }

  @Test
  void multiPointSerialization_matchesJacksonV2GeoJson() throws Exception {
    MultiPoint multiPoint = geometryFactory.createMultiPointFromCoords(
        new Coordinate[] {new Coordinate(1, 1), new Coordinate(2, 2)});
    assertGeoJsonEquality(multiPoint);
  }

  @Test
  void multiLineStringSerialization_matchesJacksonV2GeoJson() throws Exception {
    LineString line1 = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 0)});
    LineString line2 = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(0, 1), new Coordinate(1, 1)});
    MultiLineString multiLineString = geometryFactory.createMultiLineString(new LineString[] {line1, line2});
    assertGeoJsonEquality(multiLineString);
  }

  @Test
  void multiPolygonSerialization_matchesJacksonV2GeoJson() throws Exception {
    Polygon square = geometryFactory.createPolygon(new Coordinate[] {
        new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)
    });
    Polygon triangle = geometryFactory.createPolygon(new Coordinate[] {
        new Coordinate(2, 2), new Coordinate(3, 2), new Coordinate(2, 3), new Coordinate(2, 2)
    });
    MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[] {square, triangle});
    assertGeoJsonEquality(multiPolygon);
  }

  @Test
  void geometryCollectionSerialization_matchesJacksonV2GeoJson() throws Exception {
    Point point = geometryFactory.createPoint(new Coordinate(5, 6));
    LineString line = geometryFactory.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(2, 2)});
    GeometryCollection collection = geometryFactory.createGeometryCollection(new Geometry[] {point, line});
    assertGeoJsonEquality(collection);
  }

  @Test
  void pointDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}");
  }

  @Test
  void lineStringDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals("{\"type\":\"LineString\",\"coordinates\":[[0,0],[1.1,2.2]]}");
  }

  @Test
  void polygonDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}");
  }

  @Test
  void multiPointDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals("{\"type\":\"MultiPoint\",\"coordinates\":[[1,1],[2,2]]}");
  }

  @Test
  void multiLineStringDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals("{\"type\":\"MultiLineString\",\"coordinates\":[[[0,0],[1,0]],[[0,1],[1,1]]]}");
  }

  @Test
  void multiPolygonDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals(
        "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[0,0],[1,0],[1,1],[0,1],[0,0]]],[[[2,2],[3,2],[2,3],[2,2]]]]}");
  }

  @Test
  void geometryCollectionDeserialization_matchesJacksonV2() throws Exception {
    assertGeometryDeserializationEquals(
        "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[5,6]},{\"type\":\"LineString\",\"coordinates\":[[0,0],[2,2]]}]}");
  }

  private void assertGeoJsonEquality(Geometry geometry) throws Exception {
    String json = currentMapper.writeValueAsString(geometry);
    String expected = legacyMapper.writeValueAsString(geometry);

    log.info("Actual: " + json);
    log.info("Expected: " + expected);

    Geometry actualGeometry = currentMapper.readValue(json, Geometry.class);
    Geometry expectedGeometry = currentMapper.readValue(expected, Geometry.class);
    assertTrue(actualGeometry.equalsExact(expectedGeometry, 1e-9), "Serialization differs between Jackson2 and Jackson3");
  }

  private void assertGeometryDeserializationEquals(String json) throws Exception {
    Geometry jackson3 = currentMapper.readValue(json, Geometry.class);
    Geometry jackson2 = legacyMapper.readValue(json, Geometry.class);
    assertTrue(jackson3.equalsExact(jackson2, 1e-9), "Deserialization differs between Jackson2 and Jackson3");
  }
}
