package io.github.aytronnfr.jackson.jts.support;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeometryFixtures {
  private GeometryFixtures() {}

  public static GeometryFactory defaultFactory() {
    return new GeometryFactory(new PrecisionModel(), 4326);
  }

  public static Geometry[] sampleGeometries(GeometryFactory factory) {
    return new Geometry[] {
        factory.createPoint(new Coordinate(1.234567891, 2.345678912)),
        factory.createLineString(new Coordinate[] {
            new Coordinate(0, 0), new Coordinate(1.1, 2.2)
        }),
        factory.createPolygon(new Coordinate[] {
            new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)
        }),
        factory.createMultiPointFromCoords(new Coordinate[] {
            new Coordinate(1, 1), new Coordinate(2, 2)
        }),
        factory.createMultiLineString(new LineString[] {
            factory.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 0)}),
            factory.createLineString(new Coordinate[] {new Coordinate(0, 1), new Coordinate(1, 1)})
        }),
        factory.createMultiPolygon(new Polygon[] {
            factory.createPolygon(new Coordinate[] {
                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)
            }),
            factory.createPolygon(new Coordinate[] {
                new Coordinate(2, 2), new Coordinate(3, 2), new Coordinate(2, 3), new Coordinate(2, 2)
            })
        }),
        factory.createGeometryCollection(new Geometry[] {
            factory.createPoint(new Coordinate(5, 6)),
            factory.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(2, 2)})
        })
    };
  }

  public static String geometryCollectionJson() {
    return "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[5,6]},{\"type\":\"LineString\",\"coordinates\":[[0,0],[2,2]]}]}";
  }
}
