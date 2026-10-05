-- ============================================================
-- Mock Data Insert Script
-- Salt: a1b2c3d4e5f6g7h8
-- SHA256: fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e
-- ============================================================

-- 1. User (12 rows)
INSERT IGNORE INTO user (id, defaultAddressId, userName, account, password, salt, role, phone, `describe`, email, balance, avatarPath, level, vipCreateTime, vipDuration, createTime, `delete`, uploadTime, uploadBackground, uploadGoods, updateAvatar) VALUES
(1, 1, '张三', 'zhangsan', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'admin', '13800138001', '数码爱好者', 'zhangsan@qq.com', 1000.00, '/image/avatar/1/avatar.png', 1, '2025-12-01 10:00:00', 30, '2025-06-01 08:00:00', 0, NULL, 0, 0, 0),
(2, 2, '李四', 'lisi', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138002', '品质生活追求者', 'lisi@qq.com', 500.00, '/image/avatar/2/avatar.png', 0, NULL, NULL, '2025-06-15 09:00:00', 0, NULL, 0, 0, 0),
(3, 3, '王五', 'wangwu', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138003', '资深买手', 'wangwu@qq.com', 2000.00, '/image/avatar/3/avatar.png', 2, '2026-01-10 14:00:00', 365, '2025-07-01 10:00:00', 0, NULL, 0, 0, 0),
(4, 4, '赵六', 'zhaoliu', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138004', '旅行达人', 'zhaoliu@qq.com', 800.00, '/image/avatar/4/avatar.png', 0, NULL, NULL, '2025-08-01 10:00:00', 0, NULL, 0, 0, 0),
(5, 5, '孙七', 'sunqi', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138005', '美食家', 'sunqi@qq.com', 1500.00, '/image/avatar/5/avatar.png', 1, '2025-11-15 09:00:00', 30, '2025-09-01 10:00:00', 0, NULL, 0, 0, 0),
(6, 6, '周八', 'zhouba', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138006', '极客玩家', 'zhouba@qq.com', 3000.00, '/image/avatar/6/avatar.png', 0, NULL, NULL, '2025-10-01 10:00:00', 0, NULL, 0, 0, 0),
(7, 7, '吴九', 'wujiu', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'user', '13800138007', '运动爱好者', 'wujiu@qq.com', 600.00, '/image/avatar/7/avatar.png', 0, NULL, NULL, '2025-11-01 10:00:00', 0, NULL, 0, 0, 0),
(8, 8, '郑十', 'zhengshi', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138008', '文艺青年', 'zhengshi@qq.com', 1200.00, '/image/avatar/8/avatar.png', 1, '2026-02-20 10:00:00', 30, '2025-12-01 10:00:00', 0, NULL, 0, 0, 0),
(9, 9, '钱十一', 'qianshiyi', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138009', '音乐发烧友', 'qianshiyi@qq.com', 2500.00, '/image/avatar/9/avatar.png', 0, NULL, NULL, '2026-01-01 10:00:00', 0, NULL, 0, 0, 0),
(10, 10, '陈十二', 'chenshier', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'merchant', '13800138010', '摄影达人', 'chenshier@qq.com', 5000.00, '/image/avatar/10/avatar.png', 2, '2026-03-05 10:00:00', 365, '2026-02-01 10:00:00', 0, NULL, 0, 0, 0),
(11, 11, '冯十三', 'fengshisan', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'user', '13800138011', '健身达人', 'fengshisan@qq.com', 3500.00, '/image/avatar/11/avatar.png', 1, '2026-03-10 10:00:00', 30, '2026-03-01 08:00:00', 0, NULL, 0, 0, 0),
(12, 12, '褚十四', 'chushisi', 'fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e', 'a1b2c3d4e5f6g7h8', 'user', '13800138012', '读书爱好者', 'chushisi@qq.com', 1800.00, '/image/avatar/12/avatar.png', 0, NULL, NULL, '2026-04-01 09:00:00', 0, NULL, 0, 0, 0);
-- 2. Address (12 rows)
INSERT IGNORE INTO address (id, userId, addressId, country, province, city, county, detail, createTime, `delete`) VALUES
(1, 1, 1, '中国', '北京市', '北京市', '朝阳区', '建国路88号', '2025-06-01 08:00:00', 0),
(2, 2, 1, '中国', '浙江省', '杭州市', '西湖区', '文三路200号', '2025-06-15 09:00:00', 0),
(3, 3, 1, '中国', '陕西省', '西安市', '雁塔区', '长安路600号', '2025-07-01 10:00:00', 0),
(4, 4, 1, '中国', '上海市', '上海市', '浦东新区', '陆家嘴金融中心', '2025-08-01 10:00:00', 0),
(5, 5, 1, '中国', '广东省', '广州市', '天河区', '天河路100号', '2025-09-01 10:00:00', 0),
(6, 6, 1, '中国', '四川省', '成都市', '武侯区', '天府大道300号', '2025-10-01 10:00:00', 0),
(7, 7, 1, '中国', '湖北省', '武汉市', '洪山区', '珞喻路400号', '2025-11-01 10:00:00', 0),
(8, 8, 1, '中国', '江苏省', '南京市', '鼓楼区', '中山路500号', '2025-12-01 10:00:00', 0),
(9, 9, 1, '中国', '重庆市', '重庆市', '渝中区', '解放碑步行街', '2026-01-01 10:00:00', 0),
(10, 10, 1, '中国', '福建省', '厦门市', '思明区', '环岛路800号', '2026-02-01 10:00:00', 0),
(11, 11, 1, '中国', '天津市', '天津市', '和平区', '南京路100号', '2026-03-01 08:00:00', 0),
(12, 12, 1, '中国', '湖南省', '长沙市', '岳麓区', '麓山南路200号', '2026-04-01 09:00:00', 0);

-- 3. Goods (12 rows)
INSERT IGNORE INTO goods (id, userId, goodsId, addressId, preId, goodsName, `describe`, goodsPrice, goodsStock, imagePath, launch, createTime, `delete`) VALUES
(1, 1, 1, 1, NULL, 'iPhone 15 Pro', '苹果最新旗舰手机', 7999.00, 50, '/image/goods/1/product_1.png', 1, '2025-08-01 10:00:00', 0),
(2, 2, 1, 2, NULL, '华为 Mate 60 Pro', '卫星通话旗舰手机', 6999.00, 40, '/image/goods/2/product_2.png', 1, '2025-08-15 11:00:00', 0),
(3, 3, 1, 3, NULL, '小米 14 Ultra', '徕卡光学旗舰手机', 5999.00, 60, '/image/goods/3/product_3.png', 1, '2025-09-01 12:00:00', 0),
(4, 1, 2, 1, NULL, 'MacBook Pro 16', 'M3 Pro芯片专业笔记本', 14999.00, 20, '/image/goods/1/product_4.png', 1, '2025-09-15 13:00:00', 0),
(5, 2, 2, 2, NULL, 'AirPods Pro 2', '主动降噪无线耳机', 1899.00, 100, '/image/goods/2/product_5.png', 1, '2025-10-01 14:00:00', 0),
(6, 4, 1, 4, NULL, '索尼 WH-1000XM5', '无线降噪耳机，30小时续航', 2499.00, 60, '/image/goods/4/product_6.png', 1, '2025-10-15 10:00:00', 0),
(7, 5, 1, 5, NULL, 'iPad Air M2', '11英寸，128GB', 4799.00, 35, '/image/goods/5/product_7.png', 1, '2025-11-01 11:00:00', 0),
(8, 6, 1, 6, NULL, 'Apple Watch Ultra 2', '钛金属表壳，49mm', 6499.00, 25, '/image/goods/6/product_8.png', 1, '2025-11-15 12:00:00', 0),
(9, 7, 1, 7, NULL, '佳能 EOS R6 II', '全画幅微单，2420万像素', 15999.00, 15, '/image/goods/7/product_9.png', 1, '2025-12-01 13:00:00', 0),
(10, 8, 1, 8, NULL, '戴森 V15 Detect', '无绳吸尘器，激光探测', 4999.00, 45, '/image/goods/8/product_10.png', 1, '2025-12-15 14:00:00', 0),
(11, 9, 1, 9, NULL, '索尼 PS5 Pro', '次世代游戏主机，2TB', 3999.00, 25, '/image/goods/9/product_11.png', 1, '2026-01-15 10:00:00', 0),
(12, 10, 1, 10, NULL, '大疆 Mini 4 Pro', '轻量航拍无人机，4K', 5788.00, 20, '/image/goods/10/product_12.png', 1, '2026-02-15 11:00:00', 0);

-- 4. Cart (12 rows)
INSERT IGNORE INTO cart (id, userId, goodId, quantity, createTime) VALUES
(1, 1, 2, 1, '2026-01-01 10:00:00'),
(2, 2, 3, 2, '2026-01-02 11:00:00'),
(3, 3, 1, 1, '2026-01-03 12:00:00'),
(4, 4, 5, 1, '2026-01-04 13:00:00'),
(5, 5, 7, 1, '2026-01-05 14:00:00'),
(6, 6, 9, 1, '2026-01-06 15:00:00'),
(7, 7, 10, 2, '2026-01-07 16:00:00'),
(8, 8, 4, 1, '2026-01-08 17:00:00'),
(9, 9, 6, 1, '2026-01-09 18:00:00'),
(10, 10, 8, 1, '2026-01-10 19:00:00'),
(11, 11, 3, 1, '2026-04-15 10:00:00'),
(12, 12, 1, 2, '2026-05-01 11:00:00');

-- 5. Trade (12 rows)
INSERT IGNORE INTO trade (id, orderId, userId, goodId, quantity, originAddress, targetAddress, currentAddress, createTime, payTime, cancelTime, finishTime, `delete`) VALUES
(1, 'ORD001', 1, 2, 1, '杭州市西湖区', '北京市朝阳区', '北京市朝阳区', '2025-12-01 10:00:00', '2025-12-01 10:05:00', NULL, '2025-12-03 15:00:00', 0),
(2, 'ORD002', 2, 3, 2, '西安市雁塔区', '杭州市西湖区', '成都市武侯区', '2025-12-10 11:00:00', '2025-12-10 11:10:00', NULL, NULL, 0),
(3, 'ORD003', 3, 1, 1, '北京市朝阳区', '西安市雁塔区', '西安市雁塔区', '2025-12-20 12:00:00', NULL, '2025-12-20 13:00:00', NULL, 0),
(4, 'ORD004', 1, 5, 1, '杭州市西湖区', '北京市朝阳区', '北京市朝阳区', '2026-01-01 09:00:00', '2026-01-01 09:05:00', NULL, NULL, 0),
(5, 'ORD005', 4, 6, 1, '上海市浦东新区', '上海市浦东新区', '上海市浦东新区', '2026-01-15 10:00:00', '2026-01-15 10:10:00', NULL, '2026-01-18 14:00:00', 0),
(6, 'ORD006', 5, 4, 1, '广州市天河区', '广州市天河区', '广州市天河区', '2026-02-01 11:00:00', '2026-02-01 11:05:00', NULL, NULL, 0),
(7, 'ORD007', 6, 8, 1, '成都市武侯区', '武汉市洪山区', '武汉市洪山区', '2026-02-15 12:00:00', NULL, NULL, NULL, 0),
(8, 'ORD008', 7, 10, 1, '武汉市洪山区', '武汉市洪山区', '武汉市洪山区', '2026-03-01 13:00:00', '2026-03-01 13:15:00', NULL, '2026-03-05 16:00:00', 0),
(9, 'ORD009', 8, 7, 2, '南京市鼓楼区', '南京市鼓楼区', '南京市鼓楼区', '2026-03-15 14:00:00', '2026-03-15 14:10:00', NULL, NULL, 0),
(10, 'ORD010', 9, 9, 1, '重庆市渝中区', '重庆市渝中区', '重庆市渝中区', '2026-04-01 15:00:00', NULL, '2026-04-02 10:00:00', NULL, 0),
(11, 'ORD011', 11, 2, 1, '天津市和平区', '天津市和平区', '天津市和平区', '2026-04-10 10:00:00', '2026-04-10 10:05:00', NULL, '2026-04-12 15:00:00', 0),
(12, 'ORD012', 12, 5, 1, '长沙市岳麓区', '长沙市岳麓区', '长沙市岳麓区', '2026-05-01 09:00:00', '2026-05-01 09:10:00', NULL, NULL, 0);

-- 6. AI (12 rows)
INSERT IGNORE INTO ai (id, userId, goodsId, category, kind, name, price, simpleDescription, features, createTime, `delete`) VALUES
(1, 1, 1, '电子产品', '智能手机', 'iPhone 15 Pro', '7999', '苹果最新旗舰', '4800万主摄|钛金属|USB-C', '2025-08-01 10:00:00', 0),
(2, 2, 2, '电子产品', '智能手机', '华为 Mate 60 Pro', '6999', '华为旗舰', '卫星通话|昆仑玻璃|XMAGE', '2025-08-15 11:00:00', 0),
(3, 3, 3, '电子产品', '智能手机', '小米 14 Ultra', '5999', '小米旗舰', '徕卡光学|骁龙8Gen3|1TB', '2025-09-01 12:00:00', 0),
(4, 1, 4, '电子产品', '笔记本电脑', 'MacBook Pro 16', '14999', '苹果专业笔记本', 'M3 Pro芯片|18GB|512GB', '2025-09-15 13:00:00', 0),
(5, 2, 5, '电子产品', '耳机', 'AirPods Pro 2', '1899', '苹果降噪耳机', '主动降噪|USB-C|空间音频', '2025-10-01 14:00:00', 0),
(6, 4, 6, '电子产品', '耳机', '索尼 WH-1000XM5', '2499', '索尼旗舰降噪', '30小时续航|Hi-Res|多点连接', '2025-10-15 10:00:00', 0),
(7, 5, 7, '电子产品', '平板电脑', 'iPad Air M2', '4799', '轻薄全能平板', 'M2芯片|11英寸|128GB', '2025-11-01 11:00:00', 0),
(8, 6, 8, '电子产品', '智能手表', 'Apple Watch Ultra 2', '6499', '户外旗舰手表', '钛金属|49mm|深度计', '2025-11-15 12:00:00', 0),
(9, 7, 9, '电子产品', '相机', '佳能 EOS R6 II', '15999', '全画幅微单', '2420万像素|4K60P|防抖', '2025-12-01 13:00:00', 0),
(10, 8, 10, '家用电器', '吸尘器', '戴森 V15 Detect', '4999', '智能无绳吸尘器', '激光探测|LCD屏|60分钟', '2025-12-15 14:00:00', 0),
(11, 9, 11, '电子产品', '游戏主机', '索尼 PS5 Pro', '3999', '次世代游戏主机', '2TB|8K输出|光线追踪', '2026-01-15 10:00:00', 0),
(12, 10, 12, '电子产品', '无人机', '大疆 Mini 4 Pro', '5788', '轻量航拍无人机', '4K|249g|O4图传', '2026-02-15 11:00:00', 0);

-- 7. Background (12 rows)
INSERT IGNORE INTO background (id, userId, imagePath, sequence, createTime) VALUES
(1, 1, '/image/background/1/bg1.png', 1, '2025-06-01 08:00:00'),
(2, 2, '/image/background/2/bg1.png', 1, '2025-06-15 09:00:00'),
(3, 3, '/image/background/3/bg1.png', 1, '2025-07-01 10:00:00'),
(4, 4, '/image/background/4/bg1.png', 1, '2025-08-01 10:00:00'),
(5, 5, '/image/background/5/bg1.png', 1, '2025-09-01 10:00:00'),
(6, 6, '/image/background/6/bg1.png', 1, '2025-10-01 10:00:00'),
(7, 7, '/image/background/7/bg1.png', 1, '2025-11-01 10:00:00'),
(8, 8, '/image/background/8/bg1.png', 1, '2025-12-01 10:00:00'),
(9, 9, '/image/background/9/bg1.png', 1, '2026-01-01 10:00:00'),
(10, 10, '/image/background/10/bg1.png', 1, '2026-02-01 10:00:00'),
(11, 11, '/image/background/11/bg1.png', 1, '2026-03-01 08:00:00'),
(12, 12, '/image/background/12/bg1.png', 1, '2026-04-01 09:00:00');

-- 8. VIP Config (3 levels, 配置表不变)
INSERT INTO vipconfig (level, maxAddressQuantity, monthlyUpdateGoods, monthlyUpdateAvatar, monthlyUpdateBackground, maxCartQuantity, maxGoodsQuantity, maxAiQuantity, vipDuration, price, createTime, duration, levelName) VALUES
(0, 3, 5, 1, 1, 10, 10, 5, 0, 0.00, '2025-01-01 00:00:00', 0, '普通用户'),
(1, 5, 10, 3, 3, 20, 20, 10, 30, 29.90, '2025-01-01 00:00:00', 30, '月度VIP'),
(2, 10, 30, 10, 10, 50, 50, 30, 365, 99.90, '2025-01-01 00:00:00', 365, '年度VIP')
ON DUPLICATE KEY UPDATE
    maxAddressQuantity = VALUES(maxAddressQuantity),
    monthlyUpdateGoods = VALUES(monthlyUpdateGoods),
    monthlyUpdateAvatar = VALUES(monthlyUpdateAvatar),
    monthlyUpdateBackground = VALUES(monthlyUpdateBackground),
    maxCartQuantity = VALUES(maxCartQuantity),
    maxGoodsQuantity = VALUES(maxGoodsQuantity),
    maxAiQuantity = VALUES(maxAiQuantity),
    vipDuration = VALUES(vipDuration),
    price = VALUES(price),
    duration = VALUES(duration),
    levelName = VALUES(levelName);

-- 9. VIP Trade (12 rows)
INSERT IGNORE INTO viptrade (id, userId, level, money, createTime, `delete`) VALUES
(1, 1, 1, 29.90, '2025-12-01 10:00:00', 0),
(2, 3, 2, 99.90, '2026-01-05 14:00:00', 0),
(3, 3, 1, 29.90, '2026-01-10 15:00:00', 0),
(4, 5, 1, 29.90, '2025-11-15 09:00:00', 0),
(5, 8, 1, 29.90, '2026-02-20 10:00:00', 0),
(6, 10, 2, 99.90, '2026-03-05 10:00:00', 0),
(7, 1, 2, 99.90, '2026-01-20 11:00:00', 0),
(8, 4, 1, 29.90, '2026-04-01 12:00:00', 0),
(9, 9, 1, 29.90, '2026-04-15 13:00:00', 0),
(10, 6, 2, 99.90, '2026-05-01 14:00:00', 0),
(11, 11, 1, 29.90, '2026-03-10 10:00:00', 0),
(12, 2, 2, 99.90, '2026-05-15 14:00:00', 0);