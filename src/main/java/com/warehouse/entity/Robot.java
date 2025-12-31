package com.warehouse.entity;

import com.warehouse.view.LogListener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Robot implements Runnable {
    private final int rows;
    private final int columns;
    private final List<Point> shelves;
    private final List<Point> chargingStations;
    private final int id;
    private final Character name;
    private int x;
    private int y;
    private final int capacity;
    private boolean isCarrying;
    private String status;
    private Thread robotThread;
    private int targetX;
    private int targetY;
    private volatile boolean running;
    private Image image;
    private int battery;
    private String carriedItem = "";
    private final int speed;
    private List<Point> path = new ArrayList<>();
    private volatile boolean tobestopped = false;

    private static LogListener logListener;

    public static void setLogListener(LogListener listener) {
        logListener = listener;
    }

    private static void log(String message) {
        if (logListener != null) {
            logListener.onTaskLog(message);
        }
    }

    public Thread getRobotThread() {
        return robotThread;
    }

    public Robot(int id, int startX, int startY, int capacity, List<Point> chargingStations,
            int gridWidth, int gridHeight, List<Point> shelves, Character name, int robotSpeed) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.capacity = capacity;
        this.battery = 100;
        this.isCarrying = false;
        this.status = "inactive";
        this.chargingStations = chargingStations;
        this.robotThread = new Thread(this);
        this.running = true;
        this.image = new ImageIcon("src/main/resources/images/" + name + "-green.png").getImage();
        this.columns = gridWidth;
        this.rows = gridHeight;
        this.shelves = shelves;
        this.name = name;
        this.speed = robotSpeed;
        this.targetX = startX;
        this.targetY = startY;
        robotThread.start();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Image getImage() {
        return image;
    }

    public boolean isCarrying() {
        return isCarrying;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRunning() {
        return running;
    }

    public int getBattery() {
        return battery;
    }

    public void moveStep() {
        try {
            Thread.sleep(1000 / speed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!path.isEmpty()) {
            Point nextStep = path.remove(0);
            x = nextStep.x;
            y = nextStep.y;

            if (battery > 0) {
                battery -= 1;
            }
            checkChargeNeeded();
            updateImage();
            log("Robot " + id + " moved to (" + x + ", " + y + "). Battery: " + battery);
        }
    }

    private void updateImage() {
        String suffix;
        if (battery >= 50) {
            suffix = isCarrying ? "-green-black.png" : "-green.png";
        } else if (battery >= 10) {
            suffix = isCarrying ? "-orange-black.png" : "-orange.png";
        } else {
            suffix = "-red.png";
        }
        image = new ImageIcon("src/main/resources/images/" + name + suffix).getImage();
    }

    private void checkChargeNeeded() {
        if (battery <= 10 && "inactive".equals(status)) {
            status = "goto charging";
            if (id - 1 < chargingStations.size()) {
                Point chargingStation = chargingStations.get(id - 1);
                moveTo(chargingStation.x, chargingStation.y);
                waitUntilArrived(chargingStation.x, chargingStation.y);
                charge();
            }
        }
    }

    private void waitUntilArrived(int targetX, int targetY) {
        while (x != targetX || y != targetY) {
            Thread.yield();
        }
    }

    public boolean isShelf(int x, int y) {
        return shelves.contains(new Point(x, y));
    }

    public boolean isNotArea(int x, int y) {
        return !(x >= 0 && x < columns && y >= 0 && y < rows);
    }

    public boolean boardOut(int x, int y) {
        return isShelf(x, y) || isNotArea(x, y);
    }

    public synchronized void moveTo(int targetX, int targetY) {
        log("Robot " + id + " is moving to (" + targetX + ", " + targetY + ")...");
        this.targetX = targetX;
        this.targetY = targetY;
        path = calculatePath(x, y, targetX, targetY);

        if (path.isEmpty() && (x != targetX || y != targetY)) {
            log("Robot " + id + " could not find a path to the target!");
            System.out.println("Robot " + id + " could not find a path to (" + targetX + ", " + targetY + ").");
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (x != targetX || y != targetY) {
                    moveStep();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void pickUp() throws InterruptedException {
        if (isCarrying) {
            log("Robot " + id + " is already carrying an item!");
            return;
        }
        Thread.sleep(1000);
        log("Robot " + id + " picking up an item...");
        isCarrying = true;
        status = "active";
    }

    public synchronized void dropOff() throws InterruptedException {
        if (!isCarrying) {
            log("Robot " + id + " is not carrying any item!");
            return;
        }
        Thread.sleep(1000);
        log("Robot " + id + " dropping off an item...");
        isCarrying = false;
        status = "inactive";
        updateImage();
    }

    public synchronized void charge() {
        log("Robot " + id + " is charging...");
        status = "charging";
        try {
            Thread.sleep(3000);
            battery = 100;
            updateImage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        status = "inactive";
        log("Robot " + id + " finished charging.");
    }

    public void performTask(int pickUpX, int pickUpY, int dropOffX, int dropOffY) throws InterruptedException {
        status = "active";
        log("Robot " + id + " starting task...");

        moveTo(pickUpX, pickUpY);
        waitUntilArrived(pickUpX, pickUpY);
        pickUp();

        moveTo(dropOffX, dropOffY);
        waitUntilArrived(dropOffX, dropOffY);
        dropOff();

        if (tobestopped) {
            handleStop();
        } else {
            checkChargeNeeded();
        }
        log("Robot " + id + " completed task.");
    }

    private void handleStop() {
        if (tobestopped && "inactive".equals(status)) {
            if (id - 1 < chargingStations.size()) {
                Point chargingStation = chargingStations.get(id - 1);
                moveTo(chargingStation.x, chargingStation.y);
                waitUntilArrived(chargingStation.x, chargingStation.y);
                running = false;
                status = "deactivated";
                try {
                    Thread.sleep(3000);
                    battery = 100;
                    updateImage();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public synchronized void stop() {
        tobestopped = true;
    }

    public synchronized void start() {
        this.robotThread = new Thread(this);
        running = true;
        robotThread.start();
        status = "inactive";
        tobestopped = false;
    }

    public Character getName() {
        return name;
    }

    // BFS寻路算法
    private List<Point> calculatePath(int startX, int startY, int targetX, int targetY) {
        boolean[][] visited = new boolean[columns][rows];
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> parentMap = new HashMap<>();

        queue.offer(new Point(startX, startY));
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int cx = current.x, cy = current.y;

            if (cx == targetX && cy == targetY) {
                List<Point> resultPath = new ArrayList<>();
                Point step = current;
                while (step != null) {
                    resultPath.add(0, step);
                    step = parentMap.get(step);
                }
                return resultPath;
            }

            int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (!boardOut(nx, ny) && !visited[nx][ny]) {
                    queue.offer(new Point(nx, ny));
                    visited[nx][ny] = true;
                    parentMap.put(new Point(nx, ny), current);
                }
            }
        }

        return new ArrayList<>();
    }}
