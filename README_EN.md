# PlayerSessionServer

A player authentication and state management system for Minecraft multi-server clusters, supporting cross-server player identity verification and state synchronization.

## Project Overview

PlayerSessionServer provides a unified authentication center for managing player login, status tracking, and session management in multi-server Minecraft environments. Players must obtain a unique AccessToken through a web page before joining any server for the first time, then enter it for verification in-game.

## Core Features

- ✅ **Player Management**: Player registration, information lookup, conflict detection
- ✅ **Token Authentication**: Secure AccessToken generation, validation, and management
- ✅ **Session Management**: Player online status tracking, server location recording
- ✅ **Multi-server Support**: Support for multiple Minecraft servers simultaneously
- ✅ **Token Revocation**: Manual token revocation for security protection
- ✅ **History Tracking**: Complete token operation history and audit trails

## Tech Stack

- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **Database**: MySQL 8.4.8
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security
- **Build Tool**: Maven

## System Architecture

Adopts layered architecture design:

```
┌─────────────┐
│ Controller  │ ← REST API endpoints
├─────────────┤
│   Service   │ ← Business logic layer
├─────────────┤
│  Repository │ ← Data access layer
├─────────────┤
│  Database   │ ← MySQL
└─────────────┘
```

## Quick Start

### Requirements

- Java 21+
- Maven 3.6+
- MySQL 8.0+

### Installation

1. **Clone the project**
```bash
git clone https://github.com/your-username/PlayerSessionServer.git
cd PlayerSessionServer
```

2. **Configure Database**

Create MySQL database:
```sql
CREATE DATABASE player_session_db;
CREATE USER 'player_user'@'localhost' IDENTIFIED BY 'player_password';
GRANT ALL PRIVILEGES ON player_session_db.* TO 'player_user'@'localhost';
FLUSH PRIVILEGES;
```

Update database table structure (add REVOKED event type):
```sql
USE player_session_db;
ALTER TABLE token_events 
MODIFY COLUMN event_type 
ENUM('EXPIRED','GENERATED','INVALIDATED','RENEWED','VALIDATED','REVOKED') 
NOT NULL;
```

3. **Update Configuration**

Edit `src/main/resources/application.properties`, modify database connection:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/player_session_db
spring.datasource.username=player_user
spring.datasource.password=player_password
```

4. **Build Project**
```bash
./mvnw clean package
```

5. **Run Application**
```bash
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

Or using Maven:
```bash
./mvnw spring-boot:run
```

6. **Verify Installation**

Access health check endpoint:
```bash
curl http://localhost:8080/api/players/TestPlayer
```

---

## Java JAR Deployment

This document provides detailed instructions for deploying the PlayerSessionServer system using traditional Java JAR method.

### System Requirements

#### Required Software
- **Java**: JDK 21 or higher
- **Maven**: 3.6+ (for building the project)
- **MySQL**: 8.0 or higher

#### Recommended Configuration
- **CPU**: 2 cores or more
- **Memory**: 4GB or more
- **Disk**: 20GB or more

### Detailed Deployment Steps

#### 1. Database Configuration

**Create database and user**:
```bash
# Login to MySQL
mysql -u root -p

# Execute the following SQL commands
```

```sql
-- Create database
CREATE DATABASE player_session_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Create database user
CREATE USER 'player_user'@'localhost' 
  IDENTIFIED BY 'player_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON player_session_db.* 
TO 'player_user'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Exit
EXIT;
```

**Update table structure**:
```bash
# Execute initialization script
mysql -u player_user -pplayer_password player_session_db < init-scripts/01-init-database.sql
```

#### 2. Application Configuration

**Modify database connection configuration**:

Edit `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=8080

# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/player_session_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=player_user
spring.datasource.password=player_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false

# Logging configuration
logging.level.root=INFO
logging.level.org.lyc122.dev.playersessionserver=INFO
logging.file.name=logs/application.log
```

**Production environment configuration**:

For production environment, create `src/main/resources/application-prod.properties`:

```properties
# Use production profile
spring.profiles.active=prod

# Database connection pool configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# JVM parameters
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
```

#### 3. Build Project

**Clean and build**:
```bash
# Clean previous builds
./mvnw clean

# Package project (skip tests)
./mvnw package -DskipTests
```

