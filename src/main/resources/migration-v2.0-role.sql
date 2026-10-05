-- ============================================================
-- Sa-Token 角色权限迁移脚本 (v2.0)
-- 添加 role 字段并设置默认角色
-- ============================================================

-- 1. 添加 role 字段到 user 表
ALTER TABLE user
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'user'
    COMMENT '角色: user(普通用户), merchant(商家), admin(管理员)';

-- 2. 更新 mock 数据中的商家角色（有 goods 上架的用户设为 merchant）
UPDATE user u
SET u.role = 'merchant'
WHERE u.id IN (
    SELECT DISTINCT g.userId
    FROM goods g
    WHERE g.delete = 0
);

-- 3. 设置管理员账户
-- 将用户 ID=1 设为管理员（张三）
UPDATE user SET role = 'admin' WHERE id = 1;

-- 4. 创建索引
ALTER TABLE user ADD INDEX idx_role (role);