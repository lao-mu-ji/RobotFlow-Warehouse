package com.warehouse.view;

import com.warehouse.entity.Robot;
import com.warehouse.service.CustomerOrderUnit;
import com.warehouse.service.Supervisor;
import com.warehouse.service.SupplyUnit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SupervisorGUI extends JFrame implements LogListener {
    private Timer timer;
    private JTextArea messagesArea;
    private JTextArea taskLogArea;
    private JTable robotStatusTable;
    private DefaultTableModel tableModel;
    private final WarehouseGrid grid;
    private Supervisor supervisor;
    private SupplyUnit supplyUnit;
    private CustomerOrderUnit customerOrderUnit;

    private static SupervisorGUI instance;

    public SupervisorGUI(WarehouseGrid grid) {
        this.grid = grid;
        instance = this;

        Robot.setLogListener(this);

        initializeUI();
    }

    private void initializeUI() {
        String[] columnNames = { "Robot ID", "Name", "Status", "Action" };

        Object[][] data = new Object[grid.getRobots().size()][4];
        int i = 0;
        for (Robot robot : grid.getRobots()) {
            data[i][0] = robot.getId();
            data[i][1] = robot.getName();
            data[i][2] = robot.getStatus();
            data[i][3] = "deactivated".equals(robot.getStatus()) ? "Activate" : "Deactivate";
            i++;
        }

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3) {
                    return JButton.class;
                }
                return super.getColumnClass(column);
            }
        };

        // 美化表格样式 - 浅色风格
        robotStatusTable = new JTable(tableModel) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row,
                    int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 250));
                }
                return c;
            }
        };
        robotStatusTable.setRowHeight(28);
        robotStatusTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        robotStatusTable.setGridColor(new Color(220, 225, 230));
        robotStatusTable.setSelectionBackground(new Color(200, 220, 240));
        robotStatusTable.getTableHeader().setBackground(new Color(240, 245, 250));
        robotStatusTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        robotStatusTable.getTableHeader()
                .setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(70, 130, 180)));
        robotStatusTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        robotStatusTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        setLayout(new BorderLayout());
        setTitle("RoboFlow Warehouse - 智能仓储机器人调度系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 根据仓库网格尺寸动态计算窗口大小
        Dimension gridSize = grid.getPreferredSize();
        int windowWidth = gridSize.width + 500; // 左侧网格 + 右侧面板
        int windowHeight = Math.max(gridSize.height + 100, 700); // 网格高度 + 按钮区域

        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(248, 250, 252));

        JScrollPane scrollPane = new JScrollPane(robotStatusTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1)));

        // 速度控制按钮面板 - 恢复原始样式
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
        JButton accelerateProductButton = new JButton("Speed Up Product Frequency");
        JButton decelerateProductButton = new JButton("Slow Down Product Frequency");
        JButton accelerateOrderButton = new JButton("Speed Up Order Frequency");
        JButton decelerateOrderButton = new JButton("Slow Down Order Frequency");

        buttonPanel.add(accelerateProductButton);
        buttonPanel.add(decelerateProductButton);
        buttonPanel.add(accelerateOrderButton);
        buttonPanel.add(decelerateOrderButton);

        accelerateProductButton.addActionListener(e -> adjustProductSpeed(-1));
        decelerateProductButton.addActionListener(e -> adjustProductSpeed(1));
        accelerateOrderButton.addActionListener(e -> adjustOrderSpeed(-1));
        decelerateOrderButton.addActionListener(e -> adjustOrderSpeed(1));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(248, 250, 252));
        imagePanel.add(grid, BorderLayout.CENTER);
        imagePanel.add(buttonPanel, BorderLayout.SOUTH);
        // 设置首选尺寸确保仓库视图完整显示
        imagePanel.setPreferredSize(new Dimension(gridSize.width, gridSize.height + 100));
        add(imagePanel, BorderLayout.WEST);

        // 日志区域 - 浅色美化
        taskLogArea = new JTextArea();
        taskLogArea.setEditable(false);
        taskLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        taskLogArea.setBackground(new Color(252, 253, 254));
        taskLogArea.setForeground(new Color(40, 80, 60));

        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        messagesArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        messagesArea.setBackground(new Color(252, 253, 254));
        messagesArea.setForeground(new Color(50, 80, 120));

        JScrollPane messageScrollPane = new JScrollPane(messagesArea);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 200), 1),
                "Messages", 0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(70, 130, 180)));

        JScrollPane taskLogScrollPane = new JScrollPane(taskLogArea);
        taskLogScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 160, 120), 1),
                "Robots Activity Logs", 0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(60, 140, 100)));

        // 使用 BoxLayout 垂直布局，让消息面板自动填充空白区域
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(248, 250, 252));

        // 机器人状态表格面板 - 固定高度
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(248, 250, 252));
        northPanel.add(scrollPane);
        northPanel.setPreferredSize(new Dimension(400, 150));
        northPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // 消息面板 - 自动填充剩余空间
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(248, 250, 252));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        centerPanel.add(messageScrollPane);

        // 日志面板 - 固定高度
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(248, 250, 252));
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        southPanel.add(taskLogScrollPane);
        southPanel.setPreferredSize(new Dimension(400, 200));
        southPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        infoPanel.add(northPanel);
        infoPanel.add(centerPanel);
        infoPanel.add(southPanel);

        add(infoPanel, BorderLayout.CENTER);

        // 定时更新机器人状态
        timer = new Timer(100, e -> updateRobotStatuses());
        timer.start();
    }

    private void adjustProductSpeed(int delta) {
        if (supplyUnit != null) {
            int newSpeed = supplyUnit.getSpeed() + delta;
            if (newSpeed >= 0 && newSpeed <= 10) {
                supplyUnit.setSpeed(newSpeed);
            }
        }
    }

    private void adjustOrderSpeed(int delta) {
        if (customerOrderUnit != null) {
            int newSpeed = customerOrderUnit.getSpeed() + delta;
            if (newSpeed >= 0 && newSpeed <= 10) {
                customerOrderUnit.setSpeed(newSpeed);
            }
        }
    }

    private void updateRobotStatuses() {
        for (int i = 0; i < grid.getRobots().size(); i++) {
            Robot robot = grid.getRobots().get(i);
            String robotId = String.valueOf(robot.getId());
            String status = robot.getStatus();
            String action = "deactivated".equals(status) ? "Activate" : "Deactivate";

            tableModel.setValueAt(robotId, i, 0);
            tableModel.setValueAt(robot.getName(), i, 1);
            tableModel.setValueAt(status, i, 2);
            tableModel.setValueAt(action, i, 3);
        }
        tableModel.fireTableDataChanged();
    }

    // LogListener 实现
    @Override
    public void onTaskLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (taskLogArea != null) {
                taskLogArea.append(message + "\n");
            }
        });
    }

    @Override
    public void onMessageLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (messagesArea != null) {
                messagesArea.append(message + "\n");
            }
        });
    }

    // 静态方法保持向后兼容
    public static void addTaskLog(String log) {
        if (instance != null) {
            instance.onTaskLog(log);
        }
    }

    public static void addRobotLog(String log) {
        if (instance != null) {
            instance.onMessageLog(log);
        }
    }

    public void addLog(String log) {
        onMessageLog(log);
    }

    public void setSupplyUnit(SupplyUnit supplyUnit) {
        this.supplyUnit = supplyUnit;
    }

    public void setCustomerOrderUnit(CustomerOrderUnit customerOrderUnit) {
        this.customerOrderUnit = customerOrderUnit;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    private void deactivateRobot(String robotId) {
        onTaskLog("Deactivating robot: " + robotId);
        grid.getRobots().stream()
                .filter(robot -> robot.getId() == Integer.parseInt(robotId))
                .forEach(Robot::stop);
    }

    private void activateRobot(String robotId) {
        onTaskLog("Activating robot: " + robotId);
        grid.getRobots().stream()
                .filter(robot -> robot.getId() == Integer.parseInt(robotId))
                .forEach(robot -> {
                    robot.start();
                    if (supervisor != null) {
                        supervisor.startRobotThread(robot, supervisor.getRobotTaskQueues().get(robot));
                    }
                });
    }

    // 按钮渲染器
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // 按钮编辑器
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = value == null ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                String robotId = tableModel.getValueAt(robotStatusTable.getEditingRow(), 0).toString();
                if ("Deactivate".equals(label)) {
                    deactivateRobot(robotId);
                } else if ("Activate".equals(label)) {
                    activateRobot(robotId);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
