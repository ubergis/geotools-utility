package com.ubergis.locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HybridLocalization {
    private Map<Integer, CellTower> cellTowers; // 基站列表

    public HybridLocalization() {
        cellTowers = new HashMap<>();
        // 添加基站信息
        cellTowers.put(1, new CellTower(1, 10, 20));
        cellTowers.put(2, new CellTower(2, 30, 40));
        cellTowers.put(3, new CellTower(3, 50, 60));
        // 可根据需要添加更多基站
    }

    public Location getLocation(List<Integer> observedSequence, List<Double> signalStrengths) {
        // 基于三角测量的定位
        Location triangulationLocation = triangulationLocalization(observedSequence);

        // 基于维特比算法的定位
        Location viterbiLocation = viterbiLocalization(signalStrengths);

        // 综合两种算法的结果
        if (triangulationLocation != null && viterbiLocation != null) {
            double averageLatitude = (triangulationLocation.getLatitude() + viterbiLocation.getLatitude()) / 2;
            double averageLongitude = (triangulationLocation.getLongitude() + viterbiLocation.getLongitude()) / 2;
            return new Location(averageLatitude, averageLongitude);
        } else if (triangulationLocation != null) {
            return triangulationLocation;
        } else if (viterbiLocation != null) {
            return viterbiLocation;
        } else {
            return null; // 无法确定位置
        }
    }

    // 基于三角测量的定位
    private Location triangulationLocalization(List<Integer> observedSequence) {
        double latitudeSum = 0;
        double longitudeSum = 0;

        for (int cellTowerId : observedSequence) {
            if (cellTowers.containsKey(cellTowerId)) {
                CellTower cellTower = cellTowers.get(cellTowerId);
                double latitude = cellTower.getLatitude();
                double longitude = cellTower.getLongitude();
                latitudeSum += latitude;
                longitudeSum += longitude;
            }
        }

        int numTowers = observedSequence.size();
        if (numTowers > 0) {
            double averageLatitude = latitudeSum / numTowers;
            double averageLongitude = longitudeSum / numTowers;
            return new Location(averageLatitude, averageLongitude);
        } else {
            return null; // 无法确定位置
        }
    }

    // 基于维特比算法的定位
    private Location viterbiLocalization(List<Double> signalStrengths) {
        int numStates = cellTowers.size();
        int numObservations = signalStrengths.size();

        double[][] dp = new double[numStates][numObservations];
        int[][] path = new int[numStates][numObservations];

        // 初始化第一列
        for (int i = 0; i < numStates; i++) {
            dp[i][0] = signalStrengths.get(0);
            path[i][0] = i;
        }

        // 递推计算
        for (int j = 1; j < numObservations; j++) {
            for (int i = 0; i < numStates; i++) {
                double maxProbability = -1;
                int maxState = -1;

                for (int k = 0; k < numStates; k++) {
                    double transition = dp[k][j - 1];
                    double measurement = signalStrengths.get(j);
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
        double latitudeSum = 0;
        double longitudeSum = 0;

        for (int i = stateSequence.size() - 1; i >= 0; i--) {
            int state = stateSequence.get(i);
            CellTower cellTower = cellTowers.get(state + 1);
            double latitude = cellTower.getLatitude();
            double longitude = cellTower.getLongitude();
            latitudeSum += latitude;
            longitudeSum += longitude;
        }

        int numStatesInPath = stateSequence.size();
        if (numStatesInPath > 0) {
            double averageLatitude = latitudeSum / numStatesInPath;
            double averageLongitude = longitudeSum / numStatesInPath;
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

        List<Integer> observedSequence = new ArrayList<>();
        observedSequence.add(1);
        observedSequence.add(2);
        observedSequence.add(3);

        List<Double> signalStrengths = new ArrayList<>();
        signalStrengths.add(0.9);
        signalStrengths.add(0.8);
        signalStrengths.add(0.7);

        Location location = localization.getLocation(observedSequence, signalStrengths);
        if (location != null) {
            System.out.println("手机位置：纬度=" + location.getLatitude() + ", 经度=" + location.getLongitude());
        } else {
            System.out.println("无法确定手机位置");
        }
    }
}