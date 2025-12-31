package com.warehouse.model;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private String type;
    private int productId;
    private int sourceCounterId;
    private int destinationCounterId;
    private ArrayList<Product> products;

    public Task(String type, List<Product> products, int counterId) {
        this.type = type;
        this.products = new ArrayList<>(products);
        if ("INBOUND".equals(type)) {
            this.sourceCounterId = counterId;
        } else if ("OUTBOUND".equals(type)) {
            this.destinationCounterId = counterId;
        }
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getSourceCounterId() {
        return sourceCounterId;
    }

    public void setSourceCounterId(int sourceCounterId) {
        this.sourceCounterId = sourceCounterId;
    }

    public int getDestinationCounterId() {
        return destinationCounterId;
    }

    public void setDestinationCounterId(int destinationCounterId) {
        this.destinationCounterId = destinationCounterId;
    }

    @Override
    public String toString() {
        return "Task{type='" + type + "', products=" + products.size() +
                ", source=" + sourceCounterId + ", dest=" + destinationCounterId + "}";
    }
}
