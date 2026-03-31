-- 插入测试服务器API Key
INSERT INTO servers (server_name, api_key, description, created_at) VALUES 
('survival-server', 'survival-server-api-key-12345678901234567890123456789012', '生存模式服务器', NOW()),
('creative-server', 'creative-server-api-key-12345678901234567890123456789012', '创造模式服务器', NOW()),
('skyblock-server', 'skyblock-server-api-key-12345678901234567890123456789012', '空岛服务器', NOW());