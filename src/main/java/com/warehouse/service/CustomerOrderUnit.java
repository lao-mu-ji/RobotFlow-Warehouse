package com.warehouse.service;

import com.warehouse.config.ConfigLoader;
import com.warehouse.model.Product;
import com.warehouse.model.Task;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CustomerOrderUnit implements Runnable {
    private final Supervisor supervisor;
    private final Random random;
    private final List<String> productNames = Arrays.asList("Char-Broil", "Weber", "KitchenAid", "Ninja", "Cuisinart");
    private final List<String> categories = Arrays.asList("Grills", "Cooking", "Appliances", "Electronics",
            "Furniture");
    private final int minBatchSize;
    private final int maxBatchSize;
    private volatile boolean running;
    private SupplyUnit supplyUnit;
    private volatile int speed = 5;
    private final String configFilePath;

    public CustomerOrderUnit(Supervisor supervisor) {
        this(supervisor, "ConfigFile.csv");
    }

    public CustomerOrderUnit(Supervisor supervisor, String configFilePath) {
        this.supervisor = supervisor;
        this.random = new Random();
        this.minBatchSize = 1;
        this.maxBatchSize = 5;
        this.running = true;
        this.configFilePath = configFilePath;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setSupplyUnit(SupplyUnit supplyUnit) {
        this.supplyUnit = supplyUnit;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(speed * 1000L + 1000);
                Task request = generateOutgoingRequest();
                if (request != null) {
                    supervisor.addRequest(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        this.running = false;
    }

    private Task generateOutgoingRequest() {
        int batchSize = minBatchSize;
        List<Product> products = new ArrayList<>();

        int w = random.nextInt(productNames.size());
        String name = productNames.get(w);
        String category = categories.get(w);

        for (int i = 0; i < batchSize; i++) {
            products.add(new Product(name, category, "", 0));
        }

        List<Point> points = findProductsInContainer(name, category, batchSize);
        if (points == null) {
            if (supplyUnit != null) {
                supplyUnit.incoming(name, category, batchSize);
            }
            return null;
        }

        for (int i = 0; i < batchSize; i++) {
            products.get(i).setLocationX(points.get(i).x);
            products.get(i).setLocationY(points.get(i).y);
        }

        int numberOfOutboundCounters = ConfigLoader.readOutboundCounters(configFilePath);
        int outgoingCounter = random.nextInt(numberOfOutboundCounters);
        return new Task("OUTBOUND", products, outgoingCounter);
    }

    private List<Point> findProductsInContainer(String name, String category, int requiredQuantity) {
        List<Point> matchingPoints = new ArrayList<>();
        int availableQuantity = 0;

        HashMap<Point, Product> productsInContainer = supervisor.getWarehouseGrid().getProductsInContainer();
        for (Map.Entry<Point, Product> entry : productsInContainer.entrySet()) {
            Product product = entry.getValue();
            if (product.getName().equals(name) && product.getCategory().equals(category)) {
                matchingPoints.add(entry.getKey());
                availableQuantity++;

                if (availableQuantity >= requiredQuantity) {
                    return matchingPoints;
                }
            }
        }

        return null;
    }
}
