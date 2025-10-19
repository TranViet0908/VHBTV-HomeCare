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
-- Table structure for table `customer_wishlist`
--

DROP TABLE IF EXISTS `customer_wishlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_wishlist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `vendor_service_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wishlist_customer_vs` (`customer_id`,`vendor_service_id`),
  KEY `idx_wishlist_customer` (`customer_id`),
  KEY `idx_wishlist_vs` (`vendor_service_id`),
  CONSTRAINT `fk_wishlist_customer` FOREIGN KEY (`customer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wishlist_vs` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_wishlist`
--

LOCK TABLES `customer_wishlist` WRITE;
/*!40000 ALTER TABLE `customer_wishlist` DISABLE KEYS */;
INSERT INTO `customer_wishlist` VALUES (1,14,1,'2025-10-03 03:00:00'),(2,17,2,'2025-10-03 03:01:00'),(3,18,3,'2025-10-03 03:02:00'),(4,19,4,'2025-10-03 03:03:00'),(5,20,5,'2025-10-03 03:04:00'),(6,999,6,'2025-10-03 03:05:00'),(7,9101,7,'2025-10-03 03:06:00'),(8,9102,8,'2025-10-03 03:07:00'),(9,9103,9,'2025-10-03 03:08:00'),(10,14,10,'2025-10-03 03:09:00'),(11,14,9578,'2025-10-16 10:13:16'),(12,14,9577,'2025-10-16 10:13:16'),(13,14,9575,'2025-10-16 10:13:16'),(14,14,9566,'2025-10-16 10:13:16'),(15,14,9565,'2025-10-16 10:13:16'),(16,14,9564,'2025-10-16 10:13:16'),(19,14,16,'2025-10-16 10:20:54');
/*!40000 ALTER TABLE `customer_wishlist` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-19 22:42:08
