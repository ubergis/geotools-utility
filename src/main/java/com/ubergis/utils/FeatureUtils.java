package com.ubergis.utils;

import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;


/**
 * 类 <code> FeatureUtil </code> 实现了 对shapefile矢量数据中feature的相关操作，
 * 包括统计一个dataStore中feature的总数、 返回一个dataStore中所有的feature
 *
 */

public class FeatureUtils {


    /**
     * 传入一个DataStore对象和过滤条件filterType 返回该对象中满足该条件的feature的数目
     * 若filterType为null，则统计所有的feature的数目
     *
     * @param dataStore
     *            矢量数据源，可以是一个shapefile文件，也可以是一个postgis DB
     * @param filterType
     *            CQL过滤的类型，以字符串表示，“include”表示不进行过滤，将所有内容都查询出来
     * @return 满足条件的feature的数目
     * @throws Exception
     *
     */
    public static Integer countFeatures(DataStore dataStore, String filterType)
            throws Exception {
        String typeNames[] = dataStore.getTypeNames();
        String typeName = typeNames[0];
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);

        Filter filter = null;
        if (null == filterType)
            filter = CQL.toFilter("include"); // include表示返回所有
        else
            filter = CQL.toFilter(filterType);

        SimpleFeatureCollection features = source.getFeatures(filter);

