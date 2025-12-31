# RoboFlow Warehouse - 智能仓储机器人调度系统

一个基于多线程并发调度的智能仓储机器人模拟系统。

---

## 项目简介

RoboFlow Warehouse 是一款智能仓储物流机器人调度模拟系统，实现了多机器人在仓库环境中的自主导航、任务分配、货物搬运和充电管理等核心功能。

本项目采用标准 Maven 工程结构，遵循分层架构设计（Entity/Model/Service/View/Config/Repository），具备良好的可扩展性和可维护性。

## 技术栈

- **语言**: Java 21
- **构建工具**: Maven 3.6+
- **数据库**: MySQL 8.0+
- **GUI框架**: Java Swing
- **并发框架**: java.util.concurrent

## 核心特性

### 1. 智能调度算法
- **BFS 路径规划**: 机器人自动计算最短路径，绕开货架障碍物
- **最优任务分配**: 基于机器人负载均衡的动态任务分配策略
- **冲突避免**: 多机器人并发执行任务时的智能协调

### 2. 高性能并发架构
- **线程池管理**: 使用 `ExecutorService` 高效管理机器人工作线程
- **线程安全队列**: `LinkedBlockingQueue` 实现无锁任务队列
- **原子操作**: `AtomicInteger` 确保 ID 生成的线程安全

### 3. 数据持久化
- **MySQL 数据库**: 产品信息实时持久化存储
- **JDBC 连接**: 高效的数据库连接管理
- **事务保障**: 确保入库/出库操作的数据一致性

### 4. 可视化监控
- **实时动画**: 机器人移动轨迹的实时可视化
- **状态面板**: 机器人状态、任务日志的实时更新
- **交互控制**: 支持手动激活/停用机器人

## 项目结构

```
com.warehouse
├── Main.java                 # 应用入口
├── config/
│   └── ConfigLoader.java     # 配置加载器（CSV解析）
├── entity/
│   └── Robot.java            # 机器人实体（BFS寻路、任务执行）
├── model/
│   ├── Product.java          # 产品模型
│   └── Task.java             # 任务模型
├── service/
│   ├── Supervisor.java       # 任务调度器（核心调度逻辑）
│   ├── SupplyUnit.java       # 供货单元（模拟入库）
│   ├── CustomerOrderUnit.java# 订单单元（模拟出库）
│   └── TaskQueue.java        # 线程安全任务队列
├── repository/
│   └── DatabaseManager.java  # 数据库操作层
└── view/
    ├── SupervisorGUI.java    # 主界面
    ├── WarehouseGrid.java    # 仓库网格视图
    └── LogListener.java      # 日志监听接口
```


## 环境配置

### 1. 安装 JDK 21

下载地址:
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/
- OpenJDK: https://adoptium.net/

配置环境变量:
```
JAVA_HOME = C:\Program Files\Java\jdk-21
Path 添加 %JAVA_HOME%\bin
```

验证安装:
```bash
java -version
```

### 2. 安装 Maven

下载地址: https://maven.apache.org/download.cgi

配置环境变量:
```
MAVEN_HOME = C:\apache-maven-3.9.x
Path 添加 %MAVEN_HOME%\bin
```

验证安装:
```bash
mvn -version
```

### 3. 配置 MySQL

1. 下载安装 MySQL 8.0+: https://dev.mysql.com/downloads/mysql/

2. 创建数据库和表:
```sql
CREATE DATABASE db01;
USE db01;

CREATE TABLE Products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100),
    Category VARCHAR(100),
    GroupID VARCHAR(100),
    LocationX INT,
    LocationY INT,
    Weight DOUBLE
);
```

3. 如需修改数据库连接信息，编辑 `DatabaseManager.java`:
```java
private final String url = "jdbc:mysql://localhost:3306/db01";
private final String username = "root";
private final String password = "";
```

## 运行项目

```bash
# 编译项目
mvn compile

# 运行项目
mvn exec:java -Dexec.mainClass="com.warehouse.Main"

# 或直接运行
java -cp target/classes com.warehouse.Main
```

## 配置文件说明

`ConfigFile.csv` 格式:
```
16,21           # 仓库尺寸: 宽度,高度
5               # 货架组数量
2               # 入库柜台数量
2               # 出库柜台数量
3               # 充电桩数量
3,10,20,30      # 机器人数量,容量1,容量2,容量3
10,10,10        # 各机器人速度
```

## 界面说明

运行后将看到:
- 左侧: 仓库俯视图，显示货架（灰色）、机器人（彩色图标）、柜台和充电桩
- 右侧: 机器人状态表格、系统消息、任务日志

## 开发命令

```bash
# 编译
mvn clean compile

# 打包
mvn package

# 运行测试
mvn test
```

## License

本项目仅供学习和研究使用。
