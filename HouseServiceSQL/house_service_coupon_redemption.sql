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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-02  1:18:17
