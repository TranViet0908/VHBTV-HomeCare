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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-28 11:53:51
