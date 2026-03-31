# PlayerSessionServer

一个用于Minecraft多服集群的玩家登录认证和状态管理系统，支持跨服务器的玩家身份验证和状态同步。

## 项目简介

PlayerSessionServer提供了一个统一的认证中心，用于管理Minecraft多服务器环境中的玩家登录、状态跟踪和会话管理。玩家首次加入任意服务器前，需通过网页获取专属AccessToken，然后在游戏内输入验证。

## 核心功能

- ✅ **玩家管理**: 玩家注册、信息查询、冲突检测
- ✅ **Token认证**: 安全的AccessToken生成、验证和管理
- ✅ **会话管理**: 玩家在线状态跟踪、服务器位置记录
- ✅ **多服务器支持**: 支持多个Minecraft服务器同时在线
- ✅ **Token失效**: 支持手动使token失效，提供安全保护
- ✅ **历史记录**: 完整的token操作历史和审计追踪

## 技术栈

- **框架**: Spring Boot 4.0.5
- **语言**: Java 21
- **数据库**: MySQL 8.4.8
- **ORM**: Spring Data JPA + Hibernate
- **安全**: Spring Security
- **构建工具**: Maven

## 系统架构

采用分层架构设计：

```
┌─────────────┐
│ Controller  │ ← REST API端点
├─────────────┤
│   Service   │ ← 业务逻辑层
├─────────────┤
│  Repository │ ← 数据访问层
├─────────────┤
│  Database   │ ← MySQL
└─────────────┘
```

## 快速开始

### 环境要求

- Java 21+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/Ericlyclyclyc/PlayerSessionServer.git
cd PlayerSessionServer
```

2. **配置数据库**

创建MySQL数据库：
```sql
CREATE DATABASE player_session_db;
CREATE USER 'player_user'@'localhost' IDENTIFIED BY 'player_password';
GRANT ALL PRIVILEGES ON player_session_db.* TO 'player_user'@'localhost';
FLUSH PRIVILEGES;
```

更新数据库表结构（添加REVOKED事件类型）：
```sql
USE player_session_db;
ALTER TABLE token_events 
MODIFY COLUMN event_type 
ENUM('EXPIRED','GENERATED','INVALIDATED','RENEWED','VALIDATED','REVOKED') 
NOT NULL;
```

3. **修改配置文件**

编辑 `src/main/resources/application.properties`，修改数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/player_session_db
spring.datasource.username=player_user
spring.datasource.password=player_password
```

4. **构建项目**
```bash
./mvnw clean package
```

5. **运行应用**
```bash
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

或者使用Maven：
```bash
./mvnw spring-boot:run
```

6. **验证安装**

访问健康检查端点：
```bash
curl http://localhost:8080/api/players/TestPlayer
```

---

## Java JAR部署

本文档详细介绍使用传统Java JAR方式部署PlayerSessionServer系统。

### 系统要求

#### 必需软件
- **Java**: JDK 21或更高版本
- **Maven**: 3.6+ （用于构建项目）
- **MySQL**: 8.0或更高版本

#### 推荐配置
- **CPU**: 2核或更多
- **内存**: 4GB或更多
- **磁盘**: 20GB或更多

### 详细部署步骤

#### 1. 数据库配置

**创建数据库和用户**：
```bash
# 登录MySQL
mysql -u root -p

# 执行以下SQL命令
```

```sql
-- 创建数据库
CREATE DATABASE player_session_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户
CREATE USER 'player_user'@'localhost' 
  IDENTIFIED BY 'player_password';

-- 授予权限
GRANT ALL PRIVILEGES ON player_session_db.* 
TO 'player_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

**更新表结构**：
```bash
# 执行初始化脚本
mysql -u player_user -pplayer_password player_session_db < init-scripts/01-init-database.sql
```

#### 2. 应用配置

**修改数据库连接配置**：

编辑 `src/main/resources/application.properties`：

```properties
# 服务器配置
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/player_session_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=player_user
spring.datasource.password=player_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false

# 日志配置
logging.level.root=INFO
logging.level.org.lyc122.dev.playersessionserver=INFO
logging.file.name=logs/application.log
```

