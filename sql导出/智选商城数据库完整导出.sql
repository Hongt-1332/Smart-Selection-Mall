-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: web
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `web`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `web` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `web`;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userId` int NOT NULL COMMENT '用户ID关联user.id',
  `addressId` int NOT NULL COMMENT '地址编号用户下的地址序号',
  `country` varchar(50) NOT NULL COMMENT '国家',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `county` varchar(50) NOT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT '详细地址街道门牌号等',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `delete` tinyint(3) unsigned zerofill NOT NULL COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,1,1,'中国','北京市','北京市','朝阳区','建国路88','2026-09-27 13:56:05',000),(2,2,1,'中国','浙江省','杭州市','西湖区','文三路200号','2025-06-15 09:00:00',000),(3,3,1,'中国','陕西省','西安市','雁塔区','长安路600号','2025-07-01 10:00:00',000),(4,4,1,'中国','上海市','上海市','浦东新区','陆家嘴金融中心','2025-08-01 10:00:00',000),(5,5,1,'中国','广东省','广州市','天河区','天河路100号','2025-09-01 10:00:00',000),(6,6,1,'中国','四川省','成都市','武侯区','天府大道300号','2025-10-01 10:00:00',000),(7,7,1,'中国','湖北省','武汉市','洪山区','珞喻路400号','2025-11-01 10:00:00',000),(8,8,1,'中国','江苏省','南京市','鼓楼区','中山路500号','2025-12-01 10:00:00',000),(9,9,1,'中国','重庆市','重庆市','渝中区','解放碑步行街','2026-01-01 10:00:00',000),(10,10,1,'中国','福建省','厦门市','思明区','环岛路800号','2026-02-01 10:00:00',000),(11,11,1,'中国','天津市','天津市','和平区','南京路100号','2026-03-01 08:00:00',000),(12,12,1,'中国','湖南省','长沙市','岳麓区','麓山南路200号','2026-04-01 09:00:00',000),(13,13,1,'中国','福建省','龙岩市','掐','北门6854','2026-06-26 16:21:31',000),(14,13,2,'中国','福建省','龙岩市','002','366','2026-06-27 01:12:42',000),(15,1,2,'阿迪达斯','广东','梅州','三峡','广东人吃福建人','2026-09-16 10:25:40',000),(16,15,1,'中国','福建省','龙岩市','武平县','路下岗4-3','2026-09-16 17:57:00',000),(17,16,1,'中国','福','福州','鼓山','556-88','2026-09-27 13:13:50',000);
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai`
--

