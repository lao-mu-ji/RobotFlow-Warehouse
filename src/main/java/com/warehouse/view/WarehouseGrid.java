package com.warehouse.view;

import com.warehouse.entity.Robot;
import com.warehouse.model.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WarehouseGrid extends JPanel implements ActionListener {
    private final int cellSize;
    private final int rows;
    private final int columns;
    private final List<Point> shelves;
    private final List<Point> inboundCounters;
    private final List<Point> outboundCounters;
    private final List<Point> chargingStations;
    private final ArrayList<Robot> robots;
    private final Timer timer;
    private final Integer contain;
    private Integer currentContain;
    private final List<Point> usedShelves;
    private final HashMap<Point, Product> productsInContainer;

    public WarehouseGrid(int cellSize, int gridWidth, int gridHeight,
            List<Point> shelves,
            List<Point> inboundCounters,
            List<Point> outboundCounters,
            List<Point> chargingStations,
            ArrayList<Robot> robots) {
        this.cellSize = cellSize;
        this.rows = gridHeight;
        this.columns = gridWidth;
        this.shelves = shelves;
        this.inboundCounters = inboundCounters;
        this.outboundCounters = outboundCounters;
        this.chargingStations = chargingStations;
        this.robots = robots;
        this.contain = 2 * (gridHeight - 2) * shelves.size();
        this.currentContain = 0;
        this.usedShelves = new ArrayList<>();
        this.productsInContainer = new HashMap<>();

        setPreferredSize(new Dimension(columns * cellSize, rows * cellSize));
        setBackground(Color.WHITE);

        timer = new Timer(100, this);
        timer.start();
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        for (int row = 0; row <= rows; row++) {
            g.drawLine(0, row * cellSize, columns * cellSize, row * cellSize);
        }
        for (int col = 0; col <= columns; col++) {
            g.drawLine(col * cellSize, 0, col * cellSize, rows * cellSize);
        }
    }

    private void drawComponents(Graphics g) {
        // 绘制货架
        g.setColor(Color.GRAY);
        for (Point shelf : shelves) {
            drawCell(g, shelf.x, shelf.y);
        }

        Graphics2D g2d = (Graphics2D) g;

        // 绘制入库柜台 - 缩放图标到 cellSize
        for (Point inbound : inboundCounters) {
            Image img = new ImageIcon("src/main/resources/images/arrow-25.png").getImage();
            g2d.drawImage(img, inbound.x * cellSize, inbound.y * cellSize, cellSize, cellSize, null);
        }

        // 绘制出库柜台 - 缩放图标到 cellSize
        for (Point outbound : outboundCounters) {
            Image img = new ImageIcon("src/main/resources/images/arrow-25.png").getImage();
            g2d.drawImage(img, outbound.x * cellSize, outbound.y * cellSize, cellSize, cellSize, null);
        }

        // 绘制充电桩 - 缩放图标到 cellSize
        for (Point charging : chargingStations) {
            Image img = new ImageIcon("src/main/resources/images/plug-24.png").getImage();
            g2d.drawImage(img, charging.x * cellSize, charging.y * cellSize, cellSize, cellSize, null);
        }
    }

    private void drawCell(Graphics g, int col, int row) {
        int x = col * cellSize;
        int y = row * cellSize;
        g.fillRect(x, y, cellSize, cellSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawComponents(g);

        Graphics2D g2d = (Graphics2D) g;
        // 绘制机器人 - 缩放图标到 cellSize
        for (Robot robot : robots) {
            g2d.drawImage(robot.getImage(), robot.getX() * cellSize, robot.getY() * cellSize, cellSize, cellSize, null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public ArrayList<Robot> getRobots() {
        return robots;
    }

    public List<Point> getInboundCounters() {
        return inboundCounters;
    }

    public List<Point> getOutboundCounters() {
        return outboundCounters;
    }

    public HashMap<Point, Product> getProductsInContainer() {
        return productsInContainer;
    }

    /**
     * 分配一个可用的货架位置
     * 
     * @return 可用的货架位置，如果没有可用位置返回null
     */
    public Point assignLocation() {
        for (Point shelf : shelves) {
            if (!usedShelves.contains(shelf)) {
                usedShelves.add(shelf);
                return shelf;
            }
        }
        return null;
    }

    /**
     * 检查是否有足够的可用位置
     * 
     * @param requiredCount 需要的位置数量
     * @return 如果有足够位置返回true
     */
    public boolean hasEnoughSpace(int requiredCount) {
        return (shelves.size() - usedShelves.size()) >= requiredCount;
    }

    /**
     * 计算产品在货架上的实际放置位置
     * 
     * @param x 逻辑X坐标
     * @param y 逻辑Y坐标
     * @return 实际放置位置
     */
    public Point calculateLocation(int x, int y) {
        int actualX;
        if (x % 3 == 1) {
            actualX = x - 1;
        } else if (x % 3 == 2) {
            actualX = x + 1;
        } else {
            actualX = x;
        }
        return new Point(actualX, y);
    }

    /**
     * 释放货架位置
     * 
     * @param point 要释放的位置
     */
    public void releaseLocation(Point point) {
        usedShelves.remove(point);
    }
}
