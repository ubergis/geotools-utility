package com.ubergis.spatial;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.util.NullProgressListener;
import org.geotools.feature.collection.ClippedFeatureCollection;
import org.geotools.process.vector.*;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalysisGPMethod {
    // gp 裁剪
    public static void clip() throws IOException, ParseException {
        SimpleFeatureCollection featureCollection = CommonMethod.readFeatureCollection("E:\\data\\bf.shp");
        WKTReader wktReader = new WKTReader();
        Geometry geometry = wktReader.read("MultiPolygon (((120.06170797626219837 30.54693549152121079, 120.12450974119508373 30.54442342092389495, 120.13644207653233309 30.49192114543999921, 120.06723453157628967 30.48878105719335707, 120.06170797626219837 30.54693549152121079)))");
        ClippedFeatureCollection clippedFeatureCollection = new ClippedFeatureCollection(featureCollection,geometry, false);
        CommonMethod.createShp("E:\\data\\clip.shp",clippedFeatureCollection);
    }
    // gp 缓冲区
    public static void buffer() throws IOException {
        BufferFeatureCollection bf = new BufferFeatureCollection();
        SimpleFeatureCollection featureCollection = CommonMethod.readFeatureCollection("E:\\data\\bf.shp");
        SimpleFeatureCollection simpleFeatureCollection = bf.execute(featureCollection, 000.1d, "czzb");
        CommonMethod.delete(new File("E:\\data\\afterBuffer.shp"));
        CommonMethod.createShp("E:\\data\\afterBuffer.shp",simpleFeatureCollection);
    }
    // gp 合并图层
    public static void union() throws ClassNotFoundException, IOException {
        UnionFeatureCollection unionFeatureCollection = new UnionFeatureCollection();
        SimpleFeatureCollection first = CommonMethod.readFeatureCollection("E:\\data\\bf.shp");
        SimpleFeatureCollection second = CommonMethod.readFeatureCollection("E:\\data\\hardData.shp");
        SimpleFeatureCollection result = unionFeatureCollection.execute(first, second);
        CommonMethod.createShp("E:\\data\\union.shp", result);
    }
    // gp 分组聚合属性 这个非常好用
    public static void aggregate() throws IOException {
        AggregateProcess aggregateProcess = new AggregateProcess();
        SimpleFeatureCollection first = CommonMethod.readFeatureCollection("E:\\data\\bf.shp");
        Set<AggregateProcess.AggregationFunction> functions = new HashSet<>();
        // 要统计的类型
        functions.add(AggregateProcess.AggregationFunction.Average);
        functions.add(AggregateProcess.AggregationFunction.Sum);
        functions.add(AggregateProcess.AggregationFunction.Max);
        // 分组字段列表
        List<String> groupFields = new ArrayList<>();
        groupFields.add("dlmc");
        groupFields.add("zldwdm");
        AggregateProcess.Results execute = aggregateProcess.execute(first,"shape_leng", functions, false,groupFields,new NullProgressListener());
    }
    // gp 简化几何, 它的源码使用道格拉斯算法实现的
    public static void simplify() throws Exception {
        SimplifyProcess simplifyProcess = new SimplifyProcess();
        SimpleFeatureCollection simpleFeatureCollection = CommonMethod.readFeatureCollection("E:\\data\\hardData.shp");
        SimpleFeatureCollection result = simplifyProcess.execute(simpleFeatureCollection, 0.00001, true);
        CommonMethod.createShp("E:\\data\\simpfy.shp", result);
    }
    // gp 投影变换
    public void reproject() throws Exception {
        ReprojectProcess reprojectProcess = new ReprojectProcess();
        SimpleFeatureCollection simpleFeatureCollection = CommonMethod.readFeatureCollection("E:\\data\\hardData.shp");
        SimpleFeatureCollection projectCollection = reprojectProcess.execute(simpleFeatureCollection, CRS.decode("EPSG:4490", true), CRS.decode("EPSG:4549", true));
        CommonMethod.createShp("E:\\data\\simpfy.shp", projectCollection);
    }
    // gp 相交
    public static void intersect() throws ClassNotFoundException, IOException {
        IntersectionFeatureCollection intersectionFeatureCollection = new IntersectionFeatureCollection();
        SimpleFeatureCollection first = CommonMethod.readFeatureCollection("E:\\data\\bf.shp");
        SimpleFeatureCollection second = CommonMethod.readFeatureCollection("E:\\data\\hardData.shp");
        // 要保留的字段，我这里为了方便都是用的同一份字段
        List<String> fretain = new ArrayList<>();
        fretain.add("id");
        fretain.add("mj");
        fretain.add("dlmc");
        SimpleFeatureCollection result = intersectionFeatureCollection.execute(first, second,fretain, fretain, IntersectionFeatureCollection.IntersectionMode.INTERSECTION,true, true);
        CommonMethod.createShp("E:\\data\\intersect.shp", result);
    }

}
