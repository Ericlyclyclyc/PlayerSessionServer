-- 初始化数据库脚本
-- 用于Docker Compose启动时自动执行

-- 修改token_events表的event_type枚举，添加REVOKED类型
ALTER TABLE token_events 
MODIFY COLUMN event_type 
ENUM('EXPIRED','GENERATED','INVALIDATED','RENEWED','VALIDATED','REVOKED') 
NOT NULL;

-- 创建索引以优化查询性能
CREATE INDEX IF NOT EXISTS idx_players_username ON players(username);
CREATE INDEX IF NOT EXISTS idx_players_uuid ON players(uuid);
CREATE INDEX IF NOT EXISTS idx_players_status ON players(status);
CREATE INDEX IF NOT EXISTS idx_access_tokens_token ON access_tokens(token);
CREATE INDEX IF NOT EXISTS idx_access_tokens_player_id ON access_tokens(player_id);
CREATE INDEX IF NOT EXISTS idx_access_tokens_expired ON access_tokens(expired);
CREATE INDEX IF NOT EXISTS idx_access_tokens_created_at ON access_tokens(createdAt);
CREATE INDEX IF NOT EXISTS idx_token_events_token_id ON token_events(token_id);
CREATE INDEX IF NOT EXISTS idx_token_events_event_time ON token_events(eventTime);
CREATE INDEX IF NOT EXISTS idx_token_events_event_type ON token_events(eventType);
CREATE INDEX IF NOT EXISTS idx_player_sessions_player_id ON player_sessions(player_id);
CREATE INDEX IF NOT EXISTS idx_player_sessions_start_time ON player_sessions(startTime);
CREATE INDEX IF NOT EXISTS idx_servers_server_name ON servers(server_name);

-- 显示表结构信息
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_KEY
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'player_session_db'
ORDER BY 
    TABLE_NAME, ORDINAL_POSITION;

-- 显示创建的索引
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = 'player_session_db'
ORDER BY 
    TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;