**Verify build result**:
```bash
# Check if jar file exists
ls -lh target/PlayerSessionServer-0.0.1-SNAPSHOT.jar

# View jar file contents (optional)
jar tf target/PlayerSessionServer-0.0.1-SNAPSHOT.jar | head -20
```

#### 4. Start Application

**Foreground run (development)**:
```bash
# Run with default configuration
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar

# Run with specific profile
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Run with custom JVM parameters
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

**Background run (production)**:

**Linux/Mac**:
```bash
# Run with nohup in background
nohup java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  > logs/application.log 2>&1 &

# Or use screen/tmux
screen -S player-session
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
# Press Ctrl+A+D to detach session

# View process
ps aux | grep PlayerSessionServer

# Stop application
kill $(ps aux | grep PlayerSessionServer | grep -v grep | awk '{print $2}')
```

**Windows**:
```powershell
# Run with Start-Process in background
Start-Process -FilePath "java" -ArgumentList "-jar","target/PlayerSessionServer-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod" -WindowStyle Hidden

# Or use background task
$process = Start-Process -FilePath "java" -ArgumentList "-jar","target/PlayerSessionServer-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod" -PassThru
$process | Stop-Process -Force

# Create Windows service (optional)
# Use NSSM or winsw tools to register application as Windows service
```

#### 5. Create Startup Script

**Linux/Mac startup script** (`start.sh`):
```bash
#!/bin/bash

APP_NAME="PlayerSessionServer"
JAR_FILE="target/PlayerSessionServer-0.0.1-SNAPSHOT.jar"
PID_FILE="$APP_NAME.pid"
LOG_FILE="logs/application.log"

# Start function
start_app() {
    echo "Starting $APP_NAME..."
    
    # Check if already running
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null; then
            echo "$APP_NAME is already running (PID: $PID)"
            exit 1
        else
            rm -f $PID_FILE
        fi
    fi
    
    # Create log directory
    mkdir -p logs
    
    # Start application
    nohup java -jar $JAR_FILE \
        --spring.profiles.active=prod \
        > $LOG_FILE 2>&1 &
    
    echo $! > $PID_FILE
    echo "$APP_NAME started (PID: $(cat $PID_FILE))"
}

# Stop function
stop_app() {
    if [ ! -f "$PID_FILE" ]; then
        echo "$APP_NAME is not running"
        exit 1
    fi
    
    PID=$(cat $PID_FILE)
    kill $PID
    rm -f $PID_FILE
    echo "$APP_NAME stopped"
}

# Restart function
restart_app() {
    stop_app
    sleep 2
    start_app
}

# Status check function
status_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null; then
            echo "$APP_NAME is running (PID: $PID)"
            echo "Memory usage: $(ps -p $PID -o rss= | awk '{print $1/1024 " MB"}') MB"
            echo "CPU usage: $(ps -p $PID -o %cpu=)%"
        else
            echo "$APP_NAME is stopped"
            rm -f $PID_FILE
        fi
    else
        echo "$APP_NAME is not running"
    fi
}

# Main program
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
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
```

**Using startup script**:
```bash
# Grant execute permission
chmod +x start.sh

# Start application
./start.sh start

# Check status
./start.sh status

# Stop application
./start.sh stop

# Restart application
./start.sh restart
```

#### 6. Log Management

**View logs**:
```bash
# View logs in real-time
tail -f logs/application.log

# View last 100 lines
tail -n 100 logs/application.log

# Search error logs
grep -i "error" logs/application.log

# Search specific time period
grep "2026-03-31" logs/application.log
```

#### 7. System Service Configuration

**Create Systemd service** (Linux):

Create `/etc/systemd/system/player-session-server.service`:

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

**Manage service**:
```bash
# Reload systemd configuration
sudo systemctl daemon-reload

# Enable service
sudo systemctl enable player-session-server

# Start service
sudo systemctl start player-session-server

# Check service status
sudo systemctl status player-session-server

# View service logs
sudo journalctl -u player-session-server -f

# Stop service
sudo systemctl stop player-session-server

