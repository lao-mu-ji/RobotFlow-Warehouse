package com.warehouse.service;

import com.warehouse.entity.Robot;
import com.warehouse.model.Product;
import com.warehouse.model.Task;
import com.warehouse.repository.DatabaseManager;
import com.warehouse.view.LogListener;
import com.warehouse.view.WarehouseGrid;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class Supervisor {
    private WarehouseGrid warehouseGrid;
    private DatabaseManager databaseManager;
    private LogListener logListener;
    private final List<Robot> robots;
    private final TaskQueue requestQueue;
    private final Map<Robot, TaskQueue> robotTaskQueues;
    private final ExecutorService robotThreadPool;

    private Task lastInboundRequest;
    private Task lastOutboundRequest;

    public Map<Robot, TaskQueue> getRobotTaskQueues() {
        return robotTaskQueues;
    }

    public WarehouseGrid getWarehouseGrid() {
        return warehouseGrid;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setLogListener(LogListener logListener) {
        this.logListener = logListener;
    }

    public Supervisor(WarehouseGrid warehouseGrid) {
        this.warehouseGrid = warehouseGrid;
        this.databaseManager = null;
        this.robots = warehouseGrid.getRobots();
        this.requestQueue = new TaskQueue();
        this.robotTaskQueues = new ConcurrentHashMap<>();
        this.robotThreadPool = Executors.newFixedThreadPool(robots.size() * 2);

        startRequestProcessor();
        for (Robot robot : robots) {
            TaskQueue taskQueue = new TaskQueue();
            robotTaskQueues.put(robot, taskQueue);
            startRobotThread(robot, taskQueue);
        }
    }

    public void startRobotThread(Robot robot, TaskQueue taskQueue) {
        robotThreadPool.execute(() -> {
            while (true) {
                try {
                    if (robot.isRunning() && "inactive".equals(robot.getStatus())) {
                        Task task = taskQueue.dequeueTask();
                        if (task != null) {
                            log("Robot " + robot.getId() + " started task: " + task);
                            executeRobotTask(robot, task);
                        } else {
                            Thread.sleep(100);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                }
            }
        });
    }

    public synchronized void assignTaskToLeastBusyRobot(Task task) {
        Robot leastBusyRobot = null;
        int minTaskCount = Integer.MAX_VALUE;

        for (Map.Entry<Robot, TaskQueue> entry : robotTaskQueues.entrySet()) {
            Robot robot = entry.getKey();
            TaskQueue taskQueue = entry.getValue();

            if (robot.isRunning()) {
                int taskCount = taskQueue.size();
                if (taskCount < minTaskCount) {
                    minTaskCount = taskCount;
                    leastBusyRobot = robot;
                }
            }
        }

        if (leastBusyRobot != null) {
            robotTaskQueues.get(leastBusyRobot).enqueueTask(task);
            log("Task assigned to robot: " + leastBusyRobot.getName());
        } else {
            requestQueue.enqueueTask(task);
            log("No available robot found. Request requeued.");
        }
    }

    private void executeRobotTask(Robot robot, Task task) throws InterruptedException, SQLException {
        List<Point> inboundCounters = warehouseGrid.getInboundCounters();
        List<Point> outboundCounters = warehouseGrid.getOutboundCounters();

        if ("INBOUND".equals(task.getType())) {
            for (Product product : task.getProducts()) {
                int sourceIndex = task.getSourceCounterId() - 1;
                if (sourceIndex >= 0 && sourceIndex < inboundCounters.size()) {
                    Point source = inboundCounters.get(sourceIndex);
                    Point target = warehouseGrid.calculateLocation(product.getLocationX(), product.getLocationY());

                    robot.performTask(source.x, source.y, target.x, product.getLocationY());

                    if (databaseManager != null) {
                        databaseManager.addProduct(product);
                    }
                    warehouseGrid.getProductsInContainer().put(
                            new Point(product.getLocationX(), product.getLocationY()), product);
                }
            }
        } else if ("OUTBOUND".equals(task.getType())) {
            for (Product product : task.getProducts()) {
                int destIndex = task.getDestinationCounterId();
                if (destIndex >= 0 && destIndex < outboundCounters.size()) {
                    Point source = warehouseGrid.calculateLocation(product.getLocationX(), product.getLocationY());
                    Point dest = outboundCounters.get(destIndex);

                    robot.performTask(source.x, product.getLocationY(), dest.x, dest.y);

                    warehouseGrid.getProductsInContainer().remove(
                            new Point(product.getLocationX(), product.getLocationY()));

                    if (databaseManager != null) {
                        databaseManager.deleteProduct(product.getName(), product.getCategory(),
                                product.getLocationX(), product.getLocationY());
                    }
                }
            }
        }
    }

    public void setWarehouseGrid(WarehouseGrid warehouseGrid) {
        this.warehouseGrid = warehouseGrid;
    }

    public void addRobot(Robot robot) {
        robots.add(robot);
    }

    private void startRequestProcessor() {
        Thread processorThread = new Thread(() -> {
            while (true) {
                Task request = requestQueue.dequeueTask();
                if (request != null) {
                    try {
                        processRequest(request);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        processorThread.setDaemon(true);
        processorThread.start();
    }

    public synchronized void addRequest(Task request) {
        requestQueue.enqueueTask(request);
        log("New request added: " + request.getType() + " " + request);
    }

    private void processRequest(Task request) throws InterruptedException {
        switch (request.getType()) {
            case "INBOUND":
                handleIncomingRequest(request);
                break;
            case "OUTBOUND":
                handleCustomerOrderRequest(request);
                break;
            default:
                log("Unknown request type: " + request.getType());
        }
    }

    private void handleIncomingRequest(Task request) throws InterruptedException {
        if (lastInboundRequest != request) {
            log("Assigning incoming request: " + request.getType() + " " + request);
        }
        lastInboundRequest = request;

        ArrayList<Product> products = new ArrayList<>(request.getProducts());
        if (!warehouseGrid.hasEnoughSpace(products.size())) {
            requestQueue.enqueueTask(request);
            log("No shelves available. Request requeued.");
            return;
        }

        for (Product product : products) {
            product.setUniqueId();
            Point location = warehouseGrid.assignLocation();
            if (location != null) {
                product.setLocationY(location.y);
                product.setLocationX(location.x);
                log("Product stored: " + product.getName());
            } else {
                requestQueue.enqueueTask(request);
                log("No shelves available. Request requeued.");
                return;
            }
        }
        request.setProducts(products);
        assignTaskToLeastBusyRobot(request);
    }

    private void handleCustomerOrderRequest(Task request) {
        if (lastOutboundRequest != request) {
            Product firstProduct = request.getProducts().get(0);
            log("Assigning outgoing request: " + firstProduct.getName() + ":" + firstProduct.getCategory());
        }
        lastOutboundRequest = request;
        assignTaskToLeastBusyRobot(request);
    }

    public Point calculateLocation(int x, int y) {
        return warehouseGrid.calculateLocation(x, y);
    }

    private void log(String message) {
        if (logListener != null) {
            logListener.onMessageLog(message);
        }
    }

    public void shutdown() {
        robotThreadPool.shutdown();
        try {
            if (!robotThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                robotThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            robotThreadPool.shutdownNow();
        }
    }
}