**生产环境配置**：

如果使用生产环境配置，创建 `src/main/resources/application-prod.properties` 并设置：

```properties
# 使用生产环境配置
spring.profiles.active=prod

# 数据库连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# JVM参数
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
```

#### 3. 构建项目

**清理并构建**：
```bash
# 清理之前的构建
./mvnw clean

# 打包项目（跳过测试）
./mvnw package -DskipTests
```

**验证构建结果**：
```bash
# 检查jar文件是否存在
ls -lh target/PlayerSessionServer-0.0.1-SNAPSHOT.jar

# 查看jar文件内容（可选）
jar tf target/PlayerSessionServer-0.0.1-SNAPSHOT.jar | head -20
```

#### 4. 启动应用

**前台运行（开发环境）**：
```bash
# 使用默认配置运行
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar

# 指定配置文件运行
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 自定义JVM参数运行
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

**后台运行（生产环境）**：

**Linux/Mac**：
```bash
# 使用nohup后台运行
nohup java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  > logs/application.log 2>&1 &

# 或者使用screen/tmux
screen -S player-session
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
# 按 Ctrl+A+D 分离会话

# 查看进程
ps aux | grep PlayerSessionServer

# 停止应用
kill $(ps aux | grep PlayerSessionServer | grep -v grep | awk '{print $2}')
```

**Windows**：
```powershell
# 使用Start-Process后台运行
Start-Process -FilePath "java" -ArgumentList "-jar","target/PlayerSessionServer-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod" -WindowStyle Hidden

# 或者使用后台任务
$process = Start-Process -FilePath "java" -ArgumentList "-jar","target/PlayerSessionServer-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod" -PassThru
$process | Stop-Process -Force

# 创建Windows服务（可选）
# 使用NSSM或winsw工具将应用注册为Windows服务
```

#### 5. 创建启动脚本

**Linux/Mac启动脚本** (`start.sh`)：
```bash
#!/bin/bash

APP_NAME="PlayerSessionServer"
JAR_FILE="target/PlayerSessionServer-0.0.1-SNAPSHOT.jar"
PID_FILE="$APP_NAME.pid"
LOG_FILE="logs/application.log"

# 启动函数
start_app() {
    echo "正在启动 $APP_NAME..."
    
    # 检查是否已运行
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null; then
            echo "$APP_NAME 已经在运行中 (PID: $PID)"
            exit 1
        else
            rm -f $PID_FILE
        fi
    fi
    
    # 创建日志目录
    mkdir -p logs
    
    # 启动应用
    nohup java -jar $JAR_FILE \
        --spring.profiles.active=prod \
        > $LOG_FILE 2>&1 &
    
    echo $! > $PID_FILE
    echo "$APP_NAME 已启动 (PID: $(cat $PID_FILE))"
}

# 停止函数
stop_app() {
    if [ ! -f "$PID_FILE" ]; then
        echo "$APP_NAME 未运行"
        exit 1
    fi
    
    PID=$(cat $PID_FILE)
    kill $PID
    rm -f $PID_FILE
    echo "$APP_NAME 已停止"
}

# 重启函数
restart_app() {
    stop_app
    sleep 2
    start_app
}

# 状态检查函数
status_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null; then
            echo "$APP_NAME 正在运行 (PID: $PID)"
            echo "内存使用: $(ps -p $PID -o rss= | awk '{print $1/1024 " MB"}') MB"
            echo "CPU使用: $(ps -p $PID -o %cpu=)%"
        else
            echo "$APP_NAME 已停止"
            rm -f $PID_FILE
        fi
    else
        echo "$APP_NAME 未运行"
    fi
}

# 主程序
case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    status)
        status_app
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
```

**使用启动脚本**：
```bash
# 赋予执行权限
chmod +x start.sh

# 启动应用
./start.sh start

# 查看状态
./start.sh status

# 停止应用
./start.sh stop

# 重启应用
./start.sh restart
```

#### 6. 日志管理

**查看日志**：
```bash
# 实时查看日志
tail -f logs/application.log

# 查看最近100行日志
tail -n 100 logs/application.log