        int count = features.size();
        return count;
    }

    /**
     * 从一个shapefile中提取矢量数据
     *
     * @param dataStore
     *            矢量数据源，可以是一个shapefile文件，也可以是一个postgis DB
     * @param filterType
     *            CQL过滤的类型，以字符串表示，“include”表示不进行过滤，将所有内容都查询出来
     * @param featureIdStart
     *            起始的特征id，与参数featureSize组合使用，作用相当于分页。在shapefile中，featureid的编号
     *            从1开始
     * @param featureSize
     *            选择返回的特征的数目
     * @return 获取dataStore中满足过滤条件的feature的数据 输入为dataStore和filterType。以一
     *         个二维表的形式返回 。一个shapefile文件读出来之后，放在String data[][]的形式为
     *
     *         <pre>
     *         -------------+-------------+--------------+---------------------
     *         class String | class Point | class String | class Integer
     *         -------------+-------------+--------------+---------------------
     *         FeatureId    | Point       | name         | number
     *         -------------+-------------+--------------+---------------------
     *         fid.1        | POINT(x, y) | name1        | number1
     *         -------------+-------------+--------------+---------------------
     *         fid.2        | POINT(a, b) | name2        | number2
     *         -------------+-------------+--------------+---------------------
     *
     *         -------------+-------------+--------------+---------------------
     *         fid.n        | POINT(m, n) | namen        | numbern
     *         -------------+-------------+--------------+---------------------
     * </pre>
     *
     *         第二行表示shapefile中每条记录有哪些字段（属性名），第一行是这些字段属性在java
     *         中对应的java类，从第三行开始时就是每条记录的值。data存入HBase中，可能就是n-1列， 其中第二列作为column
     *         families 的 column
     *         qualifier。featureIdStart和featureSize组合可以实现分页解析提取，featureIdStart应该
     *         不小于1，如果传入的参数featureIdStart < 1，系统会自动置为1。这两个参数都可以为null，取值的组合情况为：
     *
     *         <pre>
     *         --------------+-------------+-----------------------------------------
     *         featureIdStart|featureSize  | 功能描述
     *         --------------+-------------+-----------------------------------------
     *         null          |null         | 将这个shapefile中的feature全部解析
     *         --------------+-------------+-----------------------------------------
     *         null          |n            | 解析并取出前n个feature
     *         --------------+-------------+-----------------------------------------
     *         m             |null         | 解析并取出从第m个之后的所有feature（包括第m）
     *         --------------+-------------+-----------------------------------------
     *         m             |n            | 解析并取出从第m个之后的n个feature（包括第m）
     *         --------------+-------------+-----------------------------------------
     * </pre>
     *
     * @throws Exception
     *
     */

    public static String[][] getFeatures(DataStore dataStore,
                                         String filterType, Integer featureIdStart, Integer featureSize)
            throws Exception {

        SimpleFeatureType schema;
        String typeNames[] = dataStore.getTypeNames();
        String typeName = typeNames[0];
        String classPrefix = "class com.vividsolutions.jts.geom.";
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
        String[][] data = null;

        Integer startId = 1; // 起始feature id赋初值为1

        // 获得这个shapefile中满足filterType的feature数目
        Integer featureNum = countFeatures(dataStore, filterType);

        startId = (null == featureIdStart) ? 1 : featureIdStart;
        if (startId < 1) {
            startId = 1;
        }

        if (null == featureSize) {
            featureSize = featureNum - startId + 1;
        }

        Filter filter = null;
        if (null == filterType)
            filter = CQL.toFilter("include"); // include表示返回所有
        else
            filter = CQL.toFilter(filterType);
        SimpleFeatureCollection features = source.getFeatures(filter);
        SimpleFeatureIterator feats = features.features(); // 获得feature的迭代器
        SimpleFeature feat = null;

        schema = features.getSchema();
        List<AttributeType> types = schema.getTypes();

        // 设置返回结果的列数目（列数数确定的）
        Integer colNum = schema.getAttributeCount() + 1;

        // 设置返回结果的行数（行数是不确定的）
        Integer rowNum = 2; // 先赋初值2

        if (featureNum < startId) { // feature的总数目比startid还小的情况

            rowNum = 2; // 只返回前两行
            data = new String[rowNum][colNum];
        } else {// feature的总数目比>=startid
            if (featureNum >= featureSize + startId - 1) {
                rowNum = featureSize + 2;
                data = new String[rowNum][colNum];
            } else {
                rowNum = (featureNum - startId + 1) + 2;
                data = new String[rowNum][colNum];
            }

            // 跳过startId - 1个feature，到startId这个feature
            for (int i = 1; i < startId; i++) {
                if (feats.hasNext())
                    feat = feats.next();
                else {
                    return null;
                }
            }
        }

        // 对data进行赋值
        data[0][0] = "class java.lang.String";
        data[1][0] = "FeatureIdentifer";

        // 每条记录的id
        for (int i = 2; i < rowNum; i++)
            data[i][0] = typeName + "." + ((i - 1) + (startId - 1));
        // TODO
        // 这里的记录id，要求：如果多次对一个shapefile文件操作，并且存到
        // HBase中的同一个表，需要每次操作的filterType相同，否则两个不同
        // 的feature记录会在Hbase中存成同一个rowkey，不过在绝大多数操作的
        // filterType都是include，其他的filterType可能也不会在这里用到

        // 把data的前两行填充：java类名和各自段属性名
        int tmp = 1;
        for (AttributeType type : types) {
            data[0][tmp] = type.getBinding().toString();
            data[1][tmp] = type.getName().toString();
            if (data[0][tmp].startsWith(classPrefix))
                data[0][0] = tmp + ""; // data[0][0]记录了矢量数据的列的下标，到时候方便以WKB形式存储
            tmp++;
        }

        // TODO:以下代码可以获取CRS
        // 需要时可以把CRS也写入HBase，返回的数据多加一列
        // GeometryType t = null;
        // t.getCoordinateReferenceSystem();

        // 读取shapefile中指定范围的feature并返回结果
        try {
            int i = 2;
            while (feats.hasNext()) {
                int j = 1;
                feat = feats.next();
                Object[] values = feat.getAttributes().toArray();
                for (Object o : values) {
                    data[i][j] = o.toString();
                    j++;
                }
                i++;
                if (i >= rowNum)
                    break;
            }
        } finally {
            feats.close();
        }

        return data;
    }

}
