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
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answer` mediumtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `question` text COLLATE utf8mb4_unicode_ci,
  `user_id` bigint DEFAULT NULL,
  `conversation_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cm_conv` (`conversation_id`),
  KEY `idx_cm_user` (`user_id`),
  CONSTRAINT `fk_cm_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
INSERT INTO `chat_message` VALUES (1,'Chào bạn, bạn cần vệ sinh khu vực nào?','2025-09-20 09:01:00.000001','Giá vệ sinh?',1,1),(2,'Bảo dưỡng gồm vệ sinh dàn lạnh.','2025-09-20 09:06:00.000001','Bảo dưỡng DH bao lâu?',2,2),(3,'Có thể qua trong ngày.','2025-09-20 09:11:00.000001','Ống nước rò bao lâu xong?',3,3),(4,'Sơn 2 lớp trong 5 giờ.','2025-09-20 09:16:00.000001','Mất bao lâu?',4,4),(5,'Bảo hành 6 tháng.','2025-09-20 09:21:00.000001','Chống thấm bảo hành?',5,5),(6,'Sofa 3 chỗ ~90 phút.','2025-09-20 09:26:00.000001','Giặt sofa lâu không?',6,6),(7,'Thay công tắc 30 phút.','2025-09-20 09:31:00.000001','Sửa ổ cắm?',7,7),(8,'Lắp 5 đèn trong 2 giờ.','2025-09-20 09:36:00.000001','Lắp đèn downlight?',8,8),(9,'Gói tháng gồm 2 lượt kiểm tra.','2025-09-20 09:41:00.000001','Bảo trì tòa nhà?',9,9),(10,'Vệ sinh xưởng tính theo m².','2025-09-20 09:46:00.000001','Giá tính sao?',10,10),(11,NULL,'2025-09-21 08:30:00.000000','Bên mình có thể đến sớm hơn 15 phút không?',19,11),(12,'Được, kỹ thuật sẽ có mặt 8:45.','2025-09-21 08:35:00.000000',NULL,15,11);
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
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
