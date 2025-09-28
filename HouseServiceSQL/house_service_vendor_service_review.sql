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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_service_review`
--

LOCK TABLES `vendor_service_review` WRITE;
/*!40000 ALTER TABLE `vendor_service_review` DISABLE KEYS */;
INSERT INTO `vendor_service_review` VALUES (1,1,1,1,5,'Sạch sẽ','2025-09-20 05:24:29','2025-09-26 08:56:11',0,NULL,NULL,NULL,12),(2,2,2,2,4,'Mát lạnh','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,1),(3,3,3,3,5,'Hết rò rỉ','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,2),(4,4,4,4,4,'Sơn đẹp','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,3),(5,5,5,5,5,'Chống thấm tốt','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,4),(6,6,6,6,5,'OK','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,5),(7,7,7,7,5,'Điện ổn','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,6),(8,8,8,8,4,'Lắp đẹp','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,7),(9,9,9,9,5,'Đúng mô tả','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,8),(10,10,10,10,4,'Ổn','2025-09-20 05:24:29','2025-09-26 08:53:45',0,NULL,NULL,NULL,9),(11,11,19,11,5,'Dọn sạch, đúng giờ.','2025-09-21 11:00:00','2025-09-26 08:53:45',0,NULL,NULL,NULL,10);
/*!40000 ALTER TABLE `vendor_service_review` ENABLE KEYS */;
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
