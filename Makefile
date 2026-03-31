# PlayerSessionServer Makefile
# 简化Docker操作的Makefile

.PHONY: help build up down restart logs ps clean test backup restore deploy

help: ## 显示帮助信息
	@echo "PlayerSessionServer Docker操作命令："
	@echo ""
	@echo "构建和运行："
	@echo "  make build      - 构建Docker镜像"
	@echo "  make up         - 启动所有服务"
	@echo "  make up-dev     - 启动开发环境（含phpMyAdmin）"
	@echo "  make down       - 停止所有服务"
	@echo "  make restart    - 重启所有服务"
	@echo ""
	@echo "监控和日志："
	@echo "  make logs       - 查看所有服务日志"
	@echo "  make logs-app   - 查看应用日志"
	@echo "  make logs-mysql  - 查看MySQL日志"
	@echo "  make ps         - 查看服务状态"
	@echo "  make health     - 检查应用健康状态"
	@echo ""
	@echo "管理命令："
	@echo "  make clean      - 清理Docker资源"
	@echo "  make backup     - 备份数据库"
	@echo "  make restore    - 恢复数据库"
	@echo "  make shell      - 进入应用容器"
	@echo "  make mysql      - 连接到MySQL"
	@echo ""
	@echo "开发和测试："
	@echo "  make test       - 运行测试"
	@echo "  make deploy     - 生产环境部署"
	@echo ""
	@echo "其他："
	@echo "  make build-no-cache  - 不使用缓存构建镜像"
	@echo "  make stats      - 查看容器资源使用"

build: ## 构建Docker镜像
	docker-compose build

build-no-cache: ## 不使用缓存构建镜像
	docker-compose build --no-cache

up: ## 启动所有服务（生产环境）
	docker-compose up -d

up-dev: ## 启动开发环境（含phpMyAdmin）
	docker-compose --profile dev up -d

down: ## 停止所有服务
	docker-compose down

down-v: ## 停止服务并删除数据卷
	docker-compose down -v

restart: ## 重启所有服务
	docker-compose restart

logs: ## 查看所有服务日志
	docker-compose logs -f

logs-app: ## 查看应用日志
	docker-compose logs -f app

logs-mysql: ## 查看MySQL日志
	docker-compose logs -f mysql

ps: ## 查看服务状态
	docker-compose ps

health: ## 检查应用健康状态
	@echo "检查应用健康状态..."
	@curl -f http://localhost:8080/actuator/health || echo "应用未就绪或不可访问"

clean: ## 清理Docker资源
	@echo "清理Docker资源..."
	docker-compose down -v
	docker system prune -f
	docker volume prune -f

shell: ## 进入应用容器
	docker exec -it player-session-server bash

mysql: ## 连接到MySQL
	docker exec -it player-session-mysql \
		mysql -u player_user -pplayer_password player_session_db

mysql-root: ## 连接到MySQL（root用户）
	docker exec -it player-session-mysql \
		mysql -u root -prootpassword123

backup: ## 备份数据库
	@echo "备份数据库..."
	docker exec player-session-mysql \
		mysqldump -u root -prootpassword123 \
		player_session_db > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "数据库备份完成：backup_$(shell date +%Y%m%d_%H%M%S).sql"

restore: ## 恢复数据库
	@echo "恢复数据库..."
	@if [ -z "$(FILE)" ]; then \
		echo "错误：请指定要恢复的SQL文件"; \
		echo "用法: make restore FILE=backup_file.sql"; \
		exit 1; \
	fi
	docker exec -i player-session-mysql \
		mysql -u root -prootpassword123 \
		player_session_db < $(FILE)
	@echo "数据库恢复完成"

test: ## 运行测试
	@echo "运行测试..."
	./mvnw test

stats: ## 查看容器资源使用
	docker stats player-session-server player-session-mysql

init: ## 初始化项目
	@echo "初始化项目..."
	cp .dockerenv.example .env
	@echo "环境变量配置文件已创建：.env"
	@echo "请根据需要修改配置"

deploy: ## 生产环境部署
	@echo "部署到生产环境..."
	@echo "1. 构建镜像..."
	docker-compose build
	@echo "2. 启动服务..."
	docker-compose up -d
	@echo "3. 检查健康状态..."
	sleep 10
	curl -f http://localhost:8080/actuator/health
	@echo "部署完成！"

rebuild: ## 重新构建并启动
	@echo "重新构建并启动..."
	docker-compose down
	docker-compose build --no-cache
	docker-compose up -d
	@echo "服务已重新构建并启动"

update: ## 拉取最新代码并重新部署
	@echo "更新并重新部署..."
	git pull
	./mvnw clean package -DskipTests
	docker-compose build
	docker-compose up -d
	@echo "更新完成！"