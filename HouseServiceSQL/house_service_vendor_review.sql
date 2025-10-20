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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_review`
--

LOCK TABLES `vendor_review` WRITE;
/*!40000 ALTER TABLE `vendor_review` DISABLE KEYS */;
INSERT INTO `vendor_review` VALUES (1,1,1,1,5,'Rất tốt','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(2,2,2,2,4,'Ổn, đúng hẹn','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(3,3,3,3,5,'Xử lý nhanh','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(4,4,4,NULL,4,'Tư vấn kỹ','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(5,5,5,5,5,'Hiệu quả','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(6,6,6,NULL,5,'Chỉ comment không chấm điểm','2025-09-20 05:24:29','2025-09-26 08:30:14',0,NULL,NULL,NULL),(7,7,7,7,5,'Giá hợp lý','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(8,8,8,8,4,'Sẽ ủng hộ tiếp','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(9,9,9,9,5,'Rất chuyên nghiệp','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(10,10,10,10,4,'Ổn định','2025-09-20 05:24:29','2025-09-20 05:24:29',0,NULL,NULL,NULL),(11,15,20,12,5,'Xử lý rò rỉ nhanh và gọn.','2025-09-20 12:00:00','2025-09-22 15:49:15',0,NULL,NULL,NULL),(12,15,14,NULL,5,'Dịch vụ tốt, đúng giờ và lịch sự.','2025-09-26 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(13,15,17,NULL,4,'Làm việc ổn, có thể cải thiện tốc độ.','2025-09-27 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(14,15,18,NULL,5,'Rất hài lòng, sẽ đặt lại lần sau.','2025-09-28 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(15,15,19,NULL,3,'Ổn nhưng còn thiếu dụng cụ chuyên dụng.','2025-09-29 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(16,15,9101,NULL,4,'Thái độ tốt, kết quả sạch sẽ.','2025-09-30 16:45:14','2025-10-01 16:45:14',0,NULL,NULL,NULL),(17,15,14,9800,5,'Tuyệt vời','2025-10-18 10:32:30','2025-10-18 10:32:30',0,NULL,NULL,NULL),(18,15,9104,9804,5,'Sạch sẽ, Gọn gàng','2025-10-18 11:13:00','2025-10-18 11:13:00',0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `vendor_review` ENABLE KEYS */;
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
