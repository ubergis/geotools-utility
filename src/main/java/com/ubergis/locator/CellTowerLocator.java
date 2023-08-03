package com.ubergis.locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellTowerLocator {
    private Map<Integer, CellTower> cellTowers; // 基站列表

    public CellTowerLocator() {
        cellTowers = new HashMap<>();
        // 添加基站信息
        cellTowers.put(1, new CellTower(1, 10, 20));
        cellTowers.put(2, new CellTower(2, 30, 40));
        cellTowers.put(3, new CellTower(3, 50, 60));
        // 可根据需要添加更多基站
    }

    public Location getLocation(List<SignalStrength> signalStrengths) {
        double weightedLatitudeSum = 0;
        double weightedLongitudeSum = 0;
        double totalWeight = 0;

        for (SignalStrength signalStrength : signalStrengths) {
            int cellTowerId = signalStrength.getCellTowerId();
            if (cellTowers.containsKey(cellTowerId)) {
                CellTower cellTower = cellTowers.get(cellTowerId);
                double weight = 1 / signalStrength.getDistance(); // 根据信号强度的倒数作为权重
                weightedLatitudeSum += cellTower.getLatitude() * weight;
                weightedLongitudeSum += cellTower.getLongitude() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight > 0) {
            double averageLatitude = weightedLatitudeSum / totalWeight;
            double averageLongitude = weightedLongitudeSum / totalWeight;
            return new Location(averageLatitude, averageLongitude);
        } else {
            return null; // 无法确定位置
        }
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

    // 信号强度类
    private static class SignalStrength {
        private int cellTowerId;
        private double distance;

        public SignalStrength(int cellTowerId, double distance) {
            this.cellTowerId = cellTowerId;
            this.distance = distance;
        }

        public int getCellTowerId() {
            return cellTowerId;
        }

        public double getDistance() {
            return distance;
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
        CellTowerLocator locator = new CellTowerLocator();

        List<SignalStrength> signalStrengths = new ArrayList<>();
        signalStrengths.add(new SignalStrength(1, 0.1));
        signalStrengths.add(new SignalStrength(2, 0.05));
        signalStrengths.add(new SignalStrength(3, 0.2));

        Location location = locator.getLocation(signalStrengths);
        if (location != null) {
            System.out.println("手机位置：纬度=" + location.getLatitude() + ", 经度=" + location.getLongitude());
        } else {
            System.out.println("无法确定手机位置");
        }
    }
}

