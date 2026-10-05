-- ============================================================
-- VIP配置表初始化脚本 (v1.0)
-- 建表 + 默认数据
-- ============================================================

CREATE TABLE IF NOT EXISTS vipconfig (
    level                     INT(10)       NOT NULL COMMENT '等级',
    maxAddressQuantity        INT(10)       DEFAULT NULL COMMENT '最大地址数量',
    monthlyUpdateGoods        INT(10)       NOT NULL DEFAULT 100 COMMENT '每月商品更新上限',
    monthlyUpdateAvatar       INT(10)       NOT NULL DEFAULT 7   COMMENT '每月头像更新上限',
    monthlyUpdateBackground   INT(10)       NOT NULL DEFAULT 2   COMMENT '每月背景更新上限',
    maxCartQuantity           INT(10)       NOT NULL DEFAULT 5   COMMENT '购物车最大商品数',
    maxGoodsQuantity          INT(10)       NOT NULL DEFAULT 5   COMMENT '最大上架商品数',
    maxAiQuantity             INT(10)       DEFAULT NULL COMMENT '最大AI数量',
    vipDuration               INT(10)       NOT NULL DEFAULT 30  COMMENT 'VIP有效期(天)',
    price                     DECIMAL(10,2) NOT NULL COMMENT 'VIP价格',
    levelName                 VARCHAR(50)   DEFAULT NULL COMMENT '等级名称',
    duration                  INT(10)       DEFAULT NULL COMMENT '持续天数',
    createTime                DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP等级配置表';

INSERT IGNORE INTO vipconfig (level, maxAddressQuantity, monthlyUpdateGoods, monthlyUpdateAvatar, monthlyUpdateBackground,
                               maxCartQuantity, maxGoodsQuantity, maxAiQuantity, vipDuration, price, levelName, duration)
VALUES (0, NULL, 100, 7, 2, 5, 5, NULL, 0, 0.00, '普通用户', 0);