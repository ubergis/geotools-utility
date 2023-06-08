package com.ubergis.spatial;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.coverage.Coverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;

public class CommonMethod {
    public static void projTransForm() throws FactoryException, IOException {
        File file = new File("E:\\data\\shp\\single.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureType type = (SimpleFeatureType) featureSource.getSchema();
        // 源坐标
        CoordinateReferenceSystem sourceCRS = type.getCoordinateReferenceSystem();
        // 目标坐标
        //CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4490");
        // allow for some error due to different datums
        boolean lenient = true;
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);
        // 重新写文件
        // 获取到要素集合
        SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) featureSource.getFeatures();
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> create = new HashMap<>();
        File  newShpFile = new File("E:\\data\\shp\\resingle.shp");
        create.put("url", newShpFile.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore dataStore = dataStoreFactory.createNewDataStore(create);
        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(type, targetCRS);
        dataStore.createSchema(featureType);
        // Get the name of the new Shapefile, which will be used to open the FeatureWriter
        String createdName = dataStore.getTypeNames()[0];
        Transaction transaction = new DefaultTransaction("Reproject");
        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                     dataStore.getFeatureWriterAppend(createdName, transaction);
             SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                // copy the contents of each feature and transform the geometry
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes(feature.getAttributes());
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry geometry2 = JTS.transform(geometry, transform);
                copy.setDefaultGeometry(geometry2);
                writer.write();
            }
            transaction.commit();
            writer.close();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        } finally {

            transaction.close();
        }
    }
    public static SimpleFeatureType createType(Class<?> c, String layerName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setCRS(DefaultGeographicCRS.WGS84);
        builder.add("FID",String.class);
        builder.add("the_geom", c);
        // 设置了图层的名字
        builder.setName(layerName);
        SimpleFeatureType simpleFeatureType = builder.buildFeatureType();
        return simpleFeatureType;
    }
    public static void createShp(String shpPath, SimpleFeatureCollection collection) throws IOException {
        File shpFile = new File(shpPath);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        SimpleFeatureType simpleFeatureType = collection.getSchema();
        // 创造shpstore需要的参数
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(simpleFeatureType);
        Transaction transaction = new DefaultTransaction("create");
        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
        featureStore.setTransaction(transaction);
        featureStore.addFeatures(collection);
        featureStore.setTransaction(transaction);
        transaction.commit();
        transaction.close();
    }
    public static SimpleFeatureCollection readFeatureCollection(String shpPath) {
        SimpleFeatureCollection featureCollection = null;
        File shpFile = new File(shpPath);
        try {
            ShapefileDataStore shapefileDataStore = new ShapefileDataStore(shpFile.toURI().toURL());
            // 设置编码,防止属性的中文字符出现乱码
            shapefileDataStore.setCharset(Charset.forName("UTF-8"));
            // 这个typeNamae不传递，默认是文件名称
            FeatureSource featuresource = shapefileDataStore.getFeatureSource(shapefileDataStore.getTypeNames()[0]);
            featureCollection = (SimpleFeatureCollection) featuresource.getFeatures();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return featureCollection;
    }

    public static Coverage readTiff(String tiffPath) throws IOException {
        File f = new File(tiffPath);
        ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
        policy.setValue(OverviewPolicy.IGNORE);
        ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
        ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
        useJaiRead.setValue(true);
        GridCoverage2D image = new GeoTiffReader(f).read(new GeneralParameterValue[]{policy, gridsize, useJaiRead});
        return image;
    }

    public static Coverage clipImageToFeatureSource() throws IOException {
        SimpleFeatureCollection collection = CommonMethod.readFeatureCollection("E:\\data\\shp\\mask.shp");
        FeatureIterator<SimpleFeature> iterator = collection.features();
        List<Geometry> all = new ArrayList<Geometry>();
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                all.add(geometry);
            }
        } finally {
            if (iterator != null) {
                iterator.close();
            }
        }
        Coverage coverage = readTiff("G:\\xm\\xm2.tif");
        Coverage clippedCoverage = null;
        if (all.size() > 0) {
            CoverageProcessor processor = new CoverageProcessor();
            ParameterValueGroup params = processor.getOperation("CoverageCrop")
                    .getParameters();
            params.parameter("Source").setValue(coverage);
            GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);
            Geometry[] a = all.toArray(new Geometry[0]);
            GeometryCollection c = new GeometryCollection(a, factory);
            Envelope envelope = all.get(0).getEnvelopeInternal();
            double x1 = envelope.getMinX();
            double y1 = envelope.getMinY();
            double x2 = envelope.getMaxX();
            double y2 = envelope.getMaxY();
            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(x1,x2, y1, y2, coverage.getCoordinateReferenceSystem());
            params.parameter("ENVELOPE").setValue(referencedEnvelope);
            params.parameter("ROI").setValue(c);
            params.parameter("ForceMosaic").setValue(true);
            clippedCoverage = processor.doOperation(params);
        }
        if (all.size() == 0){
            System.out.println("Crop by shapefile requested but no simple features matched extent!");
        }
        return clippedCoverage;
    }

    public static boolean delete(File file){
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (!delete(file1)) return false;
            }
        }

        for (int i = 0; i < 10; i++){
            if (file.delete() || !file.exists()) return true;
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException ignored) {

            }
        }
        return false;
    }
}