DROP TABLE IF EXISTS `ai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userId` int NOT NULL COMMENT '用户id',
  `goodsId` int NOT NULL COMMENT '商品ID关联goods.id',
  `category` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品分类',
  `kind` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品种类',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `price` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品价格',
  `simpleDescription` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '简要描述',
  `features` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '功能特性',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `delete` tinyint(3) unsigned zerofill NOT NULL COMMENT '逻辑删除0-未删除1-已删除',
  PRIMARY KEY (`id` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI推荐商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai`
--

LOCK TABLES `ai` WRITE;
/*!40000 ALTER TABLE `ai` DISABLE KEYS */;
INSERT INTO `ai` VALUES (13,13,18,'3345','345','345','345','345345','345345345','2026-06-27 16:52:15',000),(12,10,12,'电子产品','无人机','大疆 Mini 4 Pro','5788','轻量航拍无人机','4K|249g|O4图传','2026-02-15 11:00:00',000),(11,9,11,'电子产品','游戏主机','索尼 PS5 Pro','3999','次世代游戏主机','2TB|8K输出|光线追踪','2026-01-15 10:00:00',000),(10,8,10,'家用电器','吸尘器','戴森 V15 Detect','4999','智能无绳吸尘器','激光探测|LCD屏|60分钟','2025-12-15 14:00:00',000),(9,7,9,'电子产品','相机','佳能 EOS R6 II','15999','全画幅微单','2420万像素|4K60P|防抖','2025-12-01 13:00:00',000),(8,6,8,'电子产品','智能手表','Apple Watch Ultra 2','6499','户外旗舰手表','钛金属|49mm|深度计','2025-11-15 12:00:00',000),(7,5,7,'电子产品','平板电脑','iPad Air M2','4799','轻薄全能平板','M2芯片|11英寸|128GB','2025-11-01 11:00:00',000),(6,4,6,'电子产品','耳机','索尼 WH-1000XM5','2499','索尼旗舰降噪','30小时续航|Hi-Res|多点连接','2025-10-15 10:00:00',000),(5,2,5,'电子产品','耳机','AirPods Pro 2','1899','苹果降噪耳机','主动降噪|USB-C|空间音频','2025-10-01 14:00:00',000),(4,1,4,'电子产品','笔记本电脑','MacBook Pro 16','14999','苹果专业笔记本','M3 Pro芯片|18GB|512GB','2025-09-15 13:00:00',000),(3,3,3,'电子产品','智能手机','小米 14 Ultra','5999','小米旗舰','徕卡光学|骁龙8Gen3|1TB','2025-09-01 12:00:00',000),(2,2,2,'电子产品','智能手机','华为 Mate 60 Pro','6999','华为旗舰','卫星通话|昆仑玻璃|XMAGE','2025-08-15 11:00:00',000),(1,1,1,'电子产品','智能手机','iPhone 15 Pro','7999','苹果最新旗舰','4800万主摄|钛金属|USB-C','2025-08-01 10:00:00',000);
/*!40000 ALTER TABLE `ai` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `background`
--

DROP TABLE IF EXISTS `background`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `background` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userId` int NOT NULL COMMENT '用户ID关联user.id',
  `imagePath` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '背景图路径',
  `sequence` int NOT NULL COMMENT '序号位置',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='背景图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `background`
--

LOCK TABLES `background` WRITE;
/*!40000 ALTER TABLE `background` DISABLE KEYS */;
INSERT INTO `background` VALUES (64,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/100.jpg',4,'2026-09-27 14:00:00'),(63,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/75.jpg',3,'2026-09-27 14:00:00'),(62,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/83.jpg',2,'2026-09-27 14:00:00'),(61,15,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/50.jpg',4,'2026-09-27 14:00:00'),(60,15,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/77.jpg',3,'2026-09-27 14:00:00'),(59,15,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/50.jpg',2,'2026-09-27 14:00:00'),(58,15,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/21.jpg',1,'2026-09-27 14:00:00'),(57,14,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/73.jpg',4,'2026-09-27 14:00:00'),(56,14,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/8.jpg',3,'2026-09-27 14:00:00'),(55,14,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/54.jpg',2,'2026-09-27 14:00:00'),(54,14,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/74.jpg',1,'2026-09-27 14:00:00'),(53,13,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/45.jpg',4,'2026-09-27 14:00:00'),(52,13,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/30.jpg',3,'2026-09-27 14:00:00'),(51,13,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/77.jpg',2,'2026-09-27 14:00:00'),(50,12,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/86.jpg',4,'2026-09-27 14:00:00'),(49,12,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/13.jpg',3,'2026-09-27 14:00:00'),(48,12,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/7.jpg',2,'2026-09-27 14:00:00'),(47,11,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/80.jpg',4,'2026-09-27 14:00:00'),(46,11,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/22.jpg',3,'2026-09-27 14:00:00'),(45,11,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/72.jpg',2,'2026-09-27 14:00:00'),(44,10,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/38.jpg',4,'2026-09-27 14:00:00'),(43,10,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/36.jpg',3,'2026-09-27 14:00:00'),(42,10,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/23.jpg',2,'2026-09-27 14:00:00'),(41,9,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/68.jpg',4,'2026-09-27 14:00:00'),(40,9,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/74.jpg',3,'2026-09-27 14:00:00'),(39,9,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/100.jpg',2,'2026-09-27 14:00:00'),(38,8,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/82.jpg',4,'2026-09-27 14:00:00'),(37,8,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/92.jpg',3,'2026-09-27 14:00:00'),(36,8,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/56.jpg',2,'2026-09-27 14:00:00'),(35,7,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/52.jpg',4,'2026-09-27 14:00:00'),(34,7,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/9.jpg',3,'2026-09-27 14:00:00'),(33,7,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/53.jpg',2,'2026-09-27 14:00:00'),(32,6,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/69.jpg',4,'2026-09-27 14:00:00'),(31,6,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/16.jpg',3,'2026-09-27 14:00:00'),(30,6,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/15.jpg',2,'2026-09-27 14:00:00'),(29,5,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/99.jpg',4,'2026-09-27 14:00:00'),(28,5,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/44.jpg',3,'2026-09-27 14:00:00'),(27,5,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/14.jpg',2,'2026-09-27 14:00:00'),(26,4,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/57.jpg',4,'2026-09-27 14:00:00'),(25,4,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/50.jpg',3,'2026-09-27 14:00:00'),(24,4,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/9.jpg',2,'2026-09-27 14:00:00'),(23,3,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/59.jpg',4,'2026-09-27 14:00:00'),(22,3,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/12.jpg',3,'2026-09-27 14:00:00'),(21,3,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/34.jpg',2,'2026-09-27 14:00:00'),(20,2,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/51.jpg',4,'2026-09-27 14:00:00'),(19,2,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/62.jpg',3,'2026-09-27 14:00:00'),(18,2,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/86.jpg',2,'2026-09-27 14:00:00'),(17,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/goods/16/1790486837413_6695.jpg',1,'2026-09-27 13:27:18'),(16,1,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/goods/1/1789520829629_0049.jpg',4,'2026-09-16 09:07:10'),(15,1,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/goods/1/1789520825719_7782.jpg',2,'2026-09-16 09:07:06'),(14,1,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/goods/1/1789519831160_7813.jpg',3,'2026-09-16 08:50:31'),(13,13,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/13/goods/2e1981dd21d84182a81663afc73b4e4d_1782463982116.png',1,'2026-06-26 16:53:02'),(12,12,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/12/bg1.png',1,'2026-04-01 09:00:00'),(11,11,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/11/bg1.png',1,'2026-03-01 08:00:00'),(10,10,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/10/bg1.png',1,'2026-02-01 10:00:00'),(9,9,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/9/bg1.png',1,'2026-01-01 10:00:00'),(8,8,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/8/bg1.png',1,'2025-12-01 10:00:00'),(7,7,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/7/bg1.png',1,'2025-11-01 10:00:00'),(6,6,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/6/bg1.png',1,'2025-10-01 10:00:00'),(5,5,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/5/bg1.png',1,'2025-09-01 10:00:00'),(4,4,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/4/bg1.png',1,'2025-08-01 10:00:00'),(3,3,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/3/bg1.png',1,'2025-07-01 10:00:00'),(2,2,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/2/bg1.png',1,'2025-06-15 09:00:00'),(1,1,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/1/bg1.png',1,'2025-06-01 08:00:00');
/*!40000 ALTER TABLE `background` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userId` int NOT NULL COMMENT '用户ID关联user.id',
  `goodId` int NOT NULL COMMENT '商品ID关联goods.id',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '商品数量',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `goodId` (`goodId`),
  KEY `userId` (`userId`),
  CONSTRAINT `fk_cart_goods` FOREIGN KEY (`goodId`) REFERENCES `goods` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (2,2,3,2,'2026-01-02 11:00:00'),(3,3,1,1,'2026-01-03 12:00:00'),(4,4,5,1,'2026-01-04 13:00:00'),(5,5,7,1,'2026-01-05 14:00:00'),(6,6,9,1,'2026-01-06 15:00:00'),(7,7,10,2,'2026-01-07 16:00:00'),(8,8,4,1,'2026-01-08 17:00:00'),(9,9,6,1,'2026-01-09 18:00:00'),(10,10,8,1,'2026-01-10 19:00:00'),(11,11,3,1,'2026-04-15 10:00:00'),(12,12,1,2,'2026-05-01 11:00:00'),(42,13,3,1,'2026-06-27 23:55:30'),(43,13,1,1,'2026-06-28 01:31:24'),(44,1,5,2,'2026-09-16 10:41:16'),(45,16,8,3,'2026-09-27 13:32:17'),(46,16,27,3,'2026-09-27 13:43:51'),(47,13,11,1,'2026-09-28 09:33:04');
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods`
--

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userId` int NOT NULL COMMENT '商家用户ID关联user.id',
  `goodsId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `addressId` int NOT NULL DEFAULT '0' COMMENT '发货地址ID关联address.id',
  `preId` int DEFAULT NULL COMMENT '先前的商品ID',
  `goodsName` varchar(100) NOT NULL COMMENT '商品名称',
  `describe` varchar(1024) NOT NULL COMMENT '商品简介',
  `goodsPrice` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品单价保留两位小数',
  `goodsStock` int NOT NULL DEFAULT '0' COMMENT '商品库存数量',
  `imagePath` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品图片路径URL',
  `launch` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT '上架状态:0-下架1-上架',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '发布时间',
  `delete` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT '逻辑删除:0-未删除1-已删除',
  PRIMARY KEY (`id`),
  KEY `fk_goods_address` (`addressId`),
  KEY `goodsName` (`goodsName`),
  KEY `userId` (`userId`),
  CONSTRAINT `fk_goods_address` FOREIGN KEY (`addressId`) REFERENCES `address` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods`
--

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
INSERT INTO `goods` VALUES (1,1,'1',1,NULL,'iPhone 15 Pro','苹果最新旗舰手机',7999.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/1.webp',000,'2026-09-28 09:27:52',001),(2,2,'1',2,NULL,'华为 Mate 60 Pro','卫星通话旗舰手机',6999.00,40,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/81.jpg',001,'2026-09-28 09:27:52',000),(3,3,'1',3,NULL,'小米 14 Ultra','徕卡光学旗舰手机',5999.00,60,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/50.jpg',001,'2026-09-28 09:27:52',000),(4,1,'2',1,NULL,'MacBook Pro 16','M3 Pro芯片专业笔记本',14999.00,20,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/17.jpg',000,'2026-09-28 09:27:52',001),(5,2,'2',2,NULL,'AirPods Pro 2','主动降噪无线耳机',1899.00,100,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/47.jpg',001,'2026-09-28 09:27:52',000),(6,4,'1',4,NULL,'索尼 WH-1000XM5','无线降噪耳机，30小时续航',2499.00,60,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/17.jpg',001,'2026-09-28 09:27:52',000),(7,5,'1',5,NULL,'iPad Air M2','11英寸，128GB',4799.00,35,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/43.jpg',001,'2026-09-28 09:27:52',000),(8,6,'1',6,NULL,'Apple Watch Ultra 2','钛金属表壳，49mm',6499.00,25,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/53.jpg',001,'2026-09-28 09:27:52',000),(9,7,'1',7,NULL,'佳能 EOS R6 II','全画幅微单，2420万像素',15999.00,15,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/55.jpg',001,'2026-09-28 09:27:52',000),(10,8,'1',8,NULL,'戴森 V15 Detect','无绳吸尘器，激光探测',4999.00,45,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/67.jpg',001,'2026-09-28 09:27:52',000),(11,9,'1',9,NULL,'索尼 PS5 Pro','次世代游戏主机，2TB',3999.00,25,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/17.jpg',001,'2026-09-28 09:27:52',000),(12,10,'1',10,NULL,'大疆 Mini 4 Pro','轻量航拍无人机，4K',5788.00,20,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/92.jpg',001,'2026-09-28 09:27:52',000),(13,1,'1',1,1,'iPhone 15 Pro','苹果最新旗舰手机',7999.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/43.jpg',001,'2026-09-28 09:27:52',001),(14,1,'1',1,1,'iPhone 15 Pro','苹果最新旗舰手机',7999.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/58.jpg',001,'2026-09-28 09:27:52',001),(15,1,'1',1,1,'iPhone 15 Pro','苹果最新旗舰手机',7999.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/21.jpg',001,'2026-09-28 09:27:52',001),(16,1,'1',1,1,'iPhone 15 Pro','苹果最新旗舰手机',7999.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/46.jpg',000,'2026-09-28 09:27:52',001),(17,13,'3caee44767624f73aa0a919992c572fa',13,NULL,'7457','4567',0.01,0,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/5.jpg',000,'2026-09-28 09:27:52',001),(18,13,'023c1cc1d99144458d9c31dfc20a9bbc',13,17,'7457','886',0.01,0,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/20.jpg',000,'2026-09-28 09:27:52',001),(19,13,'07c949e474df423aab6458532ae12f9e',13,18,'7457','886',0.01,3,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/95.jpg',001,'2026-09-28 09:27:52',000),(20,1,'a3e57c337b73433aa530b50919a18785',1,1,'iPhone 15 Pro','苹果最新旗舰手机',7999.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/39.jpg',000,'2026-09-28 09:27:52',000),(21,1,'922820a830d1476b8ab675f41dfc3d70',1,4,'MacBook Pro 16','M3 Pro芯片专业笔记本',14999.00,20,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/73.jpg',000,'2026-09-28 09:27:53',000),(22,1,'9d8286c571174180a042844ab1fdbd27',1,NULL,'dasd','asdas',1565.00,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/24.jpg',000,'2026-09-28 09:27:53',001),(23,1,'5ce9f20b6e5745149c7d69c19ee3e0fd',1,NULL,'智选商城','asdas',1565.00,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/41.jpg',000,'2026-09-28 09:27:53',001),(24,1,'a2132a5b270a43cca79e77161b1963a1',1,NULL,'智选商','asdas',1565.00,16,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/88.jpg',000,'2026-09-28 09:27:53',001),(25,1,'245bbc3f214a4c378cedd045076d2aa2',1,NULL,'asd','werewt',45.00,46,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/6.jpg',000,'2026-09-28 09:27:53',000),(26,15,'1f22454c43724e2ba05ee3e06f445e6a',16,NULL,'asdasd','rtgrtgret',20.00,10,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/1.webp',000,'2026-09-28 09:27:53',001),(27,15,'3428e942b2ae43789925345d59602d6a',16,26,'asdasd','rtgrtgret',20.00,10,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/35.jpg',001,'2026-09-28 09:27:53',000),(28,16,'2e60c826574841e9b155cec5f87f7a82',17,NULL,'Iphone-18','便宜的苹果手机',1314520.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/6.jpg',000,'2026-09-28 09:27:53',001),(29,16,'d941c9497f4f48ec80303f5332adf5eb',17,28,'Iphone-18','便宜的苹果手机',1314520.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/38.jpg',000,'2026-09-28 09:27:53',001),(30,16,'030b2bef64ee4f078f89415d32e21d37',17,29,'Iphone-18','便宜的苹果手',1314520.00,50,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/64.jpg',000,'2026-09-28 09:27:53',000),(31,9,'1',9,11,'索尼 PS5 Pro','次世代游戏主机，2TB',3999.00,25,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/65.jpg',001,'2026-09-28 09:27:53',000);
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trade`
--

DROP TABLE IF EXISTS `trade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trade` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `orderId` varchar(50) NOT NULL COMMENT '订单号唯一标识一笔订单',
  `userId` int NOT NULL COMMENT '买家用户ID关联user.id',
  `goodId` int NOT NULL COMMENT '商品ID关联goods.id',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量',
  `originAddress` varchar(50) NOT NULL COMMENT '发货地址',
  `currentAddress` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标地址',
  `targetAddress` varchar(255) NOT NULL COMMENT '当前地址',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '下单时间',
  `payTime` datetime DEFAULT NULL,
  `cancelTime` datetime DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `delete` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT '逻辑删除:0-未删除1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `orderId` (`orderId`),
  KEY `createTime` (`createTime`),
  KEY `goodId` (`goodId`),
  KEY `userId` (`userId`),
  CONSTRAINT `fk_trade_goods` FOREIGN KEY (`goodId`) REFERENCES `goods` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_trade_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade`
--

LOCK TABLES `trade` WRITE;
/*!40000 ALTER TABLE `trade` DISABLE KEYS */;
INSERT INTO `trade` VALUES (1,'ORD001',1,2,1,'杭州市西湖区','北京市朝阳区','北京市朝阳区','2025-12-01 10:00:00','2025-12-01 10:05:00',NULL,'2025-12-03 15:00:00',000),(2,'ORD002',2,3,2,'西安市雁塔区','成都市武侯区','杭州市西湖区','2025-12-10 11:00:00','2025-12-10 11:10:00',NULL,NULL,000),(3,'ORD003',3,1,1,'北京市朝阳区','西安市雁塔区','西安市雁塔区','2025-12-20 12:00:00',NULL,'2025-12-20 13:00:00',NULL,000),(4,'ORD004',1,5,1,'杭州市西湖区','北京市朝阳区','北京市朝阳区','2026-01-01 09:00:00','2026-01-01 09:05:00',NULL,NULL,000),(5,'ORD005',4,6,1,'上海市浦东新区','上海市浦东新区','上海市浦东新区','2026-01-15 10:00:00','2026-01-15 10:10:00',NULL,'2026-01-18 14:00:00',000),(6,'ORD006',5,4,1,'广州市天河区','广州市天河区','广州市天河区','2026-02-01 11:00:00','2026-02-01 11:05:00',NULL,NULL,000),(7,'ORD007',6,8,1,'成都市武侯区','武汉市洪山区','武汉市洪山区','2026-02-15 12:00:00',NULL,NULL,NULL,000),(8,'ORD008',7,10,1,'武汉市洪山区','武汉市洪山区','武汉市洪山区','2026-03-01 13:00:00','2026-03-01 13:15:00',NULL,'2026-03-05 16:00:00',000),(9,'ORD009',8,7,2,'南京市鼓楼区','南京市鼓楼区','南京市鼓楼区','2026-03-15 14:00:00','2026-03-15 14:10:00',NULL,NULL,000),(10,'ORD010',9,9,1,'重庆市渝中区','重庆市渝中区','重庆市渝中区','2026-04-01 15:00:00',NULL,'2026-04-02 10:00:00',NULL,000),(11,'ORD011',11,2,1,'天津市和平区','天津市和平区','天津市和平区','2026-04-10 10:00:00','2026-04-10 10:05:00',NULL,'2026-04-12 15:00:00',000),(12,'ORD012',12,5,1,'长沙市岳麓区','长沙市岳麓区','长沙市岳麓区','2026-05-01 09:00:00','2026-05-01 09:10:00',NULL,NULL,000),(13,'597b799b86e140a2aef132ae180a5ab3',13,19,1,'北门6854','北门6854','366','2026-06-27 19:21:21',NULL,NULL,NULL,000),(14,'c0c5f1823913458da4f9060ce8903ddb',13,1,1,'建国路88','建国路88','366','2026-06-28 00:59:12',NULL,'2026-06-28 00:59:12',NULL,000),(15,'2df46289d5784480bbb4ab3e9a5802d9',13,1,1,'建国路88','建国路88','366','2026-07-03 16:14:09',NULL,'2026-07-03 16:14:09',NULL,000);
/*!40000 ALTER TABLE `trade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userName` varchar(20) NOT NULL COMMENT '用户名用于显示和登录',
  `account` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL COMMENT '登录密码加密存储',
  `salt` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'user',
  `wechatOpenid` varchar(100) DEFAULT NULL,
  `defaultAddressId` int DEFAULT NULL COMMENT '默认地址Id',
  `balance` decimal(38,2) unsigned zerofill NOT NULL DEFAULT '000000000000000000000000000000000000.00',
  `email` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `describe` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `avatarPath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `level` int DEFAULT '0',
  `vipCreateTime` datetime DEFAULT NULL,
  `vipDuration` int DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `delete` tinyint(3) unsigned zerofill NOT NULL COMMENT 'true 删除 false 未删除',
  `uploadTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `uploadBackground` int(10) unsigned zerofill NOT NULL,
  `uploadGoods` int(10) unsigned zerofill NOT NULL,
  `updateAvatar` int(10) unsigned zerofill NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account` (`account`),
  KEY `defaultAddressId` (`defaultAddressId`),
  KEY `userName` (`userName`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'张三','zhan','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,1,000000000000000000000000000000000800.10,'zhangsan@qq.com','13800138001','数码爱好者','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/59.jpg',3,'2026-09-16 10:21:04',30,'2025-06-01 08:00:00',000,'2026-09-16 09:07:10',0000000003,0000000003,0000000001),(2,'李四','lisi','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,2,000000000000000000000000000000000500.00,'lisi@qq.com','13800138002','品质生活追求者','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/98.jpg',0,NULL,NULL,'2025-06-15 09:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(3,'王五','wangwu','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,3,000000000000000000000000000000002000.00,'wangwu@qq.com','13800138003','资深买手','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/90.jpg',2,'2026-01-10 14:00:00',365,'2025-07-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(4,'赵六','zhaoliu','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,4,000000000000000000000000000000000800.00,'zhaoliu@qq.com','13800138004','旅行达人','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/74.jpg',0,NULL,NULL,'2025-08-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(5,'孙七','sunqi','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,5,000000000000000000000000000000001500.00,'sunqi@qq.com','13800138005','美食家','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/99.jpg',1,'2025-11-15 09:00:00',30,'2025-09-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(6,'周八','zhouba','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,6,000000000000000000000000000000003000.00,'zhouba@qq.com','13800138006','极客玩家','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/54.jpg',0,NULL,NULL,'2025-10-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(7,'吴九','wujiu','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,7,000000000000000000000000000000000600.00,'wujiu@qq.com','13800138007','运动爱好者','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/52.jpg',0,NULL,NULL,'2025-11-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(8,'郑十','zhengshi','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,8,000000000000000000000000000000001200.00,'zhengshi@qq.com','13800138008','文艺青年','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/28.jpg',1,'2026-02-20 10:00:00',30,'2025-12-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(9,'钱十一','qianshiyi','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,9,000000000000000000000000000000002500.00,'qianshiyi@qq.com','13800138009','音乐发烧友','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/68.jpg',0,NULL,NULL,'2026-01-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(10,'陈十二','chenshier','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,10,000000000000000000000000000000005000.00,'chenshier@qq.com','13800138010','摄影达人','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/99.jpg',2,'2026-03-05 10:00:00',365,'2026-02-01 10:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(11,'冯十三','fengshisan','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,11,000000000000000000000000000000003500.00,'fengshisan@qq.com','13800138011','健身达人','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/50.jpg',1,'2026-03-10 10:00:00',30,'2026-03-01 08:00:00',001,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(12,'褚十四','chushisi','fb0b65b9f8d2777abddd99ac62f57dda6b6e942c2b5158a4ea9a7aa957d08f9e','a1b2c3d4e5f6g7h8','user',NULL,12,000000000000000000000000000000001800.00,'chushisi@qq.com','13800138012','读书爱好者','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/38.jpg',0,NULL,NULL,'2026-04-01 09:00:00',000,'0000-00-00 00:00:00',0000000000,0000000000,0000000000),(13,'KaiKAIKAIKAI','KaiXin123!','97e8f84ffe994f3cceee490a2d53517548a8eda20451cc49712ea6b2e9370158','24589e977c724181','user',NULL,14,000000000000000000000000000000010000.00,'q3573448382@qq.com','18396330957','woshi1','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/74.jpg',0,NULL,365,'2026-06-26 11:42:06',000,'2026-06-27 18:56:53',0000000001,0000000003,0000000003),(14,'z9527','z95279527','5bffd5d6805d4dc117c2700e33e994624fca50306dfe1e9007d9a76d69ea0973','8efb92098c724572','user',NULL,NULL,000000000000000000000000000000000000.00,'226164920@qq.com','15170206449',NULL,'https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/19.jpg',0,NULL,NULL,'2026-07-03 00:37:29',000,'2026-07-03 00:37:29',0000000000,0000000000,0000000000),(15,'微信用户test_0b1','wx_test_0b1e2f0w3yS','','','user','test_0b1e2f0w3ySnM73Dke1w3ahE6n3e2f0C',NULL,000000000000000000000000000000000000.00,'','','','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/58.jpg',0,NULL,NULL,'2026-09-16 17:50:14',000,'2026-09-16 17:57:26',0000000000,0000000002,0000000000),(16,'微信用户test_tes','wx_test_test_openid','','','user','test_test_openid_13',17,000000000000000000000000000000000000.00,'q357@qq.com','18850727637','abcdefg','https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/78.jpg',0,NULL,NULL,'2026-09-27 13:02:11',000,'2026-09-27 13:56:33',0000000001,0000000003,0000000001);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vipconfig`
--

DROP TABLE IF EXISTS `vipconfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vipconfig` (
  `level` int NOT NULL COMMENT '用户等级',
  `maxAddressQuantity` int NOT NULL,
  `monthlyUpdateGoods` int NOT NULL DEFAULT '100' COMMENT '每个月上传和更新货物次数总和',
  `monthlyUpdateAvatar` int NOT NULL DEFAULT '7' COMMENT '每个月上传头像次数',
  `monthlyUpdateBackground` int NOT NULL DEFAULT '2' COMMENT '每个月上传背景次数',
  `maxCartQuantity` int NOT NULL DEFAULT '5' COMMENT '购物车最大数量',
  `maxGoodsQuantity` int NOT NULL DEFAULT '5' COMMENT '货物最大数量',
  `maxAiQuantity` int NOT NULL,
  `vipDuration` int NOT NULL DEFAULT '30' COMMENT 'VIP有效期(天)',
  `price` decimal(10,2) NOT NULL COMMENT 'vip价格',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `duration` int DEFAULT NULL COMMENT 'vip持续时间',
  `levelName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '等级名称',
  PRIMARY KEY (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户等级配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vipconfig`
--

LOCK TABLES `vipconfig` WRITE;
/*!40000 ALTER TABLE `vipconfig` DISABLE KEYS */;
INSERT INTO `vipconfig` VALUES (0,3,5,1,1,10,10,5,0,0.00,'2026-06-24 16:36:25',0,'普通用户'),(1,5,10,3,3,20,20,10,30,29.90,'2026-06-25 01:49:02',30,'月度VIP'),(2,10,30,10,10,50,50,30,365,99.90,'2026-06-25 01:49:02',365,'年度VIP'),(3,15,100,7,2,5,5,10,30,199.90,'2026-06-26 14:01:35',1000,'至尊企业');
/*!40000 ALTER TABLE `vipconfig` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `viptrade`
--

DROP TABLE IF EXISTS `viptrade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `viptrade` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID自增',
  `userId` int NOT NULL COMMENT '用户ID关联user.id',
  `level` int NOT NULL COMMENT 'VIP等级',
  `money` decimal(10,2) NOT NULL COMMENT '交易金额',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `delete` tinyint(3) unsigned zerofill NOT NULL,
  `num` int(10) unsigned zerofill DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP交易记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `viptrade`
--

LOCK TABLES `viptrade` WRITE;
/*!40000 ALTER TABLE `viptrade` DISABLE KEYS */;
INSERT INTO `viptrade` VALUES (1,1,1,29.90,'2025-12-01 10:00:00',000,0000000001),(2,3,2,99.90,'2026-01-05 14:00:00',000,0000000000),(3,3,1,29.90,'2026-01-10 15:00:00',000,0000000000),(4,5,1,29.90,'2025-11-15 09:00:00',000,0000000000),(5,8,1,29.90,'2026-02-20 10:00:00',000,0000000000),(6,10,2,99.90,'2026-03-05 10:00:00',000,0000000000),(7,1,2,99.90,'2026-01-20 11:00:00',000,0000000000),(8,4,1,29.90,'2026-04-01 12:00:00',000,0000000000),(9,9,1,29.90,'2026-04-15 13:00:00',000,0000000000),(10,6,2,99.90,'2026-05-01 14:00:00',000,0000000000),(11,11,1,29.90,'2026-03-10 10:00:00',000,0000000000),(12,2,2,99.90,'2026-05-15 14:00:00',000,0000000000),(13,1,3,199.90,'2026-09-16 10:21:04',000,0000000001);
/*!40000 ALTER TABLE `viptrade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'web'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-28 13:19:40