# 搜索错误日志
grep -i "error" logs/application.log

# 搜索特定时间段的日志
grep "2026-03-31" logs/application.log
```

#### 7. 系统服务配置

**创建Systemd服务**（Linux）：

创建 `/etc/systemd/system/player-session-server.service`：

```ini
[Unit]
Description=PlayerSession Server
After=network.target mysql.service

[Service]
Type=simple
User=appuser
Group=appuser
WorkingDirectory=/opt/PlayerSessionServer
ExecStart=/usr/bin/java -jar /opt/PlayerSessionServer/target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=append:/opt/PlayerSessionServer/logs/application.log
StandardError=append:/opt/PlayerSessionServer/logs/application.log

[Install]
WantedBy=multi-user.target
```

**管理服务**：
```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启用服务
sudo systemctl enable player-session-server

# 启动服务
sudo systemctl start player-session-server

# 查看服务状态
sudo systemctl status player-session-server

# 查看服务日志
sudo journalctl -u player-session-server -f

# 停止服务
sudo systemctl stop player-session-server

# 重启服务
sudo systemctl restart player-session-server
```

#### 8. 性能优化

**JVM参数优化**：
```bash
# 基础配置
-Xms512m              # 初始堆内存
-Xmx1024m             # 最大堆内存
-XX:+UseG1GC          # 使用G1垃圾收集器
-XX:MaxGCPauseMillis=200 # 最大GC暂停时间

# 性能调优
-XX:+UseStringDeduplication       # 字符串去重
-XX:+OptimizeStringConcat       # 字符串连接优化
-XX:+UseCompressedOops         # 压缩普通对象指针
-XX:+UseCompressedClassPointers # 压缩类指针
-XX:InitiatingHeapOccupancyPercent=40 # 初始堆占用率
```

#### 9. 监控和维护

**健康检查**：
```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 检查应用信息
curl http://localhost:8080/actuator/info

# 检查Prometheus指标
curl http://localhost:8080/actuator/prometheus
```

**数据库备份**：
```bash
# 备份数据库
mysqldump -u player_user -pplayer_password \
  --single-transaction \
  --routines --triggers \
  player_session_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### 10. 故障排除

**常见问题解决**：

**Q: 应用启动失败**
```bash
# 检查Java版本
java -version

# 检查端口占用
netstat -ano | findstr :8080

# 检查MySQL连接
mysql -u player_user -pplayer_password -e "SELECT 1;" player_session_db

# 查看应用日志
tail -100 logs/application.log
```

**Q: 数据库连接失败**
```bash
# 测试数据库连接
mysql -h localhost -P 3306 -u player_user -p

# 检查MySQL服务
systemctl status mysql
```

**Q: 内存不足**
```bash
# 查看JVM内存使用
jmap -heap <pid>

# 增加堆内存
java -Xms1024m -Xmx2048m -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

**Q: 端口冲突**
```bash
# 修改application.properties中的端口
server.port=8081

# 或者在启动时指定端口
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --server.port=8081
```

### 部署检查清单

- [ ] Java 21+已安装
- [ ] MySQL 8.0+已安装并运行
- [ ] 数据库和用户已创建
- [ ] 表结构已更新
- [ ] application.properties已配置
- [ ] 项目已成功构建
- [ ] 应用可以正常启动
- [ ] API端点可以正常访问
- [ ] 数据库连接正常
- [ ] 日志输出正常
- [ ] 健康检查端点正常

### 与Docker部署对比

| 特性 | Docker部署 | JAR部署 |
|------|-----------|----------|
| **环境配置** | 自动化 | 手动配置 |
| **依赖管理** | 容器内包含 | 需要手动安装 |
| **部署速度** | 快速 | 较慢 |
| **资源隔离** | 容器级别 | 进程级别 |
| **扩展性** | 易于扩展 | 需要手动配置 |
| **监控** | 需要额外配置 | 可直接监控 |
| **灵活性** | 较低 | 很高 |
| **学习成本** | 需要Docker知识 | 传统Java部署 |

### 推荐选择

- **新手/快速部署**: Docker部署
- **生产环境**: Docker部署（推荐）
- **现有Java环境**: JAR部署
- **需要自定义配置**: JAR部署
- **多环境管理**: Docker部署

---

## API文档

详细的API文档请参考：[API-DOC-v3.md](API-DOC-v3.md)

### 主要API端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/players/register` | POST | 注册新玩家 |
| `/api/tokens/generate` | POST | 获取AccessToken |
| `/api/tokens/validate` | POST | 验证Token |
| `/api/tokens/revoke` | POST | 使Token失效 |
| `/api/players/logout` | POST | 玩家退出 |
| `/api/players/{identifier}` | GET | 查询玩家信息 |
| `/api/tokens/history` | GET | 查询Token历史 |

