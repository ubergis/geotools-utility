package com.ubergis.locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HybridLocalization2 {
    private Map<Integer, CellTower> cellTowers; // 基站列表
    private double transitionProbability; // 状态转移概率
    private double measurementProbability; // 测量概率

    public HybridLocalization2() {
        cellTowers = new HashMap<>();
        // 添加基站信息
        cellTowers.put(1, new CellTower(1, 10, 20));
        cellTowers.put(2, new CellTower(2, 30, 40));
        cellTowers.put(3, new CellTower(3, 50, 60));
        // 可根据需要添加更多基站

        transitionProbability = 0.8; // 状态转移概率
        measurementProbability = 0.9; // 测量概率
    }

    public Location getLocation(List<SignalStrength> signalStrengths, Map<Integer, Double> distances) {
        Location triangulationLocation = performTriangulation(distances);
        Location viterbiLocation = performViterbi(signalStrengths);

        if (triangulationLocation != null && viterbiLocation != null) {
            // 综合两种算法的结果
            double latitude = (triangulationLocation.getLatitude() + viterbiLocation.getLatitude()) / 2;
            double longitude = (triangulationLocation.getLongitude() + viterbiLocation.getLongitude()) / 2;
            return new Location(latitude, longitude);
        } else if (triangulationLocation != null) {
            return triangulationLocation;
        } else if (viterbiLocation != null) {
            return viterbiLocation;
        } else {
            return null; // 无法确定位置
        }
    }

    // 执行三角测量定位算法
    private Location performTriangulation(Map<Integer, Double> distances) {
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

    // 执行维特比定位算法
    private Location performViterbi(List<SignalStrength> signalStrengths) {
        int numStates = cellTowers.size();
        int numObservations = signalStrengths.size();

        double[][] dp = new double[numStates][numObservations];
        int[][] path = new int[numStates][numObservations];

        // 初始化第一列
        for (int i = 0; i < numStates; i++) {
            CellTower cellTower = cellTowers.get(i + 1);
            double initialProbability = 1.0 / numStates; // 假设所有状态的初始概率相等
            dp[i][0] = initialProbability * measurementProbability;
            path[i][0] = i;
        }

        // 递推计算
        for (int j = 1; j < numObservations; j++) {
            for (int i = 0; i < numStates; i++) {
                double maxProbability = -1;
                int maxState = -1;

                for (int k = 0; k < numStates; k++) {
                    double transition = dp[k][j - 1] * transitionProbability;
                    CellTower cellTower = cellTowers.get(i + 1);
                    double measurement = transition * measurementProbability;
                    double probability = transition * measurement;

                    if (probability > maxProbability) {
                        maxProbability = probability;
                        maxState = k;
                    }
                }

                dp[i][j] = maxProbability;
                path[i][j] = maxState;
            }
        }

        // 回溯找到最大概率路径
        double maxProbability = -1;
        int maxState = -1;
        for (int i = 0; i < numStates; i++) {
            if (dp[i][numObservations - 1] > maxProbability) {
                maxProbability = dp[i][numObservations - 1];
                maxState = i;
            }
        }

        List<Integer> stateSequence = new ArrayList<>();
        stateSequence.add(maxState);
        for (int j = numObservations - 1; j > 0; j--) {
            maxState = path[maxState][j];
            stateSequence.add(maxState);
        }

        // 计算最终位置
        double weightedLatitudeSum = 0;
        double weightedLongitudeSum = 0;
        double totalWeight = 0;

        for (int i = stateSequence.size() - 1; i >= 0; i--) {
            int state = stateSequence.get(i);
            CellTower cellTower = cellTowers.get(state + 1);
            double weight = 1 / dp[state][numObservations - 1]; // 根据维特比概率的倒数作为权重
            weightedLatitudeSum += cellTower.getLatitude() * weight;
            weightedLongitudeSum += cellTower.getLongitude() * weight;
            totalWeight += weight;
        }

        if (totalWeight > 0) {
            double averageLatitude = weightedLatitudeSum / totalWeight;
            double averageLongitude = weightedLongitudeSum / totalWeight;
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
        double latitude;
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
        HybridLocalization localization = new HybridLocalization();

        // 三角测量信息
        Map<Integer, Double> distances = new HashMap<>();
        distances.put(1, 1000.0); // 基站1到手机的距离为1000米
        distances.put(2, 1500.0); // 基站2到手机的距离为1500米
        distances.put(3, 2000.0); // 基站3到手机的距离为2000米

        // 维特比算法信息
        List<SignalStrength> signalStrengths = new ArrayList<>();
        signalStrengths.add(new SignalStrength(1, 0.1));
        signalStrengths.add(new SignalStrength(2, 0.05));
        signalStrengths.add(new SignalStrength(3, 0.2));

        Location location = localization.getLocation(signalStrengths, distances);
        if (location != null) {
            System.out.println("手机位置：纬度=" + location.getLatitude() + ", 经度=" + location.getLongitude());
        } else {
            System.out.println("无法确定手机位置");
        }
    }
}
