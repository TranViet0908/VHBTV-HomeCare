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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-02  1:18:16
