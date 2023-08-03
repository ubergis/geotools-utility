package com.ubergis.locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViterbiLocalization {
    private Map<Integer, CellTower> cellTowers; // 基站列表
    private double transitionProbability; // 状态转移概率
    private double measurementProbability; // 测量概率

    public ViterbiLocalization() {
        cellTowers = new HashMap<>();
        // 添加基站信息
        cellTowers.put(1, new CellTower(1, 10, 20));
        cellTowers.put(2, new CellTower(2, 30, 40));
        cellTowers.put(3, new CellTower(3, 50, 60));
        // 可根据需要添加更多基站

        transitionProbability = 0.8; // 状态转移概率
        measurementProbability = 0.9; // 测量概率
    }

    public Location getLocation(List<Integer> observedSequence) {
        int numStates = cellTowers.size();
        int numObservations = observedSequence.size();

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
        ViterbiLocalization localization = new ViterbiLocalization();

        List<Integer> observedSequence = new ArrayList<>();
        observedSequence.add(1);
        observedSequence.add(2);
        observedSequence.add(3);

        Location location = localization.getLocation(observedSequence);
        if (location != null) {
            System.out.println("手机位置：纬度=" + location.getLatitude() + ", 经度=" + location.getLongitude());
        } else {
            System.out.println("无法确定手机位置");
        }
    }
}