package com.warehouse.config;

import com.warehouse.entity.Robot;
import com.warehouse.view.WarehouseGrid;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置加载器
 * 负责从CSV文件加载仓库配置
 */
public class ConfigLoader {

    private int gridWidth;
    private int gridHeight;
    private int numberOfInboundCounters;
    private int numberOfOutboundCounters;
    private int numberOfChargingStations;
    private int numberOfRobots;
    private List<String[]> configLines;

    public WarehouseGrid loadConfig(File configFile) throws IOException {
        String fileName = configFile.getName();
        if (fileName.endsWith(".csv")) {
            return loadFromCSV(configFile);
        } else {
            throw new IllegalArgumentException("Unsupported configuration file format: " + fileName);
        }
    }

    private WarehouseGrid loadFromCSV(File configFile) throws IOException {
        configLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                configLines.add(line.split(","));
            }
        }

        int cellSize = 30;
        gridWidth = Integer.parseInt(configLines.get(0)[0]);
        gridHeight = Integer.parseInt(configLines.get(0)[1]);

        int numberOfShelves = Integer.parseInt(configLines.get(1)[0]);
        int widthOfShelf = 2;
        int heightOfShelf = gridHeight - 2;

        List<Point> shelves = new ArrayList<>();
        for (int i = 0; i < numberOfShelves; i++) {
            for (int j = 0; j < widthOfShelf; j++) {
                for (int k = 0; k < heightOfShelf; k++) {
                    shelves.add(new Point(j + 3 * i + 1, k + 1));
                }
            }
        }

        numberOfInboundCounters = Integer.parseInt(configLines.get(2)[0]);
        List<Point> inboundCounters = new ArrayList<>();
        for (int i = 0; i < numberOfInboundCounters; i++) {
            inboundCounters.add(new Point(0, 2 * i));
        }

        numberOfOutboundCounters = Integer.parseInt(configLines.get(3)[0]);
        List<Point> outboundCounters = new ArrayList<>();
        for (int i = 0; i < numberOfOutboundCounters; i++) {
            outboundCounters.add(new Point(gridWidth - 1, 2 * i));
        }

        numberOfChargingStations = Integer.parseInt(configLines.get(4)[0]);
        List<Point> chargingStations = new ArrayList<>();
        for (int i = 0; i < numberOfChargingStations; i++) {
            chargingStations.add(new Point(2 * i, gridHeight - 1));
        }

        numberOfRobots = Integer.parseInt(configLines.get(5)[0]);
        ArrayList<Robot> robots = new ArrayList<>();
        ArrayList<Character> names = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            names.add(c);
        }

        for (int i = 0; i < numberOfRobots; i++) {
            int robotCapacity = Integer.parseInt(configLines.get(5)[i + 1]);
            int robotSpeed = Integer.parseInt(configLines.get(6)[i]);
            int robotId = i + 1;
            Point initialPosition = chargingStations.get(i);
            Robot robot = new Robot(robotId, initialPosition.x, initialPosition.y, robotCapacity,
                    chargingStations, gridWidth, gridHeight, shelves, names.get(i), robotSpeed);
            robots.add(robot);
        }

        return new WarehouseGrid(cellSize, gridWidth, gridHeight, shelves,
                inboundCounters, outboundCounters, chargingStations, robots);
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getNumberOfInboundCounters() {
        return numberOfInboundCounters;
    }

    public int getNumberOfOutboundCounters() {
        return numberOfOutboundCounters;
    }

    public int getNumberOfChargingStations() {
        return numberOfChargingStations;
    }

    public int getNumberOfRobots() {
        return numberOfRobots;
    }

    public static int readInboundCounters(String configFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(configFilePath))) {
            List<String[]> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.split(","));
            }
            return Integer.parseInt(lines.get(2)[0]);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + configFilePath, e);
        }
    }

    public static int readOutboundCounters(String configFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(configFilePath))) {
            List<String[]> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.split(","));
            }
            return Integer.parseInt(lines.get(3)[0]);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + configFilePath, e);
        }
    }
}
