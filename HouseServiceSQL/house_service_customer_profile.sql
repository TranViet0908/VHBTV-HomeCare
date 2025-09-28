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
-- Table structure for table `customer_profile`
--

DROP TABLE IF EXISTS `customer_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_line` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_profile_user` (`user_id`),
  CONSTRAINT `fk_customer_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_profile`
--

LOCK TABLES `customer_profile` WRITE;
/*!40000 ALTER TABLE `customer_profile` DISABLE KEYS */;
INSERT INTO `customer_profile` VALUES (1,1,'Nguyễn Văn A','1995-01-01','MALE','12 Lê Lợi, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,2,'Trần Thị B','1996-02-02','FEMALE','34 Hai Bà Trưng, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(3,3,'Lê Văn C','1997-03-03','MALE','56 Nguyễn Trãi, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,4,'Phạm Thị D','1998-04-04','FEMALE','78 Trần Hưng Đạo, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,5,'Hoàng Văn E','1994-05-05','MALE','90 Giảng Võ, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,6,'Đinh Thị F','1993-06-06','FEMALE','11 Cầu Giấy, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,7,'Bùi Văn G','1992-07-07','MALE','22 Tây Sơn, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,8,'Đỗ Thị H','1991-08-08','FEMALE','33 Kim Mã, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,9,'Vũ Văn I','1990-09-09','MALE','44 Xã Đàn, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,10,'Phan Thị K','1989-10-10','FEMALE','55 Phạm Hùng, Hà Nội','2025-09-20 05:24:29','2025-09-20 05:24:29'),(11,14,'Trần Hải Việt','2004-08-09','MALE','Hanoi','2025-09-21 10:05:19','2025-09-21 10:05:19'),(12,15,'Trần Hải Tũn','2001-09-11','MALE','America','2025-09-21 10:07:39','2025-09-21 10:07:39'),(13,18,'Kwon Ji Yong','2002-02-20','MALE','Hanoi','2025-09-22 03:23:40','2025-09-22 03:23:40'),(14,9101,'Nguyễn An',NULL,NULL,'Hà Nội','2025-09-27 17:51:21','2025-09-27 17:51:21'),(15,9102,'Trần Bình',NULL,NULL,'Hà Nội','2025-09-27 17:51:21','2025-09-27 17:51:21'),(16,9103,'Lê Cường',NULL,NULL,'Hà Nội','2025-09-27 17:51:21','2025-09-27 17:51:21');
/*!40000 ALTER TABLE `customer_profile` ENABLE KEYS */;
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
