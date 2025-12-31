package com.warehouse.service;

import com.warehouse.config.ConfigLoader;
import com.warehouse.model.Product;
import com.warehouse.model.Task;

import java.util.*;

public class SupplyUnit implements Runnable {
    private final Supervisor supervisor;
    private final Random random;
    private final List<String> productNames = Arrays.asList("Char-Broil", "Weber", "KitchenAid", "Ninja", "Cuisinart");
    private final List<String> categories = Arrays.asList("Grills", "Cooking", "Appliances", "Electronics",
            "Furniture");
    private final int minBatchSize;
    private final int maxBatchSize;
    private volatile boolean running;
    private volatile int speed = 5;
    private final String configFilePath;

    public SupplyUnit(Supervisor supervisor) {
        this(supervisor, "ConfigFile.csv");
    }

    public SupplyUnit(Supervisor supervisor, String configFilePath) {
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

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(speed * 1000L + 1000);
                Task request = generateIncomingRequest();
                supervisor.addRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        this.running = false;
    }

    private Task generateIncomingRequest() {
        int batchSize = minBatchSize;
        List<Product> products = new ArrayList<>();
        String groupId = UUID.randomUUID().toString();
        double weight = 1.0 + (10.0 - 1.0) * random.nextDouble();

        for (int i = 0; i < batchSize; i++) {
            int w = random.nextInt(productNames.size());
            String name = productNames.get(w);
            String category = categories.get(w);
            products.add(new Product(name, category, groupId, weight));
        }

        int numberOfInboundCounters = ConfigLoader.readInboundCounters(configFilePath);
        int incomingCounter = random.nextInt(numberOfInboundCounters) + 1;
        return new Task("INBOUND", products, incomingCounter);
    }

    private Task generateIncomingRequest(String name, String category, int batchSize) {
        List<Product> products = new ArrayList<>();
        String groupId = UUID.randomUUID().toString();
        double weight = 1.0 + (10.0 - 1.0) * random.nextDouble();

        for (int i = 0; i < batchSize; i++) {
            products.add(new Product(name, category, groupId, weight));
        }

        int numberOfInboundCounters = ConfigLoader.readInboundCounters(configFilePath);
        int incomingCounter = random.nextInt(numberOfInboundCounters) + 1;
        return new Task("INBOUND", products, incomingCounter);
    }

    public void incoming(String name, String category, int batchSize) {
        Task request = generateIncomingRequest(name, category, batchSize);
        supervisor.addRequest(request);
    }
}
