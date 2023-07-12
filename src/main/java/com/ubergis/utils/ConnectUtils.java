package com.ubergis.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.swing.JOptionPane;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.wizard.JWizard;

/**
 * 类<code> ConnectUtils </code> 提供了
 * 一些对shapefile文件的一些连接操作，包括
 * 对单个shapefile文件的连接，对存储有shapefile
 * 文件的postgis数据库的连接等
 *
 */

public class ConnectUtils {

    /**
     *
     * 读取一个.shp文件 输入参数为。 shp文件的路径 返回ShapefileDataStore
     *
     * @param path  .shp文件的路径
     * @param charSet  指定编码方式
     * @return ShapefileDataStore
     * @throws IOException
     */
    public static DataStore connect2Shp(String path, String charSet) throws IOException {

        File file = promptShapeFile(path); // 读取shapefile文件

        ShapefileDataStore shpDataStore = new ShapefileDataStore(file.toURI()
                .toURL());

        if(null != charSet)  //设定编码
            shpDataStore.setCharset(Charset.forName(charSet));

        return shpDataStore;
    }

    /**
     *
     * 连接到一个postGis数据库 使用图形UI填写参数 后面有需要可以改写 传入必须参数 去掉图形UI
     *
     * @return DataStore
     * @throws IOException
     */
    public static DataStore connect2Postgis() throws IOException {
        DataStore dataStore = null;

        JDataStoreWizard wizard = new JDataStoreWizard(
                new PostgisNGDataStoreFactory());
        int result = wizard.showModalDialog();
        if (result == JWizard.FINISH) {
            Map<String, Object> connectionParameters = wizard
                    .getConnectionParameters();
            dataStore = DataStoreFinder.getDataStore(connectionParameters);
            if (dataStore == null) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect - check parameters");
            }
        }
        return dataStore;
    }

    /**
     * 连接到一个dataStore 使用图形UI填写参数 后面有需要可以重新改写 传入必须参数去掉图形UI
     *
     * @return DataStore
     * @throws IOException
     */
    public static DataStore connect2DataStore() throws IOException {
        DataStore dataStore = null;

        JDataStoreWizard wizard = new JDataStoreWizard();
        int result = wizard.showModalDialog();
        if (result == JWizard.FINISH) {
            Map<String, Object> connectionParameters = wizard
                    .getConnectionParameters();
            dataStore = DataStoreFinder.getDataStore(connectionParameters);
            if (dataStore == null) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect - check parameters");
            }
        }
        return dataStore;
    }

    /**
     *
     * @param path
     * @return 读取一个文件 输入参数为文件的路径 如果文件不存在 弹出图形对话框来选在文件
     */
    private static File promptShapeFile(String path) {
        File file = null;

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