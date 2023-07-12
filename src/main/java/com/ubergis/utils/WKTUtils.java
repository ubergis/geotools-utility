package com.ubergis.utils;


import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class WKTUtils {
    private WKTReader reader;

    /**
     * 输入一个WKT的字符串，返回一个对应的Geometry对象
     *
     * @param WKTString
     *            WKT，以字符串表示
     * @return Geometry对象
     */
    public Geometry parseWKT(String WKTString) {
        Geometry result = null;
        reader = new WKTReader();
        try {
            result = reader.read(WKTString);
        } catch (ParseException e) {
        }
        return result;
    }
}