## 使用示例

### 完整的玩家登录流程

1. **注册玩家**
```bash
curl -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "password": "password123"
  }'
```

2. **获取Token**
```bash
curl -X POST http://localhost:8080/api/tokens/generate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

3. **验证Token**
```bash
curl -X POST http://localhost:8080/api/tokens/validate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "token": "your-token-here",
    "serverName": "SurvivalServer"
  }'
```

## Token管理规则

### Token有效期
- **初始有效期**: 3分钟（从生成时间开始）
- **续期机制**: 每次成功验证会重置有效期
- **超时处理**: 超过3分钟未使用自动失效

### Token唯一性
- 每个玩家在同一时间只能有一个有效token
- 生成新token会自动失效所有旧token
- 旧token的失效会记录INVALIDATED事件

### Token失效方式
1. **自动失效**: 生成新token时、超时（3分钟未使用）
2. **手动失效**: 调用`/tokens/revoke`接口，记录REVOKED事件

### Token历史查询

#### 功能说明
Token历史查询API支持使用游标进行高效分页查询，避免了传统offset分页的性能问题。

#### API端点
```
GET /api/tokens/history
```

#### 参数说明
- **playerId**（必填）: 玩家ID
- **limit**（可选）: 返回记录数量，默认10，最大100
- **cursor**（可选）: 查询终点的时间戳（ISO格式，如2026-03-30T10:00:00），用于分页
- **startTime**（可选）: 开始时间（ISO格式）
- **endTime**（可选）: 结束时间（ISO格式）

#### 查询示例

**1. 查询最新的10条记录（默认）**
```bash
curl "http://localhost:8080/api/tokens/history?playerId=1"
```

**2. 查询最新的20条记录**
```bash
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=20"
```

**3. 使用游标分页查询**
```bash
# 第一次查询，获取最新的5条记录
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=5"

# 使用上一批最后一条记录的时间作为cursor，查询更早的记录
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=5&cursor=2026-03-30T10:00:00"
```

**4. 结合时间范围查询**
```bash
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=10&startTime=2026-03-01T00:00:00&endTime=2026-03-31T23:59:59"
```

#### 响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 15,
      "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "eventType": "VALIDATED",
      "serverName": "SurvivalServer",
      "eventTime": "2026-03-31T10:30:00"
    },
    {
      "id": 14,
      "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "eventType": "GENERATED",
      "serverName": null,
      "eventTime": "2026-03-31T10:25:00"
    }
  ]
}
```

#### 事件类型
- **GENERATED**: Token生成
- **VALIDATED**: Token验证成功
- **RENEWED**: Token续期
- **INVALIDATED**: Token被失效（生成新token时）
- **REVOKED**: Token被撤销（手动调用）
- **EXPIRED**: Token超时失效

#### 游标分页原理
1. 不提供cursor：查询最新的N条记录
2. 提供cursor：查询该时间戳之前的N条记录
3. 每页最后一条记录的`eventTime`可作为下一页的`cursor`
4. 这种方式避免了传统offset分页的性能问题

## 开发指南

### 项目结构

```
src/main/java/org/lyc122/dev/playersessionserver/
├── config/           # 配置类
├── controller/       # REST控制器
├── dto/             # 数据传输对象
├── entity/          # 数据库实体
├── exception/       # 异常处理
├── repository/      # 数据访问层
├── security/        # 安全配置
└── service/         # 业务逻辑层
```

