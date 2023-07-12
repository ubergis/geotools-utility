package com.ubergis.utils;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

/**
 * 类 <code> WKBUtils </code> 主要实现了一些对WKB常用处理的实现
 *
 */

public class WKBUtils {
    private static WKBWriter writer = new WKBWriter();
    private static WKBReader reader = new WKBReader();

    /**
     * 通过Geometry对象返回WKB
     *
     * @param geometry
     *            Geometry对象
     * @return WKB
     */
    public byte[] geomtry2WKB(Geometry geometry) {
        return writer.write(geometry);
    }

    /**
     * 将WKB转换成对应的Geometry对象
     *
     * @param WKBBytes
     *            WB
     * @return Geometry对象
     */
    public Geometry parseWKB(byte[] WKBBytes) {
        Geometry result = null;
        try {
            result = reader.read(WKBBytes);
        } catch (ParseException e) {
        }
        return result;
    }

}
