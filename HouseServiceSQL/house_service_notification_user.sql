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
-- Table structure for table `notification_user`
--

DROP TABLE IF EXISTS `notification_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notification_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `read_at` datetime(6) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delivered_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_user` (`notification_id`,`user_id`),
  KEY `idx_notification_user_read` (`user_id`,`is_read`,`notification_id`),
  CONSTRAINT `fk_notification_user_notification` FOREIGN KEY (`notification_id`) REFERENCES `notification` (`id`),
  CONSTRAINT `fk_notification_user_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=370 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_user`
--

LOCK TABLES `notification_user` WRITE;
/*!40000 ALTER TABLE `notification_user` DISABLE KEYS */;
INSERT INTO `notification_user` VALUES (1,1,14,1,'2025-10-19 18:55:52.913501',1,'2025-10-20 01:16:33.624239'),(2,2,14,1,'2025-10-19 18:55:51.888064',0,'2025-10-20 01:16:33.624239'),(3,3,14,1,'2025-10-19 18:55:51.452259',0,'2025-10-20 01:16:33.624239'),(4,4,14,1,'2025-10-19 18:55:48.720451',0,'2025-10-20 01:16:33.624239'),(360,357,14,1,'2025-10-20 08:14:24.442127',1,'2025-10-20 08:13:09.149897'),(361,358,14,1,'2025-10-20 08:14:24.896876',1,'2025-10-20 08:13:09.186025'),(362,359,14,1,'2025-10-20 08:13:44.040770',1,'2025-10-20 08:13:39.267957'),(364,361,14,1,'2025-10-20 08:37:27.452802',0,'2025-10-20 08:22:43.577512'),(365,362,14,1,'2025-10-20 08:37:26.013478',0,'2025-10-20 08:23:13.661514'),(366,363,14,1,'2025-10-20 08:37:24.933818',0,'2025-10-20 08:35:27.610987'),(367,364,14,1,'2025-10-20 08:37:00.122005',0,'2025-10-20 08:36:57.800082'),(368,365,14,0,NULL,0,'2025-10-20 08:52:57.412477'),(369,366,14,0,NULL,0,'2025-10-20 08:53:27.490958');
/*!40000 ALTER TABLE `notification_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-20 17:51:47
