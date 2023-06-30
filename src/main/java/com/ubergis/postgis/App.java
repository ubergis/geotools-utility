package com.ubergis.postgis;

import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class App {
    public static void main(String[] args) throws IOException, FactoryException {
        PostgisDataStore postgisDataStore = new PostgisDataStore();
        postgisDataStore.setHost("127.0.0.1");
        postgisDataStore.setPort("5432");

        // 扩展数据库类型，读取postgis该参数类型设置为postgis
        // 其余设置对应的数据库类型，要求jdbc支持并且引入相关库
        postgisDataStore.setDbtype("postgis");
        postgisDataStore.setDatabase("postgisdb");
        postgisDataStore.setSchema("public");
        postgisDataStore.setUsername("postgres");
        postgisDataStore.setPassword("postgres");

        //=====================================重构后===========================================================
        Date date = new Date();
        File file = new File("D:\\Java\\ubergis\\geotools-utility\\src\\main\\resources\\a.geojson");
        Vector vector = new Vector();
        vector.setVectorid("123456790");
        vector.setVectorTableName("sw_cesium");
        System.out.println(vector.toString());
        PostgisUtility.getFieldsOfShp("D:\\Java\\ubergis\\geotools-utility\\src\\main\\resources\\test\\test.shp");
        long sec = new Date().getTime() - date.getTime();
        System.out.println(sec);
    }
}
