-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: house_service
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chat_conversation`
--

DROP TABLE IF EXISTS `chat_conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `conversation_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cc_user` (`user_id`),
  CONSTRAINT `fk_cc_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_conversation`
--

LOCK TABLES `chat_conversation` WRITE;
/*!40000 ALTER TABLE `chat_conversation` DISABLE KEYS */;
INSERT INTO `chat_conversation` VALUES (1,'/chat/1','2025-09-20 09:00:00.000001','c1','Tư vấn vệ sinh','2025-09-20 09:10:00.000001',1),(2,'/chat/2','2025-09-20 09:05:00.000001','c2','Điều hòa','2025-09-20 09:15:00.000001',2),(3,'/chat/3','2025-09-20 09:10:00.000001','c3','Ống nước','2025-09-20 09:20:00.000001',3),(4,'/chat/4','2025-09-20 09:15:00.000001','c4','Sơn nhà','2025-09-20 09:25:00.000001',4),(5,'/chat/5','2025-09-20 09:20:00.000001','c5','Chống thấm','2025-09-20 09:30:00.000001',5),(6,'/chat/6','2025-09-20 09:25:00.000001','c6','Giặt sofa','2025-09-20 09:35:00.000001',6),(7,'/chat/7','2025-09-20 09:30:00.000001','c7','Điện','2025-09-20 09:40:00.000001',7),(8,'/chat/8','2025-09-20 09:35:00.000001','c8','Lắp đèn','2025-09-20 09:45:00.000001',8),(9,'/chat/9','2025-09-20 09:40:00.000001','c9','Bảo trì','2025-09-20 09:50:00.000001',9),(10,'/chat/10','2025-09-20 09:45:00.000001','c10','Vệ sinh CN','2025-09-20 09:55:00.000001',10),(11,'/chat/so-v15-0001','2025-09-22 22:49:15.000000','CONV-SO1','Trao đổi SO-V15-0001','2025-09-22 22:49:15.000000',19);
/*!40000 ALTER TABLE `chat_conversation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answer` mediumtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `question` text COLLATE utf8mb4_unicode_ci,
  `user_id` bigint DEFAULT NULL,
  `conversation_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cm_conv` (`conversation_id`),
  KEY `idx_cm_user` (`user_id`),
  CONSTRAINT `fk_cm_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
INSERT INTO `chat_message` VALUES (1,'Chào bạn, bạn cần vệ sinh khu vực nào?','2025-09-20 09:01:00.000001','Giá vệ sinh?',1,1),(2,'Bảo dưỡng gồm vệ sinh dàn lạnh.','2025-09-20 09:06:00.000001','Bảo dưỡng DH bao lâu?',2,2),(3,'Có thể qua trong ngày.','2025-09-20 09:11:00.000001','Ống nước rò bao lâu xong?',3,3),(4,'Sơn 2 lớp trong 5 giờ.','2025-09-20 09:16:00.000001','Mất bao lâu?',4,4),(5,'Bảo hành 6 tháng.','2025-09-20 09:21:00.000001','Chống thấm bảo hành?',5,5),(6,'Sofa 3 chỗ ~90 phút.','2025-09-20 09:26:00.000001','Giặt sofa lâu không?',6,6),(7,'Thay công tắc 30 phút.','2025-09-20 09:31:00.000001','Sửa ổ cắm?',7,7),(8,'Lắp 5 đèn trong 2 giờ.','2025-09-20 09:36:00.000001','Lắp đèn downlight?',8,8),(9,'Gói tháng gồm 2 lượt kiểm tra.','2025-09-20 09:41:00.000001','Bảo trì tòa nhà?',9,9),(10,'Vệ sinh xưởng tính theo m².','2025-09-20 09:46:00.000001','Giá tính sao?',10,10),(11,NULL,'2025-09-21 08:30:00.000000','Bên mình có thể đến sớm hơn 15 phút không?',19,11),(12,'Được, kỹ thuật sẽ có mặt 8:45.','2025-09-21 08:35:00.000000',NULL,15,11);
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `type` enum('FIXED','PERCENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` decimal(12,2) NOT NULL,
  `scope` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SERVICE',
  `start_at` datetime DEFAULT NULL,
  `end_at` datetime DEFAULT NULL,
  `max_discount_amount` decimal(12,2) DEFAULT '0.00',
  `usage_limit_global` int DEFAULT '0',
  `usage_limit_per_user` int DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_code` (`code`),
  UNIQUE KEY `ix_coupon_code` (`code`),
  KEY `ix_coupon_active` (`is_active`),
  KEY `ix_coupon_time` (`start_at`,`end_at`),
  CONSTRAINT `chk_coupon_scope` CHECK ((`scope` = _utf8mb4'SERVICE'))
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (6,'SRV10','Giảm 10% dịch vụ','Coupon 10% cho dịch vụ','PERCENT',10.00,'SERVICE',NULL,NULL,200000.00,0,0,1,'2025-09-20 05:24:29','2025-09-26 01:36:00'),(7,'SRV100K','Giảm 100K dịch vụ','Coupon 100k cho dịch vụ','FIXED',100000.00,'SERVICE',NULL,NULL,0.00,0,0,0,'2025-09-20 05:24:29','2025-09-26 01:42:34'),(8,'SRV5','Giảm 5% dịch vụ','Coupon 5% cho dịch vụ','PERCENT',5.00,'SERVICE',NULL,NULL,0.00,0,0,0,'2025-09-20 05:24:29','2025-09-26 01:42:33'),(9,'SRV200K','Giảm 200K dịch vụ','Coupon 20% cho dịch vụ','FIXED',200000.00,'SERVICE',NULL,NULL,0.00,0,0,1,'2025-09-20 05:24:29','2025-09-25 16:03:53'),(10,'SV20','Giảm 15% dịch vụ','Coupon 15% cho dịch vụ','PERCENT',20.00,'SERVICE','2025-09-27 08:35:00','2025-09-30 08:35:00',500000.00,0,1,1,'2025-09-20 05:24:29','2025-09-26 01:35:55'),(11,'VHBTV10','Giảm 10% dịch vụ','Coupon 10% cho dịch vụ','PERCENT',10.00,'SERVICE','2025-09-01 00:00:00','2025-12-31 23:59:59',100000.00,0,0,1,'2025-09-22 15:49:15','2025-09-25 16:03:53'),(12,'CLEAN50K','Giảm 50K dịch vụ','Giảm cố định 50.000đ','FIXED',50000.00,'SERVICE','2025-09-01 00:00:00','2025-12-31 23:59:59',0.00,0,0,1,'2025-09-22 15:49:15','2025-09-22 15:49:15'),(13,'VIP50K','Giảm 50k đơn hàng dưới 20000k',NULL,'FIXED',50000.00,'SERVICE','2025-09-03 08:34:00','2025-10-05 08:34:00',50000.00,0,0,1,'2025-09-26 01:35:15','2025-09-26 01:35:15'),(14,'NMTN10PCT','Giảm 10% tối đa 50K',NULL,'PERCENT',10.00,'SERVICE','2025-09-24 16:05:19','2025-10-31 16:05:19',50000.00,100,1,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(15,'NMTN50K','Giảm 50.000đ',NULL,'FIXED',50000.00,'SERVICE','2025-09-28 16:05:19','2025-11-30 16:05:19',NULL,0,0,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(16,'NMTN15PCT','Giảm 15% tối đa 80K',NULL,'PERCENT',15.00,'SERVICE','2025-10-01 16:05:19','2025-10-11 16:05:19',80000.00,500,2,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(17,'NMTN100K','Giảm 100.000đ (đã hết hạn)',NULL,'FIXED',100000.00,'SERVICE','2025-09-01 16:05:19','2025-09-30 16:05:19',NULL,0,0,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(18,'NMTNBF25','Black Friday 25% tối đa 150K',NULL,'PERCENT',25.00,'SERVICE','2025-10-31 16:05:19','2025-11-10 16:05:19',150000.00,0,1,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(19,'NMTN30K','Giảm 30.000đ (đang tắt)',NULL,'FIXED',30000.00,'SERVICE','2025-09-30 16:05:19','2025-12-30 16:05:19',NULL,0,0,0,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(20,'NMTN5PCT','Giảm 5% tối đa 20K',NULL,'PERCENT',5.00,'SERVICE','2025-09-29 16:05:19','2025-10-21 16:05:19',20000.00,0,0,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(21,'NMTN70K','Giảm 70.000đ',NULL,'FIXED',70000.00,'SERVICE','2025-09-26 16:05:19','2025-10-26 16:05:19',NULL,200,1,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(22,'NMTN20PCT','Giảm 20% tối đa 120K',NULL,'PERCENT',20.00,'SERVICE','2025-09-21 16:05:19','2025-10-16 16:05:19',120000.00,0,1,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(23,'NMTN150K','Giảm 150.000đ',NULL,'FIXED',150000.00,'SERVICE','2025-09-30 16:05:19','2025-11-15 16:05:19',NULL,50,1,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(24,'NMTN8PCT','Giảm 8% tối đa 40K',NULL,'PERCENT',8.00,'SERVICE','2025-09-27 16:05:19','2025-11-05 16:05:19',40000.00,0,0,1,'2025-10-01 09:05:19','2025-10-01 09:05:19'),(25,'NMTN200K','Giảm 200.000đ',NULL,'FIXED',200000.00,'SERVICE','2025-09-25 16:05:19','2025-12-10 16:05:19',NULL,0,1,1,'2025-10-01 09:05:19','2025-10-01 09:05:19');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_redemption`
--

DROP TABLE IF EXISTS `coupon_redemption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_redemption` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `target_type` enum('PRODUCT_ORDER','SERVICE_ORDER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `amount_discounted` decimal(12,2) NOT NULL,
  `redeemed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `vendor_service_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cr_once_per_target` (`coupon_id`,`target_type`,`target_id`),
  KEY `idx_cr_coupon_user` (`coupon_id`,`user_id`),
  KEY `ix_redemption_coupon` (`coupon_id`),
  KEY `ix_redemption_user` (`user_id`),
  KEY `ix_redemption_service` (`vendor_service_id`),
  KEY `ix_redemption_time` (`redeemed_at`),
  CONSTRAINT `fk_cr_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cr_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKjqronyv1wcbpxh7ogo3q5kex1` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_redemption`
--

LOCK TABLES `coupon_redemption` WRITE;
/*!40000 ALTER TABLE `coupon_redemption` DISABLE KEYS */;
INSERT INTO `coupon_redemption` VALUES (4,8,6,'SERVICE_ORDER',6,25000.00,'2025-09-20 10:15:00',NULL),(5,6,1,'SERVICE_ORDER',1,60000.00,'2025-09-20 10:20:00',NULL),(6,6,3,'SERVICE_ORDER',3,40000.00,'2025-09-20 10:25:00',NULL),(7,7,5,'SERVICE_ORDER',5,100000.00,'2025-09-20 10:30:00',NULL),(8,6,9,'SERVICE_ORDER',9,500000.00,'2025-09-20 10:35:00',NULL),(9,10,10,'SERVICE_ORDER',10,525000.00,'2025-09-20 10:40:00',NULL),(11,11,19,'SERVICE_ORDER',11,65000.00,'2025-09-21 08:19:00',NULL),(12,12,19,'SERVICE_ORDER',13,50000.00,'2025-09-22 11:21:00',NULL);
/*!40000 ALTER TABLE `coupon_redemption` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_service`
--

DROP TABLE IF EXISTS `coupon_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_service` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `vendor_service_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_service` (`coupon_id`,`vendor_service_id`),
  KEY `fk_csvc_vs` (`vendor_service_id`),
  CONSTRAINT `fk_csvc_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_csvc_vs` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_service`
--

LOCK TABLES `coupon_service` WRITE;
/*!40000 ALTER TABLE `coupon_service` DISABLE KEYS */;
INSERT INTO `coupon_service` VALUES (1,6,1),(2,6,2),(3,6,9),(4,7,3),(5,7,8),(6,8,4),(7,8,7),(8,9,5),(9,10,6),(10,10,10),(11,11,11),(12,11,12),(13,12,14),(14,13,5);
/*!40000 ALTER TABLE `coupon_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_user`
--

DROP TABLE IF EXISTS `coupon_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_user` (`coupon_id`,`user_id`),
  KEY `fk_cu_user` (`user_id`),
  CONSTRAINT `fk_cu_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cu_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_user`
--

LOCK TABLES `coupon_user` WRITE;
/*!40000 ALTER TABLE `coupon_user` DISABLE KEYS */;
INSERT INTO `coupon_user` VALUES (1,6,6),(2,7,7),(3,8,8),(4,9,9),(5,10,10),(6,11,19),(7,11,20),(8,12,19),(9,13,18),(10,13,19),(15,25,9001),(14,25,9004),(13,25,9005),(12,25,9102);
/*!40000 ALTER TABLE `coupon_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_vendor`
--

DROP TABLE IF EXISTS `coupon_vendor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_vendor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_vendor` (`coupon_id`,`vendor_id`),
  KEY `FK4fltnac2a858pe8bew7j5wir6` (`vendor_id`),
  CONSTRAINT `FK3j7p9uh0nto0a545743k9xky8` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`),
  CONSTRAINT `FK4fltnac2a858pe8bew7j5wir6` FOREIGN KEY (`vendor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_vendor`
--

LOCK TABLES `coupon_vendor` WRITE;
/*!40000 ALTER TABLE `coupon_vendor` DISABLE KEYS */;
INSERT INTO `coupon_vendor` VALUES (5,11,6),(1,13,3),(4,13,4),(2,13,5),(3,13,8),(7,14,15),(13,15,15),(9,16,15),(6,17,15),(17,18,15),(12,19,15),(14,20,15),(15,21,15),(11,22,15),(8,23,15),(16,24,15),(10,25,15);
/*!40000 ALTER TABLE `coupon_vendor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_profile`
--

DROP TABLE IF EXISTS `customer_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_line` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_profile_user` (`user_id`),
  CONSTRAINT `fk_customer_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_profile`
--

LOCK TABLES `customer_profile` WRITE;
/*!40000 ALTER TABLE `customer_profile` DISABLE KEYS */;
INSERT INTO `customer_profile` VALUES (1,1,'Nguyễn Văn A','1995-01-01','MALE','12 Lê Lợi, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,2,'Trần Thị B','1996-02-02','FEMALE','34 Hai Bà Trưng, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(3,3,'Lê Văn C','1997-03-03','MALE','56 Nguyễn Trãi, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,4,'Phạm Thị D','1998-04-04','FEMALE','78 Trần Hưng Đạo, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,5,'Hoàng Văn E','1994-05-05','MALE','90 Giảng Võ, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,6,'Đinh Thị F','1993-06-06','FEMALE','11 Cầu Giấy, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,7,'Bùi Văn G','1992-07-07','MALE','22 Tây Sơn, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,8,'Đỗ Thị H','1991-08-08','FEMALE','33 Kim Mã, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,9,'Vũ Văn I','1990-09-09','MALE','44 Xã Đàn, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,10,'Phan Thị K','1989-10-10','FEMALE','55 Phạm Hùng, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(11,14,'Trần Hải Việt','2004-08-09','MALE','Hanoi','2025-09-21 10:05:19','2025-09-21 10:05:19'),(12,15,'Trần Hải Tũn','2001-09-11','MALE','America','2025-09-21 10:07:39','2025-09-21 10:07:39'),(13,18,'Kwon Ji Yong','2002-02-20','MALE','Hanoi','2025-09-22 03:23:40','2025-09-22 03:23:40'),(14,9101,'Nguyễn An',NULL,NULL,'Hà Nội','2025-09-27 17:51:21','2025-09-27 17:51:21'),(15,9102,'Trần Bình',NULL,NULL,'Hà Nội','2025-09-27 17:51:21','2025-09-27 17:51:21'),(16,9103,'Lê Cường',NULL,NULL,'Hà Nội','2025-09-27 17:51:21','2025-09-27 17:51:21');
/*!40000 ALTER TABLE `customer_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `pay_target_type` enum('PRODUCT_ORDER','SERVICE_ORDER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `pay_target_id` bigint NOT NULL,
  `provider` enum('COD','MOMO','STRIPE','VNPAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `currency` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'VND',
  `status` enum('FAILED','PAID','PENDING','REFUNDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_ref` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_payment_user` (`user_id`),
  CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `payment_chk_amount` CHECK ((`amount` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=9813 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,1,'PRODUCT_ORDER',1,'COD',230000.00,'VND','PAID','TXN-P-0001','2025-09-20 11:00:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,3,'PRODUCT_ORDER',3,'MOMO',405000.00,'VND','PAID','TXN-P-0003','2025-09-20 11:05:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(3,4,'PRODUCT_ORDER',4,'VNPAY',600000.00,'VND','PAID','TXN-P-0004','2025-09-20 11:06:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,5,'PRODUCT_ORDER',5,'STRIPE',1161000.00,'VND','PENDING','TXN-P-0005',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,8,'PRODUCT_ORDER',8,'COD',2100000.00,'VND','PAID','TXN-P-0008','2025-09-20 11:10:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,1,'SERVICE_ORDER',1,'MOMO',540000.00,'VND','PAID','TXN-S-0001','2025-09-20 12:00:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,3,'SERVICE_ORDER',3,'VNPAY',360000.00,'VND','PAID','TXN-S-0003','2025-09-20 12:05:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,5,'SERVICE_ORDER',5,'COD',1700000.00,'VND','PENDING','TXN-S-0005',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,9,'SERVICE_ORDER',9,'STRIPE',4500000.00,'VND','PAID','TXN-S-0009','2025-09-20 12:10:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,10,'SERVICE_ORDER',10,'MOMO',2975000.00,'VND','PAID','TXN-S-0010','2025-09-20 12:15:00','2025-09-20 05:24:29','2025-09-20 05:24:29'),(11,19,'SERVICE_ORDER',11,'VNPAY',585000.00,'VND','PAID','VNPAY-20250921-0001','2025-09-21 08:20:00','2025-09-22 15:49:15','2025-09-22 15:49:15'),(12,20,'SERVICE_ORDER',12,'COD',250000.00,'VND','PAID','COD-20250920-0002','2025-09-20 16:30:00','2025-09-22 15:49:15','2025-09-22 15:49:15'),(9801,9101,'SERVICE_ORDER',9601,'MOMO',500000.00,'VND','PAID','SEED-SO-9601-MOMO','2025-09-22 09:00:00','2025-09-22 02:00:00','2025-09-22 02:00:00'),(9802,9102,'SERVICE_ORDER',9602,'COD',350000.00,'VND','PAID','SEED-SO-9602-COD','2025-09-23 10:30:00','2025-09-23 03:30:00','2025-09-23 03:30:00'),(9803,9103,'SERVICE_ORDER',9603,'VNPAY',700000.00,'VND','PENDING','SEED-SO-9603-VNPAY',NULL,'2025-09-24 07:15:00','2025-09-24 07:15:00'),(9804,9101,'SERVICE_ORDER',9604,'MOMO',600000.00,'VND','FAILED','SEED-SO-9604-MOMO',NULL,'2025-09-24 09:00:00','2025-09-24 09:00:00'),(9805,9102,'SERVICE_ORDER',9605,'STRIPE',750000.00,'VND','PAID','SEED-SO-9605-STRIPE','2025-09-25 11:45:00','2025-09-25 04:45:00','2025-09-25 04:45:00'),(9806,9101,'SERVICE_ORDER',9606,'VNPAY',900000.00,'VND','PAID','SEED-SO-9606-VNPAY','2025-09-26 09:20:00','2025-09-26 02:20:00','2025-09-26 02:20:00'),(9807,9102,'SERVICE_ORDER',9607,'COD',650000.00,'VND','PENDING','SEED-SO-9607-COD',NULL,'2025-09-27 01:05:00','2025-09-27 01:05:00'),(9808,9103,'SERVICE_ORDER',9608,'MOMO',350000.00,'VND','PAID','SEED-SO-9608-MOMO','2025-09-27 13:10:00','2025-09-27 06:10:00','2025-09-27 06:10:00'),(9809,9101,'SERVICE_ORDER',9609,'COD',700000.00,'VND','PAID','SEED-SO-9609-COD','2025-09-28 10:00:00','2025-09-28 03:00:00','2025-09-28 03:00:00'),(9810,9102,'SERVICE_ORDER',9610,'VNPAY',800000.00,'VND','PAID','SEED-SO-9610-VNPAY','2025-09-20 12:00:00','2025-09-20 05:00:00','2025-09-20 05:00:00'),(9811,9102,'SERVICE_ORDER',9611,'STRIPE',360000.00,'VND','PENDING','SEED-SO-9611-STRIPE',NULL,'2025-09-18 08:00:00','2025-09-18 08:00:00'),(9812,9103,'SERVICE_ORDER',9612,'COD',600000.00,'VND','PENDING','SEED-SO-9612-COD',NULL,'2025-09-05 02:30:00','2025-09-05 02:30:00');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(220) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'job',
  `parent_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_slug` (`slug`),
  KEY `idx_service_parent` (`parent_id`),
  CONSTRAINT `fk_service_parent` FOREIGN KEY (`parent_id`) REFERENCES `service` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9406 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service`
--

LOCK TABLES `service` WRITE;
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
INSERT INTO `service` VALUES (1,'Vệ sinh nhà','ve-sinh-nha','Dịch vụ vệ sinh tổng hợp','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(2,'Giặt sofa','giat-sofa','Giặt sofa, ghế','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(3,'Sửa điện','sua-dien','Sửa chữa điện dân dụng','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(4,'Sửa ống nước','sua-ong-nuoc','Xử lý rò rỉ, tắc nghẽn','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(5,'Sơn nhà','son-nha','Sơn sửa nội ngoại thất','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(6,'Chống thấm','chong-tham','Chống thấm nhà ở','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(7,'Vệ sinh điều hòa','ve-sinh-dieu-hoa','Vệ sinh bảo dưỡng điều hòa','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(8,'Lắp đặt đèn','lap-den','Lắp đặt thay thế đèn','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(9,'Bảo trì tòa nhà','bao-tri-toa-nha','Gói bảo trì định kỳ','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(10,'Vệ sinh cây cảnh','ve-sinh-cay-canh','Vệ sinh cây cảnh trong nhà','2h',31,'2025-09-24 21:21:54.894476','2025-09-24 23:29:45.941415'),(11,'Giặt thảm','giat-tham','Giặt thảm gia đình','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(12,'Vệ sinh kính','ve-sinh-kinh','Lau kính trong ngoài','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(13,'Dọn nhà theo giờ','don-nha-theo-gio','Dịch vụ dọn theo giờ','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(15,'Phun khử khuẩn','phun-khu-khuan','Khử khuẩn không gian','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(16,'Diệt côn trùng','diet-con-trung','Kiểm soát côn trùng','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(17,'Lắp quạt trần','lap-quat-tran','Lắp đặt quạt trần','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(18,'Lắp ổ cắm công tắc','lap-o-cam-cong-tac','Thêm ổ cắm, công tắc','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(19,'Kiểm tra chập điện','kiem-tra-chap-dien','Tìm nguyên nhân chập','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(20,'Nạp gas điều hòa','nap-gas-dieu-hoa','Nạp bổ sung gas','job',7,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(21,'Sửa điều hòa','sua-dieu-hoa','Sửa chữa điều hòa','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(22,'Thông tắc bồn cầu','thong-tac-bon-cau','Thông tắc nhanh','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(23,'Thông tắc chậu rửa','thong-tac-chau-rua','Xử lý tắc nghẽn','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(24,'Lắp thiết bị vệ sinh','lap-thiet-bi-ve-sinh','Lavabo, bồn cầu, sen','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(25,'Lắp đặt bình nóng lạnh','lap-dat-binh-nong-lanh','Treo, đấu điện nước','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(26,'Lắp máy bơm nước','lap-may-bom-nuoc','Đấu nối và vận hành','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(27,'Sơn chống thấm tường','son-chong-tham-tuong','Sơn chống thấm tường','job',6,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(28,'Sơn cửa gỗ','son-cua-go','Sơn PU cơ bản','job',5,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(29,'Bả matit tường','ba-matit-tuong','Xử lý bề mặt trước sơn','job',5,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(30,'Vệ sinh bể nước','ve-sinh-be-nuoc','Vệ sinh bể ngầm/bồn','job',9,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(31,'Chăm sóc cây cảnh','cham-soc-cay-canh','Chăm sóc cắt tỉa, tưới nước, bón phân cho cây','2h',NULL,'2025-09-24 23:27:57.251764','2025-09-24 23:27:57.251764'),(32,'Cắt tỉa cây thông','cat-tia-cay-thong','Cắt tỉa cây thông Noel','2h',31,'2025-09-24 23:28:33.876682','2025-09-24 23:28:33.876682'),(9401,'Vệ sinh nhà','cleaning',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9402,'Vệ sinh điều hòa','ac-clean',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9403,'Sửa ống nước','plumbing',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9404,'Sửa điện','electric',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9405,'Giặt sofa','sofa',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000');
/*!40000 ALTER TABLE `service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_order`
--

DROP TABLE IF EXISTS `service_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_code` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total` decimal(12,2) NOT NULL,
  `coupon_id` bigint DEFAULT NULL,
  `contact_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_line` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` tinytext COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_so_code` (`order_code`),
  KEY `idx_so_customer` (`customer_id`),
  KEY `idx_so_vendor` (`vendor_id`),
  KEY `fk_so_coupon` (`coupon_id`),
  CONSTRAINT `fk_so_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_so_customer` FOREIGN KEY (`customer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_so_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `so_chk_money` CHECK (((`subtotal` >= 0) and (`discount_amount` >= 0) and (`total` >= 0)))
) ENGINE=InnoDB AUTO_INCREMENT=9613 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_order`
--

LOCK TABLES `service_order` WRITE;
/*!40000 ALTER TABLE `service_order` DISABLE KEYS */;
INSERT INTO `service_order` VALUES (1,'SO0001',1,1,'CONFIRMED',600000.00,60000.00,540000.00,6,'Nguyễn Văn A','0900000001','12 Lê Lợi','Dọn sáng','2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,'SO0002',2,2,'PENDING',350000.00,0.00,350000.00,NULL,'Trần Thị B','0900000002','34 Hai Bà Trưng','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(3,'SO0003',3,3,'COMPLETED',400000.00,40000.00,360000.00,6,'Lê Văn C','0900000003','56 Nguyễn Trãi','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,'SO0004',4,4,'PENDING',1500000.00,0.00,1500000.00,NULL,'Phạm Thị D','0900000004','78 Trần Hưng Đạo','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,'SO0005',5,5,'PENDING',1800000.00,100000.00,1700000.00,7,'Hoàng Văn E','0900000005','90 Giảng Võ','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,'SO0006',6,6,'CONFIRMED',500000.00,25000.00,475000.00,8,'Đinh Thị F','0900000006','11 Cầu Giấy','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,'SO0007',7,7,'PENDING',250000.00,0.00,250000.00,NULL,'Bùi Văn G','0900000007','22 Tây Sơn','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,'SO0008',8,8,'PENDING',900000.00,0.00,900000.00,NULL,'Đỗ Thị H','0900000008','33 Kim Mã','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,'SO0009',9,9,'PENDING',5000000.00,500000.00,4500000.00,6,'Vũ Văn I','0900000009','44 Xã Đàn','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,'SO0010',10,10,'PENDING',3500000.00,525000.00,2975000.00,10,'Phan Thị K','0900000010','55 Phạm Hùng','','2025-09-20 05:24:29','2025-09-20 05:24:29'),(11,'SO-V15-0001',19,15,'CANCELLED',650000.00,65000.00,585000.00,11,'Nguyễn Văn A','0900000001','12 Lê Lợi, Hà Nội','Dọn trước 9h','2025-09-21 01:15:00','2025-09-22 15:49:15'),(12,'SO-V15-0002',20,15,'COMPLETED',250000.00,0.00,250000.00,NULL,'Trần Thị B','0900000002','34 Hai Bà Trưng, Hà Nội','Ống nước rò nhỏ','2025-09-20 03:05:00','2025-09-22 15:49:15'),(13,'SO-V15-0003',19,15,'PENDING',400000.00,50000.00,350000.00,12,'Nguyễn Văn A','0900000001','12 Lê Lợi, Hà Nội','Thi công chiều','2025-09-22 04:20:00','2025-09-22 15:49:15'),(9601,'SEED-SO-9601',9101,9001,'COMPLETED',500000.00,0.00,500000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-22 02:00:00','2025-09-22 02:00:00'),(9602,'SEED-SO-9602',9102,9002,'COMPLETED',350000.00,0.00,350000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-23 03:30:00','2025-09-23 03:30:00'),(9603,'SEED-SO-9603',9103,9003,'CONFIRMED',700000.00,0.00,700000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-24 07:15:00','2025-09-24 07:15:00'),(9604,'SEED-SO-9604',9101,9001,'CANCELLED',600000.00,0.00,600000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-24 09:00:00','2025-09-24 09:00:00'),(9605,'SEED-SO-9605',9102,9004,'COMPLETED',800000.00,50000.00,750000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-25 04:45:00','2025-09-25 04:45:00'),(9606,'SEED-SO-9606',9101,9002,'COMPLETED',900000.00,0.00,900000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-26 02:20:00','2025-09-26 02:20:00'),(9607,'SEED-SO-9607',9102,9005,'IN_PROGRESS',650000.00,0.00,650000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-27 01:05:00','2025-09-27 01:05:00'),(9608,'SEED-SO-9608',9103,9002,'COMPLETED',350000.00,0.00,350000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-27 06:10:00','2025-09-27 06:10:00'),(9609,'SEED-SO-9609',9101,9004,'COMPLETED',800000.00,100000.00,700000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-28 03:00:00','2025-09-28 03:00:00'),(9610,'SEED-SO-9610',9102,9004,'COMPLETED',800000.00,0.00,800000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-20 05:00:00','2025-09-20 05:00:00'),(9611,'SEED-SO-9611',9102,9005,'PENDING',360000.00,0.00,360000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-18 08:00:00','2025-09-18 08:00:00'),(9612,'SEED-SO-9612',9103,9001,'CONFIRMED',600000.00,0.00,600000.00,NULL,'Khách seed','0901234567','Hà Nội','','2025-09-05 02:30:00','2025-09-05 02:30:00');
/*!40000 ALTER TABLE `service_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_order_item`
--

DROP TABLE IF EXISTS `service_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_order_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  `vendor_service_id` bigint NOT NULL,
  `scheduled_at` datetime NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(12,2) NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_soi_order` (`service_order_id`),
  KEY `idx_soi_vs` (`vendor_service_id`),
  KEY `idx_soi_vendor` (`vendor_id`),
  CONSTRAINT `fk_soi_order` FOREIGN KEY (`service_order_id`) REFERENCES `service_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_soi_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_soi_vs` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `service_order_item_chk_1` CHECK ((`quantity` > 0)),
  CONSTRAINT `soi_chk_unit_price` CHECK ((`unit_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=9713 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_order_item`
--

LOCK TABLES `service_order_item` WRITE;
/*!40000 ALTER TABLE `service_order_item` DISABLE KEYS */;
INSERT INTO `service_order_item` VALUES (1,1,1,1,'2025-09-21 09:00:00',1,600000.00,600000.00),(2,2,2,2,'2025-09-22 10:00:00',1,350000.00,350000.00),(3,3,3,3,'2025-09-23 14:00:00',1,400000.00,400000.00),(4,4,4,4,'2025-09-24 08:00:00',1,1500000.00,1500000.00),(5,5,5,5,'2025-09-25 13:30:00',1,1800000.00,1800000.00),(6,6,6,6,'2025-09-26 09:00:00',1,500000.00,500000.00),(7,7,7,7,'2025-09-27 10:00:00',1,250000.00,250000.00),(8,8,8,8,'2025-09-28 15:00:00',1,900000.00,900000.00),(9,9,9,9,'2025-09-29 08:00:00',1,5000000.00,5000000.00),(10,10,10,10,'2025-09-30 09:00:00',1,3500000.00,3500000.00),(11,11,15,11,'2025-09-21 09:00:00',1,350000.00,350000.00),(12,11,15,12,'2025-09-21 11:00:00',1,300000.00,300000.00),(13,12,15,13,'2025-09-20 14:00:00',1,250000.00,250000.00),(14,13,15,14,'2025-09-23 15:30:00',2,200000.00,400000.00),(9701,9601,9001,9501,'2025-09-22 17:00:00',1,500000.00,500000.00),(9702,9602,9002,9502,'2025-09-23 17:00:00',1,350000.00,350000.00),(9703,9603,9003,9503,'2025-09-24 18:00:00',1,700000.00,700000.00),(9704,9604,9001,9506,'2025-09-24 20:00:00',1,600000.00,600000.00),(9705,9605,9004,9504,'2025-09-25 19:00:00',1,800000.00,800000.00),(9706,9606,9002,9507,'2025-09-26 17:30:00',2,450000.00,900000.00),(9707,9607,9005,9505,'2025-09-27 18:00:00',1,650000.00,650000.00),(9708,9608,9002,9502,'2025-09-27 19:00:00',1,350000.00,350000.00),(9709,9609,9004,9504,'2025-09-28 16:30:00',1,800000.00,800000.00),(9710,9610,9004,9504,'2025-09-20 19:00:00',1,800000.00,800000.00),(9711,9611,9005,9508,'2025-09-18 18:00:00',1,360000.00,360000.00),(9712,9612,9001,9506,'2025-09-05 17:00:00',1,600000.00,600000.00);
/*!40000 ALTER TABLE `service_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_CUSTOMER','ROLE_VENDOR') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ROLE_CUSTOMER',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'vendor01','$2a$10$hash','ROLE_VENDOR','vendor01@example.com','0900000001',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,'vendor02','$2a$10$hash','ROLE_VENDOR','vendor02@example.com','0900000002',NULL,1,'2025-09-20 05:24:29','2025-09-22 02:54:18'),(3,'vendor03','$2a$10$hash','ROLE_VENDOR','vendor03@example.com','0900000003',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,'vendor04','$2a$10$hash','ROLE_VENDOR','vendor04@example.com','0900000004',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,'vendor05','$2a$10$hash','ROLE_VENDOR','vendor05@example.com','0900000005',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,'vendor06','$2a$10$hash','ROLE_VENDOR','vendor06@example.com','0900000006',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,'vendor07','$2a$10$hash','ROLE_VENDOR','vendor07@example.com','0900000007',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,'vendor08','$2a$10$hash','ROLE_VENDOR','vendor08@example.com','0900000008',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,'vendor09','$2a$10$hash','ROLE_VENDOR','vendor09@example.com','0900000009',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,'vendor10','$2a$10$hash','ROLE_VENDOR','vendor10@example.com','0900000010',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(13,'admin','$2a$10$j672HpXtoT/G4zLg9f.58uDokC.k8gxdc0LcBtXHRTLP/GOluddvi','ROLE_ADMIN','admin@gmail.com','0123456789',NULL,1,'2025-09-20 07:56:34','2025-09-21 08:15:07'),(14,'TranViet0908','$2a$10$7BcXb7eemLMJqq9NUkTnxOTb/IvammDnBF4D9aT1xQzt/Dlopxniu','ROLE_CUSTOMER','viettun0908@gmail.com','0397915683','/uploads/avatars/f306f34afab54eca95c2ca0294a76e88.png',1,'2025-09-21 10:05:19','2025-09-22 03:21:13'),(15,'viettun0908','$2a$10$NKCD5l/bwy5kZE468cPDXe7SwJuy4tYS6hAjpaZ/uDhE2tGTujMVy','ROLE_VENDOR','tranviet2004v@gmail.com','0999999999','/uploads/avatars/be79aded539b47c1bd29a423ac647814.png',1,'2025-09-21 10:07:39','2025-10-01 18:02:57'),(17,'TestUsername','$2a$10$.LaCEzHSFkXs9KrPUdBz/.9UZJOPFqFKyoiL/H2LQXXG26h.E7mnu','ROLE_CUSTOMER','1@gmail.com','0147852369','/uploads/avatars/73f3440b842b4f42b07626211975b995.png',1,'2025-09-22 03:06:30','2025-09-22 03:21:26'),(18,'a','$2a$10$DyhgxiuA0E/XfBQdsTz4gOuorFlTBzjumlpd0cYTMH4kx..BMtw5y','ROLE_CUSTOMER','2@gmail.com','0123654789','/uploads/avatars/2b134ea813bc4cf8a80865c146e892be.png',1,'2025-09-22 03:23:40','2025-09-22 03:23:40'),(19,'cust_anhkhoa','$2a$10$r8nNf2qk2eQ7zF4z5u9rAeG0V9x9o2l5vF6G2j3T1u9b0Lw1xYtmi','ROLE_CUSTOMER','anhkhoa@example.com','0900000001',NULL,1,'2025-09-22 15:49:15','2025-09-22 15:49:15'),(20,'cust_minhchau','$2a$10$r8nNf2qk2eQ7zF4z5u9rAeG0V9x9o2l5vF6G2j3T1u9b0Lw1xYtmi','ROLE_CUSTOMER','minhchau@example.com','0900000002',NULL,1,'2025-09-22 15:49:15','2025-09-22 15:49:15'),(999,'guest','$2a$10$7BcXb7eemLMJqq9NUkTnxOTb/IvammDnBF4D9aT1xQzt/Dlopxniu','ROLE_CUSTOMER','guest@example.com',NULL,NULL,1,'2025-09-29 03:32:29','2025-09-29 03:32:29'),(9001,'seed_vendor1','{noop}123456','ROLE_VENDOR','vendor1@example.com','0900001000',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9002,'seed_vendor2','{noop}123456','ROLE_VENDOR','vendor2@example.com','0900001001',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9003,'seed_vendor3','{noop}123456','ROLE_VENDOR','vendor3@example.com','0900001002',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9004,'seed_vendor4','{noop}123456','ROLE_VENDOR','vendor4@example.com','0900001003',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9005,'seed_vendor5','{noop}123456','ROLE_VENDOR','vendor5@example.com','0900001004',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9101,'seed_cust1','{noop}123456','ROLE_CUSTOMER','cust1@example.com','0900001005',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9102,'seed_cust2','{noop}123456','ROLE_CUSTOMER','cust2@example.com','0900001006',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9103,'seed_cust3','{noop}123456','ROLE_CUSTOMER','cust3@example.com','0900001007',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_applications`
--

DROP TABLE IF EXISTS `vendor_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `display_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `experience_years` int DEFAULT '0',
  `note` longtext COLLATE utf8mb4_unicode_ci,
  `status` enum('APPROVED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_va_user_status` (`user_id`,`status`),
  KEY `idx_va_user` (`user_id`),
  KEY `idx_va_status` (`status`),
  CONSTRAINT `fk_va_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_applications`
--

LOCK TABLES `vendor_applications` WRITE;
/*!40000 ALTER TABLE `vendor_applications` DISABLE KEYS */;
INSERT INTO `vendor_applications` VALUES (4,15,'NamTun','Trần Hải Tũn','tranviet2004v@gmail.com','0999999999','Thôn phố thú y - Đức Thượng - Hoài Đức - Hà Nội','Hà Nội',5,'Im a best cleaner in VBB','APPROVED','2025-09-21 09:10:50','2025-09-21 09:31:56');
/*!40000 ALTER TABLE `vendor_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_profile`
--

DROP TABLE IF EXISTS `vendor_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `display_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `legal_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` tinytext COLLATE utf8mb4_unicode_ci,
  `years_experience` int DEFAULT NULL,
  `rating_avg` decimal(3,2) DEFAULT '0.00',
  `rating_count` int DEFAULT '0',
  `address_line` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `verified` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_profile_user` (`user_id`),
  CONSTRAINT `fk_vendor_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `vendor_profile_chk_1` CHECK (((`rating_avg` >= 0.00) and (`rating_avg` <= 5.00)))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_profile`
--

LOCK TABLES `vendor_profile` WRITE;
/*!40000 ALTER TABLE `vendor_profile` DISABLE KEYS */;
INSERT INTO `vendor_profile` VALUES (1,1,'Vệ Sinh A','Anh A','Chuyên vệ sinh nhà.',5,4.80,120,'Hà Nội',1,'2025-09-20 05:24:29','2025-09-21 22:11:46'),(2,2,'Điện Lạnh B','Anh B','Sửa điều hòa.',6,4.60,98,'Hà Nội',1,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(3,3,'Thợ Nước C','Anh C','Sửa ống nước.',4,4.50,77,'Hà Nội',0,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(4,4,'Sơn Nhà D','Anh D','Sơn sửa nhà.',8,4.70,150,'Hà Nội',1,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(5,5,'Chống Thấm E','Anh E','Chống thấm, xử lý nấm mốc.',7,4.40,63,'Hà Nội',0,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(6,6,'Giặt Thảm F','Anh F','Giặt thảm, sofa.',3,4.30,41,'Hà Nội',0,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(7,7,'Điện G','Anh G','Sửa điện dân dụng.',9,4.90,210,'Hà Nội',1,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(8,8,'Lắp Đặt H','Anh H','Lắp đèn, quạt.',5,4.20,35,'Hà Nội',0,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(9,9,'Bảo Trì I','Anh I','Bảo trì tòa nhà.',10,4.60,88,'Hà Nội',1,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(10,10,'Vệ Sinh J','Anh J','Vệ sinh công nghiệp.',2,4.10,22,'Hà Nội',0,'2025-09-20 05:24:29','2025-09-22 05:12:35'),(12,15,'NamTun','Trần Hải Tũn','Im a best cleaner in VBB',5,0.00,0,'Thôn phố thú y - Đức Thượng - Hoài Đức - Hà Nội',1,'2025-09-21 09:31:56','2025-09-21 09:31:56'),(13,9001,'Bảo Trì I',NULL,NULL,NULL,4.50,10,NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(14,9002,'Vệ Sinh J',NULL,NULL,NULL,4.60,12,NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(15,9003,'Chống Thấm E',NULL,NULL,NULL,4.40,8,NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(16,9004,'Sơn Nhà D',NULL,NULL,NULL,4.70,15,NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(17,9005,'NamTun',NULL,NULL,NULL,4.30,6,NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21');
/*!40000 ALTER TABLE `vendor_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_review`
--

DROP TABLE IF EXISTS `vendor_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `service_order_id` bigint DEFAULT NULL,
  `rating` tinyint unsigned NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `hidden` tinyint(1) NOT NULL DEFAULT '0',
  `hidden_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hidden_by_admin_id` bigint DEFAULT NULL,
  `hidden_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_vendor_review_so` (`service_order_id`),
  KEY `fk_vr_vendor` (`vendor_id`),
  KEY `fk_vr_customer` (`customer_id`),
  CONSTRAINT `fk_vr_customer` FOREIGN KEY (`customer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vr_so` FOREIGN KEY (`service_order_id`) REFERENCES `service_order` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_vr_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `vendor_review_chk_1` CHECK (((`rating` is null) or (`rating` between 1 and 5))),
  CONSTRAINT `vendor_review_chk_2` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_review`
--

LOCK TABLES `vendor_review` WRITE;
/*!40000 ALTER TABLE `vendor_review` DISABLE KEYS */;
INSERT INTO `vendor_review` VALUES (1,1,1,1,5,'Rất tốt','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(2,2,2,2,4,'Ổn, đúng hẹn','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(3,3,3,3,5,'Xử lý nhanh','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(4,4,4,NULL,4,'Tư vấn kỹ','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(5,5,5,5,5,'Hiệu quả','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(6,6,6,NULL,5,'Chỉ comment không chấm điểm','2025-09-20 05:24:29','2025-09-26 08:30:14',0,NULL,NULL,NULL),(7,7,7,7,5,'Giá hợp lý','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(8,8,8,8,4,'Sẽ ủng hộ tiếp','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(9,9,9,9,5,'Rất chuyên nghiệp','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(10,10,10,10,4,'Ổn định','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(11,15,20,12,5,'Xử lý rò rỉ nhanh và gọn.','2025-09-20 12:00:00','2025-09-22 15:49:15',0,NULL,NULL,NULL),(12,15,14,NULL,5,'Dịch vụ tốt, đúng giờ và lịch sự.','2025-09-26 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(13,15,17,NULL,4,'Làm việc ổn, có thể cải thiện tốc độ.','2025-09-27 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(14,15,18,NULL,5,'Rất hài lòng, sẽ đặt lại lần sau.','2025-09-28 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(15,15,19,NULL,3,'Ổn nhưng còn thiếu dụng cụ chuyên dụng.','2025-09-29 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(16,15,9101,NULL,4,'Thái độ tốt, kết quả sạch sẽ.','2025-09-30 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `vendor_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_service`
--

DROP TABLE IF EXISTS `vendor_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` tinytext COLLATE utf8mb4_unicode_ci,
  `base_price` decimal(12,2) NOT NULL,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'job',
  `duration_minutes` int DEFAULT '60',
  `min_notice_hours` int DEFAULT '24',
  `max_daily_jobs` int DEFAULT '10',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_service_unique` (`vendor_id`,`service_id`,`title`),
  KEY `idx_vs_vendor` (`vendor_id`),
  KEY `idx_vs_service` (`service_id`),
  KEY `idx_vs_service_status` (`service_id`,`status`),
  KEY `idx_vs_vendor_status` (`vendor_id`,`status`),
  CONSTRAINT `fk_vs_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_vs_vendor_profile` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `vendor_service_chk_price` CHECK ((`base_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=9516 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_service`
--

LOCK TABLES `vendor_service` WRITE;
/*!40000 ALTER TABLE `vendor_service` DISABLE KEYS */;
INSERT INTO `vendor_service` VALUES (1,1,1,'Gói vệ sinh căn hộ 60m²','Dọn tổng vệ sinh',600000.00,'job',120,24,5,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,2,7,'Vệ sinh điều hòa treo tường','Tháo vệ sinh lắp lại',350000.00,'job',60,24,8,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(3,3,4,'Sửa ống nước rò rỉ','Kiểm tra, thay thế',400000.00,'job',90,24,6,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,4,5,'Sơn lại phòng 20m²','Sơn 2 lớp',1500000.00,'job',300,48,2,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,5,6,'Chống thấm ban công','Vật liệu gốc xi măng',1800000.00,'job',240,48,2,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,6,2,'Giặt sofa 3 chỗ','Giặt hơi nước',500000.00,'job',90,24,4,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,7,3,'Sửa điện ổ cắm, công tắc','Thay mới vật tư',250000.00,'job',60,24,10,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,8,8,'Lắp 5 đèn downlight','Đã gồm vật tư',900000.00,'job',120,24,5,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,9,9,'Bảo trì tòa nhà mini','Gói theo tháng',5000000.00,'job',480,72,1,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,10,10,'Vệ sinh nhà xưởng 100m²','Máy móc công nghiệp',3500000.00,'job',360,72,2,'ACTIVE',NULL,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(11,15,1,'Gói vệ sinh căn hộ 60m²','Dọn tổng vệ sinh căn hộ tiêu chuẩn 60m²',350000.00,'job',120,24,5,'HIDDEN','/uploads/vendor-services/15/11/b18922b5ae4a44898565b1f666b16fed.png','2025-09-22 15:41:34','2025-09-24 03:02:46'),(12,15,2,'Vệ sinh điều hòa treo tường','Tháo vệ sinh và lắp lại máy treo tường',300000.00,'job',60,24,6,'PAUSED','/uploads/vendor-services/15/12/53ae681212694a99a9c2eacca7ebf1ad.png','2025-09-22 15:41:34','2025-09-24 03:02:49'),(13,15,3,'Sửa ống nước rò rỉ','Xử lý rò rỉ, thay ron, siết khớp cơ bản',250000.00,'job',60,12,8,'ACTIVE','/uploads/vendor-services/15/13/6089089c07e146b7bd941f5ea1dd9f7f.jpg','2025-09-22 15:41:34','2025-09-22 15:41:34'),(14,15,7,'Sửa điện gia dụng cơ bản','Kiểm tra và sửa các thiết bị điện nhỏ',200000.00,'job',45,6,10,'ACTIVE','/uploads/vendor-services/15/14/0e02a9ced24d4b51a37775966788cd27.png','2025-09-22 15:41:34','2025-09-22 15:41:34'),(16,15,24,'Lắp vòi xịt','Lắp vòi xịt trong toilet cho mọi nhà vệ sinh ',65000.00,'job',60,24,10,'ACTIVE','/uploads/vendor-services/15/16/319f8645cc0348e2a221f8171d184fea.jpg','2025-09-24 03:04:08','2025-09-24 03:22:12'),(17,1,8,'Dịch vụ Lắp đèn ngủ','Lắp đèn ngủ cho phòng ngủ',150000.00,'job',30,24,10,'ACTIVE','/uploads/vendor_cover/1c9ca993913948328102f148121aabfc.png','2025-09-24 03:30:22','2025-09-24 03:30:22'),(9501,9001,9401,'Vệ sinh nhà trọn gói',NULL,500000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9502,9002,9402,'Vệ sinh điều hòa',NULL,350000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9503,9003,9403,'Sửa ống nước',NULL,700000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9504,9004,9405,'Giặt sofa',NULL,800000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9505,9005,9403,'Sửa ống nước nhanh',NULL,650000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9506,9001,9404,'Sửa điện gia dụng',NULL,600000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9507,9002,9401,'Vệ sinh nhà cơ bản',NULL,450000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9508,9005,9402,'Vệ sinh điều hòa tận nơi',NULL,360000.00,'job',60,4,10,'ACTIVE',NULL,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9509,15,16,'Dịch vụ diệt côn trùng','',250000.00,'job',60,24,10,'ACTIVE','/uploads/vendor-services/15/9509/e7ee2fb263ac414a81d1752aed3d19cd_erd_houseservice.png',NULL,'2025-09-30 09:28:49');
/*!40000 ALTER TABLE `vendor_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_service_media`
--

DROP TABLE IF EXISTS `vendor_service_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_service_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_service_id` bigint NOT NULL,
  `media_type` enum('IMAGE','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `alt_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_cover` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vsm_vs_url` (`vendor_service_id`,`url`),
  KEY `idx_vsm_vs_order` (`vendor_service_id`,`sort_order`),
  KEY `idx_vsm_vs_cover` (`vendor_service_id`,`is_cover`),
  CONSTRAINT `fk_vsm_vendor_service` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_service_media`
--

LOCK TABLES `vendor_service_media` WRITE;
/*!40000 ALTER TABLE `vendor_service_media` DISABLE KEYS */;
INSERT INTO `vendor_service_media` VALUES (1,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-01.jpg','Vệ sinh căn hộ ảnh 1',0,0,'2025-09-24 10:58:23','2025-09-24 12:48:26'),(2,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-02.jpg','Vệ sinh căn hộ ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(3,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-03.jpg','Vệ sinh căn hộ ảnh 3',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(4,11,'VIDEO','/uploads/vendor-services/15/11/intro-11-01.mp4','Video giới thiệu 11',3,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(5,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-04.jpg','Vệ sinh căn hộ ảnh 4',4,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(6,12,'IMAGE','/uploads/vendor-services/15/12/ac-12-01.jpg','Vệ sinh điều hòa ảnh 1',0,0,'2025-09-24 10:58:23','2025-09-24 13:23:05'),(7,12,'IMAGE','/uploads/vendor-services/15/12/ac-12-02.jpg','Vệ sinh điều hòa ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(8,12,'VIDEO','/uploads/vendor-services/15/12/ac-12-clip.mp4','Video điều hòa 12',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(9,12,'IMAGE','/uploads/vendor-services/15/12/ac-12-03.jpg','Vệ sinh điều hòa ảnh 3',3,1,'2025-09-24 10:58:23','2025-09-24 13:23:06'),(10,13,'IMAGE','/uploads/vendor-services/15/13/plumbing-13-01.jpg','Sửa ống nước ảnh 1',0,0,'2025-09-24 10:58:23','2025-09-24 13:35:39'),(11,13,'IMAGE','/uploads/vendor-services/15/13/plumbing-13-02.jpg','Sửa ống nước ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(12,13,'IMAGE','/uploads/vendor-services/15/13/plumbing-13-03.jpg','Sửa ống nước ảnh 3',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(13,14,'IMAGE','/uploads/vendor-services/15/14/paint-14-01.jpg','Sơn phòng ảnh 1',0,1,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(14,14,'IMAGE','/uploads/vendor-services/15/14/paint-14-02.jpg','Sơn phòng ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(15,14,'VIDEO','/uploads/vendor-services/15/14/paint-14-clip.mp4','Video sơn 14',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(16,14,'IMAGE','/uploads/vendor-services/15/14/paint-14-03.jpg','Sơn phòng ảnh 3',3,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(21,1,'IMAGE','/uploads/vendor-services/1/1/home-1-01.jpg','Ảnh dịch vụ 1 - 01',0,1,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(22,1,'IMAGE','/uploads/vendor-services/1/1/home-1-02.jpg','Ảnh dịch vụ 1 - 02',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(23,2,'IMAGE','/uploads/vendor-services/2/2/ac-2-01.jpg','Ảnh dịch vụ 2 - 01',0,1,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(24,1,'IMAGE','/uploads/vendor-services/1/1/0f9d659722b6452892b62e9ba4fc8a40.jpg',NULL,2,0,'2025-09-24 13:12:08','2025-09-24 13:12:08'),(26,11,'IMAGE','/uploads/vendor-services/15/11/b18922b5ae4a44898565b1f666b16fed.png',NULL,5,1,'2025-09-24 13:34:18','2025-09-24 13:34:18'),(27,12,'IMAGE','/uploads/vendor-services/15/12/32d594d8c316492ca77e6fdff5442f79.png',NULL,4,0,'2025-09-24 13:34:26','2025-09-24 13:34:26'),(28,13,'IMAGE','/uploads/vendor-services/15/13/6089089c07e146b7bd941f5ea1dd9f7f.jpg',NULL,3,1,'2025-09-24 13:34:37','2025-09-24 13:35:39'),(29,14,'IMAGE','/uploads/vendor-services/15/14/022b2df0dcfc4f359fb6133d4398e88d.png',NULL,4,0,'2025-09-24 13:34:44','2025-09-24 13:34:44'),(31,16,'IMAGE','/uploads/vendor-services/15/16/319f8645cc0348e2a221f8171d184fea.jpg','',0,1,'2025-09-24 13:35:05','2025-09-30 22:37:31'),(32,9509,'IMAGE','/uploads/vendor-services/15/9509/e7ee2fb263ac414a81d1752aed3d19cd_erd_houseservice.png','cây',1,1,'2025-09-30 22:52:40','2025-09-30 22:53:20'),(33,9509,'VIDEO','/uploads/vendor-services/15/9509/5d609121cf60461192dee249fcc21b99_y2mate.com_-_static_tv_screen_transition_effect_720p.mp4','Screen',2,0,'2025-09-30 22:53:11','2025-09-30 22:53:11');
/*!40000 ALTER TABLE `vendor_service_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_service_review`
--

DROP TABLE IF EXISTS `vendor_service_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_service_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_service_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `service_order_item_id` bigint DEFAULT NULL,
  `rating` tinyint unsigned NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `hidden` tinyint(1) NOT NULL DEFAULT '0',
  `hidden_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hidden_by_admin_id` bigint DEFAULT NULL,
  `hidden_at` datetime DEFAULT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_service_review_item` (`service_order_item_id`),
  KEY `fk_vsr_vs` (`vendor_service_id`),
  KEY `fk_vsr_customer` (`customer_id`),
  CONSTRAINT `fk_vsr_customer` FOREIGN KEY (`customer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vsr_soi` FOREIGN KEY (`service_order_item_id`) REFERENCES `service_order_item` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_vsr_vs` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE CASCADE,
  CONSTRAINT `vendor_service_review_chk_1` CHECK (((`rating` is null) or (`rating` between 1 and 5))),
  CONSTRAINT `vendor_service_review_chk_2` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_service_review`
--

LOCK TABLES `vendor_service_review` WRITE;
/*!40000 ALTER TABLE `vendor_service_review` DISABLE KEYS */;
INSERT INTO `vendor_service_review` VALUES (1,1,1,1,5,'Sạch sẽ','2025-09-20 05:24:29','2025-09-26 08:56:11',0,NULL,NULL,NULL,12),(2,2,2,2,4,'Mát lạnh','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,1),(3,3,3,3,5,'Hết rò rỉ','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,2),(4,4,4,4,4,'Sơn đẹp','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,3),(5,5,5,5,5,'Chống thấm tốt','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,4),(6,6,6,6,5,'OK','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,5),(7,7,7,7,5,'Điện ổn','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,6),(8,8,8,8,4,'Lắp đẹp','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,7),(9,9,9,9,5,'Đúng mô tả','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,8),(10,10,10,10,4,'Ổn','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,9),(11,11,19,11,5,'Dọn sạch, đúng giờ.','2025-09-21 11:00:00','2025-09-26 08:53:45',0,NULL,NULL,NULL,10),(22,11,14,NULL,5,'Gói 1 làm kỹ, đáng tiền.','2025-09-26 16:51:31','2025-10-01 16:51:31',0,NULL,NULL,NULL,15),(23,12,17,NULL,4,'Vệ sinh ổn, hơi chậm khâu lắp.','2025-09-27 16:51:31','2025-10-01 16:51:31',0,NULL,NULL,NULL,15),(24,13,18,NULL,5,'Khử khuẩn nhanh, chuyên nghiệp.','2025-09-28 16:51:31','2025-10-01 16:51:31',0,NULL,NULL,NULL,15),(25,14,19,NULL,3,'Giặt sofa sạch nhưng hơi mùi.','2025-09-29 16:51:31','2025-10-01 16:51:31',0,NULL,NULL,NULL,15),(26,16,20,NULL,4,'Vệ sinh kính tốt.','2025-09-30 16:51:31','2025-10-01 16:51:31',0,NULL,NULL,NULL,15);
/*!40000 ALTER TABLE `vendor_service_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_skill`
--

DROP TABLE IF EXISTS `vendor_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_id` bigint NOT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_skill` (`vendor_id`,`slug`),
  CONSTRAINT `fk_vskill_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_skill`
--

LOCK TABLES `vendor_skill` WRITE;
/*!40000 ALTER TABLE `vendor_skill` DISABLE KEYS */;
INSERT INTO `vendor_skill` VALUES (1,1,'Vệ sinh nhà','ve-sinh-nha'),(2,2,'Bảo dưỡng điều hòa','bao-duong-dieu-hoa'),(3,3,'Sửa ống nước','sua-ong-nuoc'),(4,4,'Sơn tường','son-tuong'),(5,5,'Chống thấm sân thượng','chong-tham-san-thuong'),(6,6,'Giặt thảm','giat-tham'),(7,7,'Sửa điện ổ cắm','sua-dien-o-cam'),(8,8,'Lắp đèn downlight','lap-den-downlight'),(9,9,'Bảo trì hệ thống','bao-tri-he-thong'),(10,10,'Vệ sinh công nghiệp','ve-sinh-cong-nghiep'),(11,15,'Vệ sinh căn hộ','ve-sinh-can-ho'),(12,15,'Vệ sinh điều hòa','ve-sinh-dieu-hoa'),(13,15,'Thợ nước cơ bản','tho-nuoc-co-ban'),(14,15,'Sửa điện gia dụng','sua-dien-gia-dung'),(15,15,'Vệ sinh công nghiệp','ve-sinh-cong-nghiep');
/*!40000 ALTER TABLE `vendor_skill` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-02  1:18:32
