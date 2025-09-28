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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (6,'SRV10','Giảm 10% dịch vụ','Coupon 10% cho dịch vụ','PERCENT',10.00,'SERVICE',NULL,NULL,200000.00,0,0,1,'2025-09-20 05:24:29','2025-09-26 01:36:00'),(7,'SRV100K','Giảm 100K dịch vụ','Coupon 100k cho dịch vụ','FIXED',100000.00,'SERVICE',NULL,NULL,0.00,0,0,0,'2025-09-20 05:24:29','2025-09-26 01:42:34'),(8,'SRV5','Giảm 5% dịch vụ','Coupon 5% cho dịch vụ','PERCENT',5.00,'SERVICE',NULL,NULL,0.00,0,0,0,'2025-09-20 05:24:29','2025-09-26 01:42:33'),(9,'SRV200K','Giảm 200K dịch vụ','Coupon 20% cho dịch vụ','FIXED',200000.00,'SERVICE',NULL,NULL,0.00,0,0,1,'2025-09-20 05:24:29','2025-09-25 16:03:53'),(10,'SV20','Giảm 15% dịch vụ','Coupon 15% cho dịch vụ','PERCENT',20.00,'SERVICE','2025-09-27 08:35:00','2025-09-30 08:35:00',500000.00,0,1,1,'2025-09-20 05:24:29','2025-09-26 01:35:55'),(11,'VHBTV10','Giảm 10% dịch vụ','Coupon 10% cho dịch vụ','PERCENT',10.00,'SERVICE','2025-09-01 00:00:00','2025-12-31 23:59:59',100000.00,0,0,1,'2025-09-22 15:49:15','2025-09-25 16:03:53'),(12,'CLEAN50K','Giảm 50K dịch vụ','Giảm cố định 50.000đ','FIXED',50000.00,'SERVICE','2025-09-01 00:00:00','2025-12-31 23:59:59',0.00,0,0,1,'2025-09-22 15:49:15','2025-09-22 15:49:15'),(13,'VIP50K','Giảm 50k đơn hàng dưới 20000k',NULL,'FIXED',50000.00,'SERVICE','2025-09-03 08:34:00','2025-10-05 08:34:00',50000.00,0,0,1,'2025-09-26 01:35:15','2025-09-26 01:35:15');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-28 11:53:51
