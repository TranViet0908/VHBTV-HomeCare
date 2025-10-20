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
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  `vendor_service_id` bigint NOT NULL,
  `schedule_at` datetime DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` bigint NOT NULL,
  `subtotal` bigint NOT NULL,
  `address_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_service` (`cart_id`,`vendor_service_id`),
  KEY `idx_ci_cart` (`cart_id`),
  KEY `idx_ci_vs` (`vendor_service_id`),
  KEY `idx_ci_vendor` (`vendor_id`),
  CONSTRAINT `fk_ci_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ci_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ci_vs` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ci_chk_money` CHECK (((`unit_price` >= 0) and (`subtotal` >= 0))),
  CONSTRAINT `ci_chk_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (2,2,3,3,'2025-10-07 09:00:00',2,400000,800000,'Ngõ 12 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:02:30','2025-10-03 16:07:34'),(3,3,4,4,'2025-10-08 09:00:00',1,1500000,1500000,'Ngõ 13 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:03:30','2025-10-03 16:07:34'),(4,3,5,5,'2025-10-08 10:00:00',1,1800000,1800000,'Ngõ 13 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:03:30','2025-10-03 16:07:34'),(5,4,5,5,'2025-10-09 09:00:00',2,1800000,3600000,'Ngõ 14 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:04:30','2025-10-03 16:07:34'),(6,5,6,6,'2025-10-10 09:00:00',1,500000,500000,'Ngõ 15 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:05:30','2025-10-03 16:07:34'),(7,6,7,7,'2025-10-11 09:00:00',2,250000,500000,'Ngõ 16 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:06:30','2025-10-03 16:07:34'),(8,6,8,8,'2025-10-11 10:00:00',2,900000,1800000,'Ngõ 16 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:06:30','2025-10-03 16:07:34'),(9,7,8,8,'2025-10-12 09:00:00',1,900000,900000,'Ngõ 17 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:07:30','2025-10-03 16:07:34'),(10,8,9,9,'2025-10-13 09:00:00',2,5000000,10000000,'Ngõ 18 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:08:30','2025-10-03 16:07:34'),(11,9,10,10,'2025-10-14 09:00:00',1,3500000,3500000,'Ngõ 19 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:09:30','2025-10-03 16:07:34'),(12,9,15,11,'2025-10-14 10:00:00',1,350000,350000,'Ngõ 19 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:09:30','2025-10-03 16:07:34'),(13,10,15,11,'2025-10-15 09:00:00',2,350000,700000,'Ngõ 20 Kim Mã, Ba Đình, Hà Nội',NULL,'2025-10-03 04:10:30','2025-10-03 16:07:34'),(92,11,15,13,'2025-10-18 17:52:00',2,250000,500000,NULL,NULL,'2025-10-18 10:53:00','2025-10-18 10:53:00'),(96,1,15,16,NULL,1,65000,65000,NULL,NULL,'2025-10-20 01:31:24','2025-10-20 01:31:24');
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-20 17:51:48
