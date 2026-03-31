# 多阶段构建 - 构建阶段
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制pom.xml和mvnw
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下载依赖（利用Docker缓存层）
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-jammy

# 创建应用用户（安全最佳实践）
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/target/PlayerSessionServer-0.0.1-SNAPSHOT.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+UseCompressedClassPointers"

# 应用配置
ENV SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20 \
    SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5 \
    SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000 \
    SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=600000 \
    SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=1800000 \
    SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE=50 \
    SPRING_JPA_PROPERTIES_HIBERNATE_ORDER_INSERTS=true \
    SPRING_JPA_PROPERTIES_HIBERNATE_ORDER_UPDATES=true \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_ORG_LYC122_DEV_PLAYERSESSIONSERVER=INFO

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]