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
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('ORDER','PAYMENT','WISHLIST','PROMOTION') NOT NULL,
  `title` varchar(200) NOT NULL,
  `message` text NOT NULL,
  `related_type` enum('SERVICE_ORDER','PAYMENT','VENDOR_SERVICE','COUPON') DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `data_json` json DEFAULT NULL,
  `actor_user_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_notification_actor_user` (`actor_user_id`),
  KEY `idx_notification_type_created` (`type`,`created_at`),
  KEY `idx_notification_rel` (`related_type`,`related_id`),
  CONSTRAINT `fk_notification_actor_user` FOREIGN KEY (`actor_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=367 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,'ORDER','Đơn #1001 cập nhật','Đơn hàng của bạn đã chuyển sang trạng thái PROCESSING','SERVICE_ORDER',1001,NULL,NULL,'2025-10-20 01:16:33.609625'),(2,'PAYMENT','Thanh toán thành công','Bạn đã thanh toán đơn #1001','PAYMENT',501,NULL,NULL,'2025-10-20 01:16:33.609625'),(3,'WISHLIST','Dịch vụ giảm giá','Dịch vụ bạn theo dõi vừa giảm 10%','VENDOR_SERVICE',3001,NULL,NULL,'2025-10-20 01:16:33.609625'),(4,'PROMOTION','Tặng mã khuyến mại','Nhận mã NEWCUST -10% cho mọi dịch vụ','COUPON',7001,NULL,NULL,'2025-10-20 01:16:33.609625'),(357,'ORDER','Đơn #9811 cập nhật','Đơn hàng của bạn đã chuyển sang trạng thái PENDING','SERVICE_ORDER',9811,NULL,NULL,'2025-10-20 15:13:09.131086'),(358,'ORDER','Đơn #9812 cập nhật','Đơn hàng của bạn đã chuyển sang trạng thái PENDING','SERVICE_ORDER',9812,NULL,NULL,'2025-10-20 15:13:09.180446'),(359,'PAYMENT','Thanh toán thành công','Bạn đã thanh toán đơn #9811','PAYMENT',9837,NULL,NULL,'2025-10-20 15:13:39.262773'),(361,'ORDER','Đơn #9813 cập nhật','Đơn hàng của bạn đã chuyển sang trạng thái PENDING','SERVICE_ORDER',9813,NULL,NULL,'2025-10-20 15:22:43.571501'),(362,'PAYMENT','Thanh toán thành công','Bạn đã thanh toán đơn #9813','PAYMENT',9838,NULL,NULL,'2025-10-20 15:23:13.659269'),(363,'ORDER','Đơn #9820 cập nhật','Đơn hàng của bạn đã chuyển sang trạng thái PENDING','SERVICE_ORDER',9820,NULL,NULL,'2025-10-20 15:35:27.596202'),(364,'PAYMENT','Thanh toán thành công','Bạn đã thanh toán đơn #9820','PAYMENT',9843,NULL,NULL,'2025-10-20 15:36:57.796980'),(365,'ORDER','Đơn #9821 cập nhật','Đơn hàng của bạn đã chuyển sang trạng thái PENDING','SERVICE_ORDER',9821,NULL,NULL,'2025-10-20 15:52:57.408585'),(366,'PAYMENT','Thanh toán thành công','Bạn đã thanh toán đơn #9821','PAYMENT',9844,NULL,NULL,'2025-10-20 15:53:27.487053');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-20 17:51:46
