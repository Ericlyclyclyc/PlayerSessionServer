# PlayerSessionServer API 文档 (v3)

## 概述

PlayerSessionServer是一个用于Minecraft多服集群的玩家登录认证和状态管理系统。本文档描述了v3版本的所有API端点。

**基础URL**: `http://localhost:8080/api`

**认证方式**: 当前测试环境已临时禁用API Key认证

**响应格式**: 所有API返回JSON格式

---

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1774890000000
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null,
  "timestamp": 1774890000000
}
```

**常见HTTP状态码**:
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未授权/Token无效
- `403` - 禁止访问
- `404` - 资源不存在
- `409` - 资源冲突
- `500` - 服务器内部错误

---

## API端点

### 1. 玩家注册

注册新玩家账户。

**端点**: `POST /players/register`

**请求体**:
```json
{
  "username": "TestPlayer",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "password": "password123"
}
```

**字段说明**:
- `username`: 玩家名（必填，最长16字符，唯一）
- `uuid`: 玩家UUID（必填，36字符，唯一）
- `password`: 密码（必填，会使用BCrypt加密存储）

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null,
  "timestamp": 1774890000000
}
```

**错误情况**:
- `409` - 用户名或UUID已存在
- `400` - 参数验证失败（如空用户名）

---

### 2. 获取AccessToken

为玩家生成新的AccessToken。**注意：生成新token会自动失效该玩家的所有旧token**。

**端点**: `POST /tokens/generate`

**请求体**:
```json
{
  "username": "TestPlayer",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Token生成成功",
  "data": {
    "accessToken": "f58e3f93-a170-40e0-b097-3c64552dc860",
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "expiresAt": 1774891800000
  },
  "timestamp": 1774890000000
}
```

**字段说明**:
- `accessToken`: 生成的token字符串（UUID格式）
- `expiresAt`: 过期时间戳（生成后3分钟）

---

### 3. 验证Token

玩家加入服务器时验证Token有效性。如果验证成功，玩家状态会更新为在线。

**端点**: `POST /tokens/validate`

**请求体**:
```json
{
  "username": "TestPlayer",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "token": "f58e3f93-a170-40e0-b097-3c64552dc860",
  "serverName": "SurvivalServer"
}
```

**字段说明**:
- `token`: AccessToken字符串（注意字段名为`token`，不是`accessToken`）
- `serverName`: 玩家加入的服务器名称

**响应示例**:
```json
{
  "code": 200,
  "message": "Token验证成功",
  "data": null,
  "timestamp": 1774890000000
}
```

**错误情况**:
- `401` - Token不存在、已过期或无效
- `403` - Token不属于该玩家
- `400` - 参数验证失败

**验证规则**:
- Token必须在3分钟内使用
- Token不能已过期（手动失效或超时）
- Token必须属于指定的玩家
- 每次验证会更新token的"最后使用时间"，延长有效期

---

### 4. 玩家退出

玩家退出服务器，更新玩家状态为离线。

**端点**: `POST /players/logout`

**请求体**:
```json
{
  "username": "TestPlayer",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "serverName": "SurvivalServer"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1774890000000
}
```

**行为**:
- 更新玩家状态为OFFLINE
- 记录下线时间
- 清空currentServer字段
- Token在3分钟内可以重新使用

---

### 5. 使Token失效

手动使指定的Token失效，用于安全措施如异常活动检测、强制下线等。

**端点**: `POST /tokens/revoke`

**请求体**:
```json
{
  "username": "TestPlayer",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "token": "f58e3f93-a170-40e0-b097-3c64552dc860"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Token已失效",
  "data": null,
  "timestamp": 1774890000000
}
```

**行为**:
- 标记token为已过期
- 如果玩家在线，自动清理会话（状态设为OFFLINE）
- 记录REVOKED事件到token历史
- 幂等性：重复revoke同一token不会产生错误

**错误情况**:
- `401` - Token不存在
- `403` - Token不属于该玩家

---

### 6. 查询玩家信息

根据用户名或UUID查询玩家信息。

**端点**: `GET /players/{identifier}`