# Restart service
sudo systemctl restart player-session-server
```

#### 8. Performance Optimization

**JVM parameter optimization**:
```bash
# Basic configuration
-Xms512m              # Initial heap memory
-Xmx1024m             # Maximum heap memory
-XX:+UseG1GC          # Use G1 garbage collector
-XX:MaxGCPauseMillis=200 # Maximum GC pause time

# Performance tuning
-XX:+UseStringDeduplication       # String deduplication
-XX:+OptimizeStringConcat       # String concatenation optimization
-XX:+UseCompressedOops         # Compressed ordinary object pointers
-XX:+UseCompressedClassPointers # Compressed class pointers
-XX:InitiatingHeapOccupancyPercent=40 # Initial heap occupancy percentage
```

#### 9. Monitoring and Maintenance

**Health check**:
```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check application info
curl http://localhost:8080/actuator/info

# Check Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

**Database backup**:
```bash
# Backup database
mysqldump -u player_user -pplayer_password \
  --single-transaction \
  --routines --triggers \
  player_session_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### 10. Troubleshooting

**Common issues and solutions**:

**Q: Application fails to start**
```bash
# Check Java version
java -version

# Check port usage
netstat -ano | findstr :8080

# Check MySQL connection
mysql -u player_user -pplayer_password -e "SELECT 1;" player_session_db

# View application logs
tail -100 logs/application.log
```

**Q: Database connection fails**
```bash
# Test database connection
mysql -h localhost -P 3306 -u player_user -p

# Check MySQL service
systemctl status mysql
```

**Q: Insufficient memory**
```bash
# View JVM memory usage
jmap -heap <pid>

# Increase heap memory
java -Xms1024m -Xmx2048m -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar
```

**Q: Port conflict**
```bash
# Modify port in application.properties
server.port=8081

# Or specify port at startup
java -jar target/PlayerSessionServer-0.0.1-SNAPSHOT.jar --server.port=8081
```

### Deployment Checklist

- [ ] Java 21+ installed
- [ ] MySQL 8.0+ installed and running
- [ ] Database and user created
- [ ] Table structure updated
- [ ] application.properties configured
- [ ] Project built successfully
- [ ] Application can start normally
- [ ] API endpoints accessible
- [ ] Database connection normal
- [ ] Log output normal
- [ ] Health check endpoint normal

### Comparison with Docker Deployment

| Feature | Docker Deployment | JAR Deployment |
|---------|-------------------|----------------|
| **Environment Setup** | Automated | Manual configuration |
| **Dependency Management** | Included in container | Manual installation required |
| **Deployment Speed** | Fast | Slower |
| **Resource Isolation** | Container-level | Process-level |
| **Scalability** | Easy to scale | Manual configuration required |
| **Monitoring** | Additional configuration needed | Direct monitoring possible |
| **Flexibility** | Lower | Very high |
| **Learning Curve** | Requires Docker knowledge | Traditional Java deployment |

### Recommended Choice

- **Beginners/Quick deployment**: Docker deployment
- **Production environment**: Docker deployment (recommended)
- **Existing Java environment**: JAR deployment
- **Need custom configuration**: JAR deployment
- **Multi-environment management**: Docker deployment

---

## API Documentation

For detailed API documentation, see: [API-DOC-v3.md](API-DOC-v3.md)

### Main API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/players/register` | POST | Register new player |
| `/api/tokens/generate` | POST | Get AccessToken |
| `/api/tokens/validate` | POST | Validate Token |
| `/api/tokens/revoke` | POST | Revoke Token |
| `/api/players/logout` | POST | Player logout |
| `/api/players/{identifier}` | GET | Query player information |
| `/api/tokens/history` | GET | Query Token history |

## Usage Examples

### Complete Player Login Flow

1. **Register Player**
```bash
curl -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "password": "password123"
  }'
```

