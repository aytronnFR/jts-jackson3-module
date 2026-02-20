package io.github.aytronn.jackson.jts;

import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.InvalidDefinitionException;

public class GeometryDeserializer extends ValueDeserializer<Geometry> {

  private static final int DEFAULT_SRID = 4326;
  private static final GeometryFactory DEFAULT_GEOMETRY_FACTORY = getDefaultGeometryFactory();

  private final GeometryFactory geometryFactory;

  public GeometryDeserializer() {
    this(null);
  }

  @Override
  public Geometry deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
    return deserializeGeometry(p.readValueAs(JsonNode.class), context);
  }

  public GeometryDeserializer(GeometryFactory geometryFactory) {
    this.geometryFactory = Optional.ofNullable(geometryFactory).orElse(DEFAULT_GEOMETRY_FACTORY);
  }

  private Geometry deserializeGeometry(JsonNode node, DeserializationContext context) throws DatabindException {
    String typeName = node.get(Field.TYPE).asText();

    GeometryType type = GeometryType.fromString(typeName).orElseThrow(() -> invalidGeometryType(context, typeName));

    return switch (type) {
      case POINT -> deserializePoint(node, context);
      case MULTI_POINT -> deserializeMultiPoint(node, context);
      case LINE_STRING -> deserializeLineString(node, context);
      case MULTI_LINE_STRING -> deserializeMultiLineString(node, context);
      case POLYGON -> deserializePolygon(node, context);
      case MULTI_POLYGON -> deserializeMultiPolygon(node, context);
      case GEOMETRY_COLLECTION -> deserializeGeometryCollection(node, context);
    };
  }

  private DatabindException invalidGeometryType(DeserializationContext context, String typeName) {
    return InvalidDefinitionException.from(context, "Invalid geometry type: " + typeName);
  }

  private Point deserializePoint(JsonNode node, DeserializationContext context) throws DatabindException {
    JsonNode coordinates = getArray(node, context, Field.COORDINATES);
    return this.geometryFactory.createPoint(deserializeCoordinate(coordinates, context));
  }

  private Polygon deserializePolygon(JsonNode node, DeserializationContext context) throws DatabindException {
    JsonNode coordinates = getArray(node, context, Field.COORDINATES);
    return deserializeLinearRings(coordinates, context);
  }

  private MultiPolygon deserializeMultiPolygon(JsonNode node, DeserializationContext context) throws DatabindException {
    JsonNode coordinates = getArray(node, context, Field.COORDINATES);
    Polygon[] polygons = new Polygon[coordinates.size()];
    for (int i = 0; i != coordinates.size(); ++i) {
      polygons[i] = deserializeLinearRings(coordinates.get(i), context);
    }
    return this.geometryFactory.createMultiPolygon(polygons);
  }

  private MultiPoint deserializeMultiPoint(JsonNode node, DeserializationContext context) throws DatabindException {
    JsonNode coordinates = getArray(node, context, Field.COORDINATES);
    Coordinate[] coords = deserializeCoordinates(coordinates, context);
    return this.geometryFactory.createMultiPointFromCoords(coords);
  }

  private GeometryCollection deserializeGeometryCollection(JsonNode node, DeserializationContext context)
      throws DatabindException {
    JsonNode geometries = getArray(node, context, Field.GEOMETRIES);
    Geometry[] geom = new Geometry[geometries.size()];
    for (int i = 0; i != geometries.size(); ++i) {
      geom[i] = deserializeGeometry(geometries.get(i), context);
    }
    return this.geometryFactory.createGeometryCollection(geom);
  }

  private MultiLineString deserializeMultiLineString(JsonNode node, DeserializationContext context)
      throws DatabindException {
    JsonNode coordinates = getArray(node, context, Field.COORDINATES);
    LineString[] lineStrings = lineStringsFromJson(coordinates, context);
    return this.geometryFactory.createMultiLineString(lineStrings);
  }

  private LineString[] lineStringsFromJson(JsonNode node, DeserializationContext context) throws DatabindException {
    LineString[] strings = new LineString[node.size()];
    for (int i = 0; i != node.size(); ++i) {
      Coordinate[] coordinates = deserializeCoordinates(node.get(i), context);
      strings[i] = this.geometryFactory.createLineString(coordinates);
    }
    return strings;
  }

  private LineString deserializeLineString(JsonNode node, DeserializationContext context) throws DatabindException {
    JsonNode coordinates = getArray(node, context, Field.COORDINATES);
    Coordinate[] coords = deserializeCoordinates(coordinates, context);
    return this.geometryFactory.createLineString(coords);
  }

  private Coordinate[] deserializeCoordinates(JsonNode node, DeserializationContext context) throws DatabindException {
    Coordinate[] points = new Coordinate[node.size()];
    for (int i = 0; i != node.size(); ++i) {
      points[i] = deserializeCoordinate(node.get(i), context);
    }
    return points;
  }

  private Polygon deserializeLinearRings(JsonNode node, DeserializationContext context) throws DatabindException {
    LinearRing shell = deserializeLinearRing(node.get(0), context);
    LinearRing[] holes = new LinearRing[node.size() - 1];
    for (int i = 1; i < node.size(); ++i) {
      holes[i - 1] = deserializeLinearRing(node.get(i), context);
    }
    return this.geometryFactory.createPolygon(shell, holes);
  }

  private LinearRing deserializeLinearRing(JsonNode node, DeserializationContext context) throws DatabindException {
    Coordinate[] coordinates = deserializeCoordinates(node, context);
    return this.geometryFactory.createLinearRing(coordinates);
  }

  private Coordinate deserializeCoordinate(JsonNode node, DeserializationContext context) throws DatabindException {
    if (node.size() < 2) {
      throw InvalidDefinitionException.from(context, "Invalid coordinate: " + node);
    }
    double x = getOrdinate(node, 0, context);
    double y = getOrdinate(node, 1, context);
    if (node.size() < 3) {
      return new Coordinate(x, y);
    }
    double z = getOrdinate(node, 2, context);
    return new Coordinate(x, y, z);
  }

  private JsonNode getArray(JsonNode node, DeserializationContext context, String fieldName) throws DatabindException {
    JsonNode coordinates = node.get(fieldName);
    if (coordinates != null && !coordinates.isArray()) {
      throw InvalidDefinitionException.from(context, "Invalid coordinate: " + coordinates);
    }
    return coordinates;
  }

  private double getOrdinate(JsonNode node, int i, DeserializationContext context) throws DatabindException {
    JsonNode ordinate = node.get(i);
    if (!ordinate.isNumber()) {
      throw InvalidDefinitionException.from(context, "Invalid ordinate: " + ordinate);
    }
    return ordinate.asDouble();
  }

  private static GeometryFactory getDefaultGeometryFactory() {
    return new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), DEFAULT_SRID);
  }
}
