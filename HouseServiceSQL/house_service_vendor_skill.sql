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
-- Table structure for table `vendor_skill`
--

DROP TABLE IF EXISTS `vendor_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_id` bigint NOT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_skill` (`vendor_id`,`slug`),
  CONSTRAINT `fk_vskill_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendor_profile` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_skill`
--

LOCK TABLES `vendor_skill` WRITE;
/*!40000 ALTER TABLE `vendor_skill` DISABLE KEYS */;
INSERT INTO `vendor_skill` VALUES (1,1,'Vệ sinh nhà','ve-sinh-nha'),(2,2,'Bảo dưỡng điều hòa','bao-duong-dieu-hoa'),(3,3,'Sửa ống nước','sua-ong-nuoc'),(4,4,'Sơn tường','son-tuong'),(5,5,'Chống thấm sân thượng','chong-tham-san-thuong'),(6,6,'Giặt thảm','giat-tham'),(7,7,'Sửa điện ổ cắm','sua-dien-o-cam'),(8,8,'Lắp đèn downlight','lap-den-downlight'),(9,9,'Bảo trì hệ thống','bao-tri-he-thong'),(10,10,'Vệ sinh công nghiệp','ve-sinh-cong-nghiep'),(11,15,'Vệ sinh căn hộ','ve-sinh-can-ho'),(12,15,'Vệ sinh điều hòa','ve-sinh-dieu-hoa'),(13,15,'Thợ nước cơ bản','tho-nuoc-co-ban'),(14,15,'Sửa điện gia dụng','sua-dien-gia-dung'),(15,15,'Vệ sinh công nghiệp','ve-sinh-cong-nghiep');
/*!40000 ALTER TABLE `vendor_skill` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-28 11:53:49
