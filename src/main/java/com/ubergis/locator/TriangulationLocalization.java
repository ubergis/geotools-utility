package com.ubergis.locator;

import java.util.HashMap;
import java.util.Map;

public class TriangulationLocalization {
    private Map<Integer, CellTower> cellTowers; // 基站列表

    public TriangulationLocalization() {
        cellTowers = new HashMap<>();
        // 添加基站信息
        cellTowers.put(1, new CellTower(1, 10, 20));
        cellTowers.put(2, new CellTower(2, 30, 40));
        cellTowers.put(3, new CellTower(3, 50, 60));
        // 可根据需要添加更多基站
    }

    public Location getLocation(Map<Integer, Double> distances) {
        double latitudeSum = 0;
        double longitudeSum = 0;

        for (Map.Entry<Integer, Double> entry : distances.entrySet()) {
            int cellTowerId = entry.getKey();
            if (cellTowers.containsKey(cellTowerId)) {
                CellTower cellTower = cellTowers.get(cellTowerId);
                double distance = entry.getValue();
                double latitude = cellTower.getLatitude();
                double longitude = cellTower.getLongitude();
                double radius = distance; // 假设基站和手机之间的距离即为半径
                double[] point = calculateCoordinates(latitude, longitude, radius);
                latitudeSum += point[0];
                longitudeSum += point[1];
            }
        }

        int numTowers = distances.size();
        if (numTowers > 0) {
            double averageLatitude = latitudeSum / numTowers;
            double averageLongitude = longitudeSum / numTowers;
            return new Location(averageLatitude, averageLongitude);
        } else {
            return null; // 无法确定位置
        }
    }

    // 根据基站经纬度和距离计算手机的经纬度
    private double[] calculateCoordinates(double latitude, double longitude, double distance) {
        // 简化处理，这里仅使用了直角坐标系
        double latitudeOffset = distance / 111000.0;
        double longitudeOffset = distance / (111000.0 * Math.cos(Math.toRadians(latitude)));
        double newLatitude = latitude + latitudeOffset;
        double newLongitude = longitude + longitudeOffset;
        return new double[] { newLatitude, newLongitude };
    }

    // 基站类
    private static class CellTower {
        private int id;
        private double latitude;
        private double longitude;

        public CellTower(int id, double latitude, double longitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public int getId() {
            return id;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    // 位置类
    private static class Location {
        private double latitude;
        private double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    // 测试示例
    public static void main(String[] args) {
        TriangulationLocalization localization = new TriangulationLocalization();

        Map<Integer, Double> distances = new HashMap<>();
        distances.put(1, 1000.0); // 基站1到手机的距离为1000米
        distances.put(2, 1500.0); // 基站2到手机的距离为1500米
        distances.put(3, 2000.0); // 基站3到手机的距离为2000米

        Location location = localization.getLocation(distances);
        if (location != null) {
            System.out.println("手机位置：纬度=" + location.getLatitude() + ", 经度=" + location.getLongitude());
        } else {
            System.out.println("无法确定手机位置");
        }
    }
}

