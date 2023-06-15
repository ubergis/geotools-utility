package com.ubergis.spatial;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.ClippedFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AnalysisMethod {
    public static void clip() {
        SimpleFeatureCollection china = CommonMethod.readFeatureCollection("E:\\data\\shp\\china.shp");
        SimpleFeatureCollection countries = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries.shp");
        SimpleFeature next = china.features().next();
        Geometry geometry = (Geometry) next.getDefaultGeometry();
        ClippedFeatureCollection clippedFeatureCollection = new ClippedFeatureCollection(countries, geometry, true);
        SimpleFeatureIterator clipedFeatures = clippedFeatureCollection.features();
        int gcount = 0;
        while (clipedFeatures.hasNext()) {
            SimpleFeature feature = clipedFeatures.next();
            Collection<Property> properties = feature.getProperties();
            Iterator<Property> iterator = properties.iterator();
            while (iterator.hasNext()) {
                Property property = iterator.next();
                System.out.println(property.getName() + "  " + property.getValue());
            }
            gcount ++;
        }
        System.out.println("裁剪后还剩下的元素！" + gcount);
    }

    public static void buffer() throws IOException, FactoryException {
        SimpleFeatureCollection cities = CommonMethod.readFeatureCollection("E:\\data\\shp\\cities.shp");
        List<SimpleFeature> bufferResult = new ArrayList<>();
        SimpleFeatureIterator iterator = cities.features();
        SimpleFeatureType type = CommonMethod.createType(Polygon.class, "citesBuffer");
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
        while (iterator.hasNext()) {
            SimpleFeature simpleFeature = iterator.next();
            Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
            Geometry buffer = geometry.buffer(10);
            featureBuilder.add("1");
            featureBuilder.add(buffer);
            SimpleFeature bufferSimpleFeature = featureBuilder.buildFeature(null);
            bufferResult.add(bufferSimpleFeature);
        }
        SimpleFeatureCollection collection = new ListFeatureCollection(type, bufferResult);
        CommonMethod.createShp("E:\\data\\shp\\citiesBuffer.shp", collection);
    }

    public static void union() throws IOException {
        SimpleFeatureCollection featureCollectionP1 = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries_part1.shp");
        SimpleFeatureCollection featureCollectionP2 = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries_part2.shp");
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureIterator iterator1 = featureCollectionP1.features();
        SimpleFeatureIterator iterator2 = featureCollectionP2.features();
        while (iterator1.hasNext()) {
            SimpleFeature simpleFeature = iterator1.next();
            features.add(simpleFeature);
        }
        while (iterator2.hasNext()) {
            SimpleFeature simpleFeature = iterator2.next();
            features.add(simpleFeature);
        }
        SimpleFeatureCollection collection = new ListFeatureCollection(featureCollectionP1.getSchema(), features);
        CommonMethod.createShp("E:\\data\\shp\\countries_union.shp", collection);
    }

    public static void merge() throws IOException {
        SimpleFeatureCollection collection = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries_mergedata.shp");
        SimpleFeatureIterator features = collection.features();
        List<Polygon> polygons = new ArrayList<>();
        while (features.hasNext()) {
            SimpleFeature simpleFeature = features.next();
            Geometry defaultGeometry = (Geometry) simpleFeature.getDefaultGeometry();
            Geometry union = defaultGeometry.union();
            polygons.add((Polygon) union);
        }
        Polygon[] ps = polygons.toArray(new Polygon[polygons.size()]);
        MultiPolygon multiPolygon = new MultiPolygon(ps, new GeometryFactory());
        Geometry union = multiPolygon.union();
        SimpleFeatureType type = CommonMethod.createType(MultiPolygon.class, "countriesMerge");
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        builder.add("1");
        builder.add(union);
        SimpleFeature simpleFeature = builder.buildFeature(null);
        List<SimpleFeature> featureList = new ArrayList<>();
        featureList.add(simpleFeature);
        SimpleFeatureCollection simpleFeatureCollection = new ListFeatureCollection(type, featureList);
        // 生成矢量数据
        CommonMethod.createShp("E:\\data\\shp\\countriesMerge.shp", simpleFeatureCollection);
    }

    public static void erase() throws IOException {
        SimpleFeatureCollection subCollection = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries_differenceData.shp");
        SimpleFeatureCollection collection = CommonMethod.readFeatureCollection("E:\\data\\shp\\countriesMerge.shp");
        SimpleFeatureIterator subFeatures = subCollection.features();
        SimpleFeatureIterator features = collection.features();
        Geometry subGeometry = null;
        while (subFeatures.hasNext()) {
            SimpleFeature simpleFeature = subFeatures.next();
            subGeometry = (Geometry) simpleFeature.getDefaultGeometry();
        }
        Geometry geometry = null;
        while (features.hasNext()) {
            SimpleFeature simpleFeature = features.next();
            geometry = (Geometry) simpleFeature.getDefaultGeometry();
        }
        Geometry difference = geometry.difference(subGeometry);
        SimpleFeatureType type = CommonMethod.createType(MultiPolygon.class, "countriesDifference");
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        builder.add("1");
        builder.add(difference);
        SimpleFeature simpleFeature = builder.buildFeature(null);
        List<SimpleFeature> featureList = new ArrayList<>();
        featureList.add(simpleFeature);
        SimpleFeatureCollection simpleFeatureCollection = new ListFeatureCollection(type, featureList);
        // 生成矢量数据
        CommonMethod.createShp("E:\\data\\shp\\countriesDifference.shp", simpleFeatureCollection);
    }

    public static void intersect() throws IOException {
        SimpleFeatureCollection intersectCollection = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries_intersect.shp");
        SimpleFeatureCollection countries = CommonMethod.readFeatureCollection("E:\\data\\shp\\countries.shp");
        SimpleFeatureIterator features = intersectCollection.features();
        SimpleFeatureIterator countriesFeatures = countries.features();
        List<Polygon> polygons = new ArrayList<Polygon>();
        Geometry other = null;
        while (features.hasNext()) {
            SimpleFeature next = features.next();
            other = (Geometry) next.getDefaultGeometry();
            other =  other.buffer(0);
        }
        List<Geometry> geometries = new ArrayList<>();
        // 一个一个求交集，合并成一个大图层求交集会报错，还不清楚什么原因
        while (countriesFeatures.hasNext()) {
            SimpleFeature next = countriesFeatures.next();
            Geometry defaultGeometry = (Geometry) next.getDefaultGeometry();
            if (defaultGeometry instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) defaultGeometry;
                int numGeometries = multiPolygon.getNumGeometries();
                for (int i = 0; i < numGeometries; i ++) {
                    Geometry geometryN = multiPolygon.getGeometryN(i);
                    boolean valid = geometryN.isValid();
                    System.out.println("======>" + valid);
                    polygons.add((Polygon) multiPolygon.getGeometryN(i));
                    try {
                        Geometry intersection = other.intersection(geometryN);
                        geometries.add(intersection);
                    } catch (Exception e) {
                        Property fid = next.getProperty("FID");
                        System.out.println(fid.getValue());
                    }

                }
            } else  {
                Geometry union = defaultGeometry.union();
                polygons.add((Polygon) union);
            }
        }
        SimpleFeatureType type = CommonMethod.createType(MultiPolygon.class, "countriesIntersection");
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        List<SimpleFeature> featureList = new ArrayList<>();
        for (int i = 0; i < geometries.size(); i ++) {
            builder.add("1");
            builder.add(geometries.get(i));
            SimpleFeature simpleFeature = builder.buildFeature(null);

            featureList.add(simpleFeature);
        }
        SimpleFeatureCollection simpleFeatureCollection = new ListFeatureCollection(type, featureList);
        // 生成矢量数据
        CommonMethod.createShp("E:\\data\\shp\\countriesIntersection.shp", simpleFeatureCollection);
    }
}