2. **Get Token**
```bash
curl -X POST http://localhost:8080/api/tokens/generate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

3. **Validate Token**
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

## Token Management Rules

### Token Validity
- **Initial Validity**: 3 minutes (from generation time)
- **Renewal Mechanism**: Each successful validation resets the validity period
- **Timeout Handling**: Automatically expires after 3 minutes of inactivity

### Token Uniqueness
- Each player can only have one valid token at any time
- Generating a new token automatically revokes all old tokens
- Old token revocation is recorded as INVALIDATED event

### Token Revocation Methods
1. **Automatic Revocation**: When generating new token, timeout (3 minutes inactivity)
2. **Manual Revocation**: Via `/tokens/revoke` endpoint, recorded as REVOKED event

### Token History Query

#### Feature Description
Token history query API supports efficient cursor-based pagination, avoiding the performance issues of traditional offset-based pagination.

#### API Endpoint
```
GET /api/tokens/history
```

#### Parameters
- **playerId** (required): Player ID
- **limit** (optional): Number of records to return, default 10, maximum 100
- **cursor** (optional): Query endpoint timestamp (ISO format, e.g., 2026-03-30T10:00:00), for pagination
- **startTime** (optional): Start time (ISO format)
- **endTime** (optional): End time (ISO format)

#### Query Examples

**1. Query latest 10 records (default)**
```bash
curl "http://localhost:8080/api/tokens/history?playerId=1"
```

**2. Query latest 20 records**
```bash
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=20"
```

**3. Cursor-based pagination**
```bash
# First query, get latest 5 records
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=5"

# Use the last record's time from previous batch as cursor to query older records
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=5&cursor=2026-03-30T10:00:00"
```

**4. Query with time range**
```bash
curl "http://localhost:8080/api/tokens/history?playerId=1&limit=10&startTime=2026-03-01T00:00:00&endTime=2026-03-31T23:59:59"
```

#### Response Format
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

#### Event Types
- **GENERATED**: Token generated
- **VALIDATED**: Token validated successfully
- **RENEWED**: Token renewed
- **INVALIDATED**: Token invalidated (when generating new token)
- **REVOKED**: Token revoked (manual call)
- **EXPIRED**: Token expired due to timeout

#### Cursor Pagination Principle
1. No cursor provided: Query latest N records
2. Cursor provided: Query N records before the timestamp
3. The `eventTime` of the last record in each page can be used as the `cursor` for the next page
4. This approach avoids performance issues with traditional offset-based pagination

## Development Guide

### Project Structure

```
src/main/java/org/lyc122/dev/playersessionserver/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # Database entities
├── exception/       # Exception handling
├── repository/      # Data access layer
├── security/        # Security configuration
└── service/         # Business logic layer
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Build without tests
./mvnw clean package -DskipTests
```

### Development Environment Configuration

Current development environment configuration:
- Server port: 8080
- Database: MySQL 8.4.8
- API Key authentication: Temporarily disabled (for testing)

Production environment requires:
- Enable API Key authentication
- Use HTTPS
- Configure database connection pool
- Enable logging and monitoring

## Docker Deployment

### Using Docker Compose (Recommended)

#### Development Environment (with phpMyAdmin)
```bash
# Copy environment variable configuration
cp .dockerenv.example .env

# Start all services
docker-compose --profile dev up -d

# View logs
docker-compose logs -f app

# Access phpMyAdmin
# http://localhost:8081
# Username: root / rootpassword123
```

#### Production Environment
```bash
# Copy environment variable configuration
cp .dockerenv.example .env

# Start core services (without phpMyAdmin)
docker-compose up -d

# View service status
docker-compose ps

# View application logs
docker-compose logs -f app

# View MySQL logs
docker-compose logs -f mysql
```

### Manual Build and Run

```bash
# Build image
docker build -t player-session-server:latest .

# Run container
docker run -d \
  --name player-session-server \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/player_session_db \
  -e SPRING_DATASOURCE_USERNAME=player_user \
  -e SPRING_DATASOURCE_PASSWORD=player_password \
  player-session-server:latest
```

### Docker Configuration Overview

#### Service Architecture
- **MySQL 8.4**: Database service, port 3306
- **PlayerSessionServer**: Application service, port 8080
- **phpMyAdmin**: Database management tool (dev only), port 8081

#### Database Connection Information
- **Host**: mysql (within containers) or localhost (from host)
- **Port**: 3306
- **Database**: player_session_db
- **User**: player_user
- **Password**: player_password

#### Data Persistence
- MySQL data: Docker volume `mysql-data`
- Application logs: Docker volume `app-logs`

#### Health Checks
- MySQL: Check every 10s, timeout 5s
- Application: Check every 30s, timeout 10s

#### Resource Limits
- CPU: Max 2 cores, reserved 0.5 core
- Memory: Max 1.5G, reserved 512M

### Docker Management Commands

```bash
# View all container status
docker-compose ps

