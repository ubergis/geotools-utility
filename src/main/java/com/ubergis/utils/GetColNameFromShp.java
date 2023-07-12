package com.ubergis.utils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;


/**
 * 类 <code> GetColNameFromShp </code> 从shapefile文件中获取矢量数据的schema
 *
 */

public class GetColNameFromShp {

    private DataStore dataStore;
    private SimpleFeatureType schema;
    private static final String PATH = "xxx";
    private static final String FILTERTYPE = "include";

    /**
     *
     * @param path
     * @throws Exception
     *             连接一个shapefile path为指定文件的路径
     */
    private void connect(String path) throws Exception {

        /*
         * Get the shapefile, prompting for its path/name if this was not
         * provided on the command line
         */
        File file = promptShapeFile(path); // 读取shapefile文件
        Map<String, Serializable> connectParameters = new HashMap<String, Serializable>();
        connectParameters.put("url", file.toURI().toURL()); // 获取连接参数
        connectParameters.put("create spatial index", true);

        dataStore = DataStoreFinder.getDataStore(connectParameters);

        if (dataStore == null) {
            Logger.getLogger(GetColNameFromShp.class.getName()).log(
                    Level.WARNING,
                    "No DataStore found to handle" + file.getPath());
            System.exit(1);
        }
    }

    /**
     *
     * @return 将一个shapefile作为一个表返回，返回shp中的所有数据
     * @throws Exception
     *
     */
    public String[][] filterFeatures() throws Exception {

        connect(PATH);

        String typeNames[] = dataStore.getTypeNames();
        String typeName = typeNames[0];
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);

        Filter filter = CQL.toFilter(FILTERTYPE);
        SimpleFeatureCollection features = source.getFeatures(filter);

        schema = features.getSchema();
        List<AttributeType> types = schema.getTypes();

        Integer colNum = schema.getAttributeCount() + 1;
        Integer rowNum = features.size() + 2;

        // data的第一行存放属性的类型
        // data的第二行存放属性名
        // 之后每一行是数据
        String[][] data = new String[rowNum][colNum];

        data[0][0] = "class java.lang.String";
        data[1][0] = "FeatureIdentifer";

        // 每条记录的id
        for (int i = 2; i < rowNum; i++)
            data[i][0] = typeName + "." + (i - 1);

        // TODO:以下代码可以获取CRS
        // GeometryType t = null;
        // t.getCoordinateReferenceSystem();

        int tmp = 1;
        for (AttributeType type : types) {
            data[0][tmp] = type.getBinding().toString();
            data[1][tmp] = type.getName().toString();
            tmp++;
        }

        // 读取文件中的内容
        SimpleFeatureIterator feats = features.features();
        try {
            int i = 2;
            int j;
            while (feats.hasNext()) {
                j = 1;
                SimpleFeature f = feats.next();
                Object[] values = f.getAttributes().toArray();
                for (Object o : values) {
                    // System.out.println(o.toString());
                    data[i][j] = o.toString();
                    j++;
                }
                i++;
            }
        } finally {
            feats.close();
        }

        return data;
    }

    /**
     *
     * @return
     * @throws Exception
     *
     *             统计feature的数目
     */
    public Integer countFeatures() throws Exception {
        String typeNames[] = dataStore.getTypeNames();
        String typeName = typeNames[0];
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);

        Filter filter = CQL.toFilter(FILTERTYPE);
        SimpleFeatureCollection features = source.getFeatures(filter);

        int count = features.size();
        return count;
    }

    /**
     *
     * @param path
     * @return
     *
     *         读入一个shapefile文件 如果没有指定文件的路径，则创建 一个对话框来选在文件
     */
    private static File promptShapeFile(String path) {
        File file = null;

        // check if the filename was provided on the command line
        if (null != path) {
            file = new File(path);
            if (file.exists()) {
                return file;
            }

            // file didn't exist - see if the user wants to continue
            int rtnVal = JOptionPane.showConfirmDialog(null, "Can't find "
                            + file.getName() + ". Choose another ?", "Input shapefile",
                    JOptionPane.YES_NO_OPTION);
            if (rtnVal != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        // display a data store file chooser dialog for shapefiles
        return JFileDataStoreChooser.showOpenFile("shp", null);
    }

}