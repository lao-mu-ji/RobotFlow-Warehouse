package com.warehouse;

import com.warehouse.config.ConfigLoader;
import com.warehouse.repository.DatabaseManager;
import com.warehouse.service.CustomerOrderUnit;
import com.warehouse.service.Supervisor;
import com.warehouse.service.SupplyUnit;
import com.warehouse.view.SupervisorGUI;
import com.warehouse.view.WarehouseGrid;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String configFilePath = "ConfigFile.csv";

        if (args.length > 0) {
            configFilePath = args[0];
        }

        try {
            // 加载配置
            System.out.println("Loading configuration...");
            ConfigLoader configLoader = new ConfigLoader();
            WarehouseGrid grid = configLoader.loadConfig(new File(configFilePath));
            // 初始化数据库
            System.out.println("Initializing database...");
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.connect();
            // 创建Supervisor
            System.out.println("Starting supervisor...");
            Supervisor supervisor = new Supervisor(grid);
            supervisor.setDatabaseManager(databaseManager);

            // 启动GUI
            SupervisorGUI gui = new SupervisorGUI(grid);
            supervisor.setLogListener(gui);
            System.out.println("Launching GUI...");

            final String finalConfigFilePath = configFilePath;
            SwingUtilities.invokeLater(() -> {
                gui.setSupervisor(supervisor);
                gui.setVisible(true);
            });

            // 启动业务线程
            System.out.println("Starting supply and customer order units...");
            SupplyUnit supplyUnit = new SupplyUnit(supervisor, finalConfigFilePath);
            CustomerOrderUnit customerOrderUnit = new CustomerOrderUnit(supervisor, finalConfigFilePath);
            customerOrderUnit.setSupplyUnit(supplyUnit);
            gui.setSupplyUnit(supplyUnit);
            gui.setCustomerOrderUnit(customerOrderUnit);

            new Thread(supplyUnit, "SupplyUnit-Thread").start();
            new Thread(customerOrderUnit, "CustomerOrderUnit-Thread").start();

            System.out.println("System started successfully!");

            // 关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                supplyUnit.stop();
                customerOrderUnit.stop();
                supervisor.shutdown();
                databaseManager.closeConnection();
            }));

        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error initializing system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
