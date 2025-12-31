package com.warehouse.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Product {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private Integer id;
    private String name;
    private String category;
    private String groupId;
    private int locationY;
    private int locationX;
    private int quantity;
    private double weight;

    public Product(Integer id, String name, String category, String groupId,
            int locationRow, int locationCol, int quantity, double weight) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.groupId = groupId;
        this.locationY = locationRow;
        this.locationX = locationCol;
        this.quantity = quantity;
        this.weight = weight;
    }

    public Product(String name, String category, String groupId, double weight) {
        this.name = name;
        this.category = category;
        this.groupId = groupId;
        this.weight = weight;
    }

    public void setUniqueId(int uniqueId) {
        this.id = uniqueId;
    }

    public void setUniqueId() {
        this.id = ID_GENERATOR.getAndIncrement();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getLocationY() {
        return locationY;
    }

    public void setLocationY(int locationY) {
        this.locationY = locationY;
    }

    public int getLocationX() {
        return locationX;
    }

    public void setLocationX(int locationX) {
        this.locationX = locationX;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