### 运行测试

```bash
# 运行所有测试
./mvnw test

# 跳过测试构建
./mvnw clean package -DskipTests
```

### 开发环境配置

当前开发环境配置：
- 服务器端口: 8080
- 数据库: MySQL 8.4.8
- API Key认证: 临时禁用（测试用）

生产环境需要：
- 启用API Key认证
- 使用HTTPS
- 配置数据库连接池
- 启用日志记录和监控

## 部署方式

本项目支持两种部署方式，您可以根据需求选择适合的方式：

### 方式一：Docker部署（推荐）

**适用场景**：
- 生产环境部署
- 快速部署和测试
- 需要容器化环境
- 多环境管理

**优势**：
- ✅ 环境一致性保证
- ✅ 快速部署和扩展
- ✅ 资源隔离和限制
- ✅ 简化运维管理
- ✅ 包含数据库和应用

**快速开始**：
```bash
# 1. 复制环境变量配置
cp .dockerenv.example .env

# 2. 启动服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps
```

**详细信息**：请参考下方的[详细Docker部署](#docker部署)章节。

---

### 方式二：Java JAR部署

**适用场景**：
- 传统服务器环境
- 已有MySQL数据库
- 需要自定义JVM参数
- 集成到现有Java应用

**优势**：
- ✅ 灵活配置JVM参数
- ✅ 可与现有系统集成
- ✅ 更细粒度的日志控制
- ✅ 适合传统运维环境

**快速开始**：
```bash
# 1. 配置数据库（创建数据库和用户）
mysql -u root -p
```

```sql
CREATE DATABASE player_session_db;
CREATE USER 'player_user'@'localhost' IDENTIFIED BY 'player_password';
GRANT ALL PRIVILEGES ON player_session_db.* TO 'player_user'@'localhost';
FLUSH PRIVILEGES;
```

```bash
# 2. 更新表结构
mysql -u player_user -pplayer_password player_session_db < init-scripts/01-init-database.sql

# 3. 修改配置文件
# 编辑 src/main/resources/application.properties
# 更新数据库连接信息

# 4. 构建项目
./mvnw clean package

# 5. 运行应用
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

**详细信息**：请参考[Java JAR部署](#java-jar部署)章节。

---

## Docker部署

### 使用Docker Compose（推荐）

#### 开发环境部署（含phpMyAdmin）
```bash
# 复制环境变量配置
cp .dockerenv.example .env

# 启动所有服务
docker-compose --profile dev up -d

# 查看日志
docker-compose logs -f app

# 访问phpMyAdmin
# http://localhost:8081
# 用户: root / rootpassword123
```

#### 生产环境部署
```bash
# 复制环境变量配置
cp .dockerenv.example .env

# 启动核心服务（不含phpMyAdmin）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看应用日志
docker-compose logs -f app

# 查看MySQL日志
docker-compose logs -f mysql
```

### 手动构建和运行

```bash
# 构建镜像
docker build -t player-session-server:latest .

# 运行容器
docker run -d \
  --name player-session-server \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/player_session_db \
  -e SPRING_DATASOURCE_USERNAME=player_user \
  -e SPRING_DATASOURCE_PASSWORD=player_password \
  player-session-server:latest
```

### Docker配置说明

#### 服务架构
- **MySQL 8.4**: 数据库服务，端口3306
- **PlayerSessionServer**: 应用服务，端口8080
- **phpMyAdmin**: 数据库管理工具（仅开发环境），端口8081

#### 数据库连接信息
- **主机**: mysql（容器内）或 localhost（主机）
- **端口**: 3306
- **数据库**: player_session_db
- **用户**: player_user
- **密码**: player_password

#### 数据持久化
- MySQL数据: Docker volume `mysql-data`
- 应用日志: Docker volume `app-logs`

#### 健康检查
- MySQL: 每10秒检查一次，超时5秒
- 应用: 每30秒检查一次，超时10秒

#### 资源限制
- CPU: 最多2核，保留0.5核
- 内存: 最多1.5G，保留512M

### Docker管理命令

```bash
# 查看所有容器状态
docker-compose ps

# 查看服务日志
docker-compose logs -f [service-name]

# 重启服务
docker-compose restart [service-name]

# 停止服务
docker-compose stop [service-name]

# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷
docker-compose down -v

# 进入容器
docker exec -it player-session-server bash

# 查看容器资源使用
docker stats player-session-server
```

### 数据库操作

```bash
# 备份数据库
docker exec player-session-mysql \
  mysqldump -u root -prootpassword123 \
  player_session_db > backup.sql

# 恢复数据库
docker exec -i player-session-mysql \
  mysql -u root -prootpassword123 \
  player_session_db < backup.sql

# 连接到MySQL
docker exec -it player-session-mysql \
  mysql -u player_user -pplayer_password player_session_db

# 查看数据库表
docker exec -it player-session-mysql \
  mysql -u player_user -pplayer_password \
  -e "SHOW TABLES;" player_session_db
```

### 监控和维护

```bash
# 查看应用健康状态
curl http://localhost:8080/actuator/health

# 查看应用信息
curl http://localhost:8080/actuator/info

# 查看Prometheus指标
curl http://localhost:8080/actuator/prometheus

# 查看应用日志
docker exec -it player-session-server \
  tail -f /app/logs/application.log
```

### 环境变量配置

创建 `.env` 文件（基于 `.dockerenv.example`）：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=rootpassword123
MYSQL_DATABASE=player_session_db
MYSQL_USER=player_user
MYSQL_PASSWORD=player_password

# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# JVM配置
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC

# 时区配置
TZ=Asia/Shanghai
```

### 故障排除

**Q: 容器启动失败**
```bash
# 查看详细日志
docker-compose logs [service-name]

# 检查容器状态
docker-compose ps

# 重新构建镜像
docker-compose build --no-cache [service-name]
```

**Q: 数据库连接失败**
```bash
# 检查MySQL容器状态
docker-compose ps mysql

# 测试数据库连接
docker exec -it player-session-mysql \
  mysql -u player_user -pplayer_password -e "SELECT 1;"

# 检查网络配置
docker network inspect player-session-player-session-network
```

**Q: 应用无法访问**
```bash
# 检查端口映射
docker port player-session-server

# 检查健康检查状态
docker inspect player-session-server --format='{{json .State.Health}}'

# 查看防火墙规则
netstat -ano | findstr :8080
```

### 更多信息

详细的Docker配置说明请参考：[DOCKER-UPDATE.md](DOCKER-UPDATE.md)

## 安全特性

- ✅ 密码使用BCrypt加密存储
- ✅ Token使用UUID格式，难以猜测
- ✅ 完整的权限验证和所有权检查
- ✅ 记录所有token操作事件用于审计
- ✅ 支持手动token失效，提供安全保护

## 监控和日志

- 应用日志：控制台输出
- 数据库日志：MySQL日志
- 建议集成：Prometheus + Grafana

## 故障排除

### 常见问题

**Q: 数据库连接失败**
- 检查MySQL服务是否启动
- 确认数据库连接信息正确
- 验证用户权限

**Q: Token验证失败**
- 确认token未过期（3分钟有效期）
- 检查token是否属于该玩家
- 验证请求参数格式正确

**Q: 端口占用**
- 修改`application.properties`中的`server.port`
- 检查端口是否被其他应用占用

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 版本历史

### v3.0 (2026-03-31)
- 添加Token失效功能
- 添加REVOKED事件类型
- 修复DTO字段命名
- 改进错误处理和参数验证
- 数据库从H2迁移到MySQL

### v2.0 (2026-03-30)
- 完整的玩家注册和认证功能
- Token生成和验证
- 会话管理
- 多服务器支持
- Token历史查询

### v1.0 (2026-03-29)
- 初始版本
- 基础的玩家管理功能

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

- 项目地址: https://github.com/your-username/PlayerSessionServer
- 问题反馈: https://github.com/your-username/PlayerSessionServer/issues

## 致谢

感谢所有为此项目做出贡献的开发者！

---

**注意**: 本项目目前处于开发阶段，API可能会发生变化。建议在生产环境使用前进行充分测试。