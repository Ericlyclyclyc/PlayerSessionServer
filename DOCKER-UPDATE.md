# Docker配置更新记录

## 更新时间
2026-03-31

## 更新内容

### 1. Dockerfile更新

#### 主要变更
- **多阶段构建**: 分离构建和运行阶段，减小最终镜像大小
- **安全性增强**: 
  - 创建专用应用用户 `appuser`
  - 避免以root用户运行应用
- **性能优化**:
  - 添加多个JVM优化参数
  - 启用G1垃圾收集器
  - 字符串去重和压缩优化
- **健康检查**: 添加应用健康检查端点
- **环境变量**: 添加生产环境配置选项

#### 技术细节
- 构建阶段使用 `maven:3.9.6-eclipse-temurin-21`
- 运行阶段使用 `eclipse-temurin:21-jre-jammy`
- 镜像大小优化（多阶段构建）
- 日志目录权限设置

### 2. docker-compose.yml更新

#### 主要变更
- **数据库更新**:
  - 升级到MySQL 8.4
  - 更新数据库连接配置（匹配实际配置）
  - 添加MySQL性能优化参数
  - 修改时区设置
- **应用服务优化**:
  - 添加资源限制（CPU和内存）
  - 添加应用健康检查
  - 优化连接池配置
  - 添加日志卷挂载
- **网络配置**:
  - 自定义子网配置
  - 网络隔离
- **新增服务**: phpMyAdmin（仅开发环境）

#### 技术细节
- MySQL健康检查：30秒间隔
- 应用健康检查：30秒间隔，60秒启动时间
- 资源限制：2CPU核，1.5G内存
- 数据卷持久化配置

### 3. .dockerignore更新

#### 主要变更
- 添加更多IDE相关文件
- 添加日志文件和目录
- 添加测试相关文件
- 添加Docker相关文件
- 添加环境变量文件
- 优化构建缓存

#### 忽略内容
- Maven构建产物和缓存
- IDE配置文件
- 日志文件
- Git配置
- Docker相关文件
- 测试文件
- 文档文件（部分）

### 4. 新增文件

#### init-scripts/01-init-database.sql
- **用途**: Docker启动时自动执行
- **功能**:
  - 修改token_events表结构（添加REVOKED类型）
  - 创建数据库索引优化查询性能
  - 显示表结构和索引信息

#### application-prod.properties
- **用途**: 生产环境配置文件
- **功能**:
  - 数据库连接池优化
  - JPA性能优化
  - 日志配置
  - 监控端点配置
  - 性能参数调优

## 部署说明

### 开发环境部署

```bash
# 启动所有服务（包括phpMyAdmin）
docker-compose --profile dev up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

### 生产环境部署

```bash
# 启动核心服务（不含phpMyAdmin）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
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
  player-session-server:latest
```

## 数据库连接信息

### 生产环境配置
- **主机**: mysql
- **端口**: 3306
- **数据库**: player_session_db
- **用户**: player_user
- **密码**: player_password

### 外部访问配置
如需从主机访问数据库：
```bash
mysql -h localhost -P 3306 -u player_user -p
```

## 监控和维护

### 健康检查端点
- **应用健康**: http://localhost:8080/actuator/health
- **应用信息**: http://localhost:8080/actuator/info
- **Prometheus指标**: http://localhost:8080/actuator/prometheus

### 日志查看
```bash
# 查看应用日志
docker-compose logs -f app

# 查看MySQL日志
docker-compose logs -f mysql

# 进入容器查看日志
docker exec -it player-session-server tail -f /app/logs/application.log
```

### 数据备份
```bash
# 备份数据库
docker exec player-session-mysql \
  mysqldump -u root -prootpassword123 \
  player_session_db > backup.sql

# 恢复数据库
docker exec -i player-session-mysql \
  mysql -u root -prootpassword123 \
  player_session_db < backup.sql
```

## 性能优化

### 数据库优化
- InnoDB缓冲池大小: 512M
- 最大连接数: 200
- 批处理启用
- 索引优化

### 应用优化
- JVM堆内存: 512MB-1024MB
- G1垃圾收集器
- 字符串优化
- 连接池优化

### 网络优化
- 自定义子网隔离
- 桥接网络驱动
- 网络性能优化

## 安全配置

### 容器安全
- 非root用户运行
- 最小化镜像大小
- 安全的基础镜像

### 网络安全
- 内部网络隔离
- 仅暴露必要端口
- 服务间通信加密

### 数据安全
- 数据卷持久化
- 定期备份机制
- 访问权限控制

## 故障排除

### 常见问题

**Q: 容器启动失败**
- 检查端口是否被占用
- 确认Docker服务运行正常
- 查看容器日志：`docker-compose logs`

**Q: 数据库连接失败**
- 确认MySQL容器健康状态
- 检查网络配置
- 验证连接参数

**Q: 应用无法访问**
- 检查端口映射
- 确认防火墙设置
- 查看健康检查状态

## 维护建议

### 定期任务
- 数据库备份（每周）
- 日志清理（每月）
- 容器镜像更新（每月）
- 安全扫描（季度）

### 监控指标
- CPU使用率
- 内存使用率
- 响应时间
- 错误率
- 数据库连接数

## 更新检查清单

- [x] Dockerfile多阶段构建
- [x] 安全用户配置
- [x] 健康检查配置
- [x] 资源限制配置
- [x] 数据库连接优化
- [x] 连接池配置
- [x] 日志配置
- [x] 监控端点配置
- [x] 网络配置优化
- [x] 数据库初始化脚本
- [x] 生产环境配置文件
- [x] 文档更新