# View service logs
docker-compose logs -f [service-name]

# Restart service
docker-compose restart [service-name]

# Stop service
docker-compose stop [service-name]

# Stop and remove containers
docker-compose down

# Stop and remove containers and volumes
docker-compose down -v

# Enter container
docker exec -it player-session-server bash

# View container resource usage
docker stats player-session-server
```

### Database Operations

```bash
# Backup database
docker exec player-session-mysql \
  mysqldump -u root -prootpassword123 \
  player_session_db > backup.sql

# Restore database
docker exec -i player-session-mysql \
  mysql -u root -prootpassword123 \
  player_session_db < backup.sql

# Connect to MySQL
docker exec -it player-session-mysql \
  mysql -u player_user -pplayer_password player_session_db

# View database tables
docker exec -it player-session-mysql \
  mysql -u player_user -pplayer_password \
  -e "SHOW TABLES;" player_session_db
```

### Monitoring and Maintenance

```bash
# Check application health status
curl http://localhost:8080/actuator/health

# View application info
curl http://localhost:8080/actuator/info

# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# View application logs
docker exec -it player-session-server \
  tail -f /app/logs/application.log
```

### Environment Variables Configuration

Create `.env` file (based on `.dockerenv.example`):

```bash
# MySQL Configuration
MYSQL_ROOT_PASSWORD=rootpassword123
MYSQL_DATABASE=player_session_db
MYSQL_USER=player_user
MYSQL_PASSWORD=player_password

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# JVM Configuration
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC

# Timezone Configuration
TZ=Asia/Shanghai
```

### Troubleshooting

**Q: Container startup failed**
```bash
# View detailed logs
docker-compose logs [service-name]

# Check container status
docker-compose ps

# Rebuild image
docker-compose build --no-cache [service-name]
```

**Q: Database connection failed**
```bash
# Check MySQL container status
docker-compose ps mysql

# Test database connection
docker exec -it player-session-mysql \
  mysql -u player_user -pplayer_password -e "SELECT 1;"

# Check network configuration
docker network inspect player-session-player-session-network
```

**Q: Application not accessible**
```bash
# Check port mapping
docker port player-session-server

# Check health check status
docker inspect player-session-server --format='{{json .State.Health}}'

# Check firewall rules
netstat -ano | findstr :8080
```

### More Information

For detailed Docker configuration instructions, see: [DOCKER-UPDATE.md](DOCKER-UPDATE.md)

## Security Features

- ✅ Passwords encrypted with BCrypt
- ✅ Tokens use UUID format, hard to guess
- ✅ Complete permission validation and ownership checks
- ✅ All token operations recorded for audit
- ✅ Manual token revocation support for security

## Monitoring and Logging

- Application logs: Console output
- Database logs: MySQL logs
- Recommended integration: Prometheus + Grafana

## Troubleshooting

### Common Issues

**Q: Database connection failed**
- Check if MySQL service is running
- Confirm database connection information is correct
- Verify user permissions

**Q: Token validation failed**
- Confirm token hasn't expired (3-minute validity)
- Check if token belongs to the player
- Verify request parameter format is correct

**Q: Port already in use**
- Modify `server.port` in `application.properties`
- Check if port is occupied by other applications

## Contributing

Issues and Pull Requests are welcome!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Version History

### v3.0 (2026-03-31)
- Added token revocation feature
- Added REVOKED event type
- Fixed DTO field naming
- Improved error handling and parameter validation
- Migrated database from H2 to MySQL

### v2.0 (2026-03-30)
- Complete player registration and authentication
- Token generation and validation
- Session management
- Multi-server support
- Token history query

### v1.0 (2026-03-29)
- Initial version
- Basic player management features

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Contact

- Project URL: https://github.com/your-username/PlayerSessionServer
- Issue Tracker: https://github.com/your-username/PlayerSessionServer/issues

## Acknowledgments

Thanks to all developers who have contributed to this project!

---

**Note**: This project is currently in development, and APIs may change. It is recommended to conduct thorough testing before using in production environments.