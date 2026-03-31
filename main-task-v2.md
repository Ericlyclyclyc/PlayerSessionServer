Spring后端系统开发需求文档：用于Minecraft多服同步登录插件

项目概述
开发一个Spring Boot后端系统，支持Minecraft多服集群的玩家登录认证和状态管理。玩家首次加入任意服务器前，需通过网页获取专属AccessToken，并在游戏内输入验证；系统处理玩家注册、Token管理、状态跟踪和冲突解决。数据持久化使用关系型数据库（如MySQL），避免Redis。

功能需求
1. 玩家管理
   • 注册：接收玩家名、UUID和密码，存储到数据库。密码需加密（如BCrypt）。

   • 查询：根据玩家名或UUID，返回基本信息（玩家名、UUID）、最后上线时间、当前状态（在线/离线）。如在线，返回所在服务器名称；如离线，返回下线时间。

   • 冲突处理：如果已有认证玩家与待加入玩家的用户名或UUID相同，则拒绝新玩家的加入请求。

2. AccessToken管理
   • 生成：为玩家生成唯一、难以暴力破解的AccessToken（建议使用UUID格式），关联玩家名和UUID，并设置创建时间和过期状态。

   • 验证：玩家加入服务器时，插件提供玩家名、UUID、AccessToken和服务器名称。系统验证Token有效（未过期、与玩家信息匹配），并更新玩家状态为在线、记录所在服务器。

   • 超时检测：玩家退出服务器时，插件通知系统；系统记录下线时间，并启动超时计时（3分钟）。如果玩家在3分钟内重新加入任意服务器，允许使用同一Token验证；超时后标记Token为过期，需重新获取。

   • 查询：支持查询Token历史（可限制条数，如最近10条）及当前有效的Token。

3. 服务器集成
   • 插件请求时需包含服务器名称，用于跟踪玩家位置。

   • 系统记录玩家最后所在的服务器名称，并在查询时返回。

非功能需求
• 性能：响应时间快，支持并发玩家请求。

• 安全性：Token需随机生成，防止猜测；密码加密存储；API建议增加认证机制（如插件端使用API密钥）。

• 可扩展性：模块化设计，便于未来扩展功能。

技术栈
• 框架：Spring Boot 2.x/3.x

• 数据持久化：Spring Data JPA + MySQL/PostgreSQL（或其他关系型数据库）

• 安全：Spring Security（可选，用于API保护）

• 构建工具：Maven或Gradle

系统架构
采用Model-Entity-Service-Controller分层架构：
• Entity：定义数据模型，映射数据库表。

• Model：数据传输对象（DTO），用于API请求/响应。

• Service：业务逻辑层，处理注册、Token管理、查询等。

• Controller：REST API端点，接收插件或网页请求。

• Repository：数据访问层，使用JPA接口。

数据模型
1. Player实体
   • id: Long (主键)

   • username: String (唯一，玩家名)

   • uuid: String (唯一，玩家UUID)

   • password: String (加密存储)

   • lastOnlineTime: LocalDateTime

   • offlineTime: LocalDateTime

   • status: Enum (ONLINE, OFFLINE)

   • currentServer: String (记录所在服务器名称，null表示离线)

2. AccessToken实体
   • id: Long (主键)

   • token: String (唯一，UUID格式)

   • player: Player (外键，关联玩家)

   • createdAt: LocalDateTime

   • expired: Boolean (默认false，超时或手动过期)

   • lastUsedTime: LocalDateTime (最后使用时间，用于超时检测)

API设计
• 所有API返回JSON格式，统一错误处理。

• 基础路径：/api

1. 玩家注册
   • POST /players/register

   • 请求体：{"username": "string", "uuid": "string", "password": "string"}

   • 响应：201 Created，返回玩家基本信息。

2. 获取AccessToken
   • POST /tokens/generate

   • 请求体：{"username": "string", "uuid": "string"}

   • 响应：生成Token，返回Token字符串和过期时间。

3. 验证Token（玩家加入）
   • POST /tokens/validate

   • 请求体：{"username": "string", "uuid": "string", "token": "string", "serverName": "string"}

   • 响应：验证成功返回玩家状态，失败返回错误码。

4. 玩家退出
   • POST /players/logout

   • 请求体：{"username": "string", "uuid": "string", "serverName": "string"}

   • 响应：更新玩家状态为离线，记录下线时间。

5. 查询玩家
   • GET /players/{identifier}?type=username/uuid

   • 响应：返回玩家详情，包括最后上线时间、状态、所在服务器等。

6. 查询Token历史
   • GET /tokens/history?playerId={id}&limit=10

   • 响应：返回Token列表，包括创建时间、过期状态。

服务逻辑
• PlayerService：处理注册、查询、更新状态。

• TokenService：生成Token、验证Token、管理超时（通过定时任务或退出时计算时间差标记过期）。

• 超时检测：玩家退出时，记录时间戳；验证时检查当前时间与最后使用时间的差值，超过3分钟则标记Token过期。

• 冲突处理：注册或验证时，检查数据库是否存在相同用户名或UUID的在线玩家，存在则拒绝。

特殊情况处理
• 玩家冲突：在验证Token前，检查待加入玩家的用户名或UUID是否已有在线记录，如有则返回错误。

• Token过期：自动清理过期Token，或标记为无效。

• 服务器名称更新：玩家加入或切换服务器时，更新currentServer字段。

数据存储方案
• 使用关系型数据库（如MySQL），通过JPA实现持久化。

• 表结构对应Entity设计，添加索引优化查询（如username、uuid、token字段）。

• 考虑定期清理过期Token数据，以维护性能。

部署与监控
• 提供健康检查端点。

• 日志记录关键操作，便于调试。