**路径参数**:
- `identifier`: 玩家名或UUID

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "TestPlayer",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "status": "ONLINE",
    "lastOnlineTime": "2026-03-31T10:00:00",
    "offlineTime": null,
    "currentServer": "SurvivalServer"
  },
  "timestamp": 1774890000000
}
```

**字段说明**:
- `id`: 玩家ID
- `status`: 玩家状态（ONLINE/OFFLINE）
- `lastOnlineTime`: 最后上线时间
- `offlineTime`: 下线时间（null表示未下线）
- `currentServer`: 当前所在服务器（null表示离线）

**查询优先级**:
1. 先尝试按用户名查询
2. 如果未找到，尝试按UUID查询
3. 都未找到返回404

---

### 7. 查询Token历史

查询指定玩家的Token历史记录。

**端点**: `GET /tokens/history`

**查询参数**:
- `playerId`: 玩家ID（必填）
- `limit`: 返回条数限制（可选，默认10）
- `startTime`: 开始时间（可选，格式：ISO 8601）
- `endTime`: 结束时间（可选，格式：ISO 8601）

**请求示例**:
```
GET /api/tokens/history?playerId=1&limit=10
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 5,
      "token": "f58e3f93-a170-40e0-b097-3c64552dc860",
      "eventType": "VALIDATED",
      "serverName": "SurvivalServer",
      "eventTime": "2026-03-31T10:00:00"
    },
    {
      "id": 4,
      "token": "f58e3f93-a170-40e0-b097-3c64552dc860",
      "eventType": "GENERATED",
      "serverName": null,
      "eventTime": "2026-03-31T09:59:00"
    }
  ],
  "timestamp": 1774890000000
}
```

**事件类型**:
- `GENERATED` - Token生成
- `VALIDATED` - Token验证成功
- `RENEWED` - Token续期（更新使用时间）
- `EXPIRED` - Token超时过期
- `INVALIDATED` - Token自动失效（生成新token时）
- `REVOKED` - Token手动失效

**错误情况**:
- `400` - 缺少playerId参数

---

## 数据模型

### Player（玩家）
```json
{
  "id": 1,
  "username": "TestPlayer",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ONLINE",
  "lastOnlineTime": "2026-03-31T10:00:00",
  "offlineTime": null,
  "currentServer": "SurvivalServer"
}
```

### AccessToken（访问令牌）
```json
{
  "id": 1,
  "token": "f58e3f93-a170-40e0-b097-3c64552dc860",
  "createdAt": "2026-03-31T09:59:00",
  "expired": false,
  "lastUsedTime": "2026-03-31T10:00:00"
}
```

### TokenEvent（令牌事件）
```json
{
  "id": 1,
  "token": "f58e3f93-a170-40e0-b097-3c64552dc860",
  "eventType": "VALIDATED",
  "serverName": "SurvivalServer",
  "eventTime": "2026-03-31T10:00:00"
}
```

---

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
1. **自动失效**:
   - 生成新token时
   - 超时（3分钟未使用）
2. **手动失效**:
   - 调用`/tokens/revoke`接口
   - 记录REVOKED事件

---

## 安全特性

### 密码安全
- 使用BCrypt加密存储
- 永不返回明文密码

### Token安全
- 使用UUID格式，难以猜测
- 验证token所有权
- 记录所有token操作事件

### 访问控制
- 当前测试环境：所有端点开放
- 生产环境：需要API Key认证

---

## 使用示例

### 完整的玩家登录流程

1. **注册玩家**
```bash
curl -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{"username":"TestPlayer","uuid":"550e8400-e29b-41d4-a716-446655440000","password":"password123"}'
```

2. **获取Token**
```bash
curl -X POST http://localhost:8080/api/tokens/generate \
  -H "Content-Type: application/json" \
  -d '{"username":"TestPlayer","uuid":"550e8400-e29b-41d4-a716-446655440000"}'
```

3. **验证Token（玩家加入服务器）**
```bash
curl -X POST http://localhost:8080/api/tokens/validate \
  -H "Content-Type: application/json" \
  -d '{"username":"TestPlayer","uuid":"550e8400-e29b-41d4-a716-446655440000","token":"your-token-here","serverName":"SurvivalServer"}'
```

4. **查询玩家状态**
```bash
curl http://localhost:8080/api/players/TestPlayer
```

5. **玩家退出**
```bash
curl -X POST http://localhost:8080/api/players/logout \
  -H "Content-Type: application/json" \
  -d '{"username":"TestPlayer","uuid":"550e8400-e29b-41d4-a716-446655440000","serverName":"SurvivalServer"}'
```

### 手动使Token失效

```bash
curl -X POST http://localhost:8080/api/tokens/revoke \
  -H "Content-Type: application/json" \
  -d '{"username":"TestPlayer","uuid":"550e8400-e29b-41d4-a716-446655440000","token":"your-token-here"}'
```

---

## 错误代码参考

| 代码 | 含义 | 常见原因 |
|------|------|----------|
| 200 | 成功 | 操作成功完成 |
| 400 | 请求错误 | 参数缺失、格式错误、验证失败 |
| 401 | 未授权 | Token无效、Token过期、Token不存在 |
| 403 | 禁止访问 | Token不属于该玩家、权限不足 |
| 404 | 未找到 | 玩家不存在、资源不存在 |
| 409 | 冲突 | 用户名已存在、UUID已存在 |
| 500 | 服务器错误 | 数据库错误、系统异常 |

---

## 注意事项

### 开发环境
- 端口：8080
- 数据库：MySQL 8.4.8
- API Key认证：临时禁用

### 生产环境部署
- 启用API Key认证
- 使用HTTPS
- 配置数据库连接池
- 启用日志记录和监控
- 定期清理过期Token数据

### 性能建议
- Token验证响应时间 < 100ms
- 支持多玩家并发请求
- 使用数据库索引优化查询

---

## 更新日志

### v3.0
- 添加Token失效功能（POST /api/tokens/revoke）
- 添加REVOKED事件类型
- 修复DTO字段命名（使用`token`而非`accessToken`）
- 改进错误处理和参数验证
- 数据库从H2迁移到MySQL

### v2.0
- 完整的玩家注册和认证功能
- Token生成和验证
- 会话管理
- 多服务器支持
- Token历史查询

---

## 技术支持

如有问题，请查看：
- 开发日志：`dev-log/` 目录
- 测试报告：`dev-log/v3-test.1.md`
- 开发记录：`dev-log/v3-2.md`