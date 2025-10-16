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
-- Table structure for table `vendor_service_media`
--

DROP TABLE IF EXISTS `vendor_service_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_service_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_service_id` bigint NOT NULL,
  `media_type` enum('IMAGE','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `alt_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_cover` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vsm_vs_url` (`vendor_service_id`,`url`),
  KEY `idx_vsm_vs_order` (`vendor_service_id`,`sort_order`),
  KEY `idx_vsm_vs_cover` (`vendor_service_id`,`is_cover`),
  CONSTRAINT `fk_vsm_vendor_service` FOREIGN KEY (`vendor_service_id`) REFERENCES `vendor_service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_service_media`
--

LOCK TABLES `vendor_service_media` WRITE;
/*!40000 ALTER TABLE `vendor_service_media` DISABLE KEYS */;
INSERT INTO `vendor_service_media` VALUES (1,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-01.jpg','Vệ sinh căn hộ ảnh 1',0,0,'2025-09-24 10:58:23','2025-09-24 12:48:26'),(2,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-02.jpg','Vệ sinh căn hộ ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(3,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-03.jpg','Vệ sinh căn hộ ảnh 3',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(4,11,'VIDEO','/uploads/vendor-services/15/11/intro-11-01.mp4','Video giới thiệu 11',3,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(5,11,'IMAGE','/uploads/vendor-services/15/11/cleaning-11-04.jpg','Vệ sinh căn hộ ảnh 4',4,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(6,12,'IMAGE','/uploads/vendor-services/15/12/ac-12-01.jpg','Vệ sinh điều hòa ảnh 1',0,0,'2025-09-24 10:58:23','2025-09-24 13:23:05'),(7,12,'IMAGE','/uploads/vendor-services/15/12/ac-12-02.jpg','Vệ sinh điều hòa ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(8,12,'VIDEO','/uploads/vendor-services/15/12/ac-12-clip.mp4','Video điều hòa 12',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(9,12,'IMAGE','/uploads/vendor-services/15/12/ac-12-03.jpg','Vệ sinh điều hòa ảnh 3',3,1,'2025-09-24 10:58:23','2025-09-24 13:23:06'),(10,13,'IMAGE','/uploads/vendor-services/15/13/plumbing-13-01.jpg','Sửa ống nước ảnh 1',0,0,'2025-09-24 10:58:23','2025-09-24 13:35:39'),(11,13,'IMAGE','/uploads/vendor-services/15/13/plumbing-13-02.jpg','Sửa ống nước ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(12,13,'IMAGE','/uploads/vendor-services/15/13/plumbing-13-03.jpg','Sửa ống nước ảnh 3',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(13,14,'IMAGE','/uploads/vendor-services/15/14/paint-14-01.jpg','Sơn phòng ảnh 1',0,1,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(14,14,'IMAGE','/uploads/vendor-services/15/14/paint-14-02.jpg','Sơn phòng ảnh 2',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(15,14,'VIDEO','/uploads/vendor-services/15/14/paint-14-clip.mp4','Video sơn 14',2,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(16,14,'IMAGE','/uploads/vendor-services/15/14/paint-14-03.jpg','Sơn phòng ảnh 3',3,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(21,1,'IMAGE','/uploads/vendor-services/1/1/home-1-01.jpg','Ảnh dịch vụ 1 - 01',0,1,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(22,1,'IMAGE','/uploads/vendor-services/1/1/home-1-02.jpg','Ảnh dịch vụ 1 - 02',1,0,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(23,2,'IMAGE','/uploads/vendor-services/2/2/ac-2-01.jpg','Ảnh dịch vụ 2 - 01',0,1,'2025-09-24 10:58:23','2025-09-24 10:58:23'),(24,1,'IMAGE','/uploads/vendor-services/1/1/0f9d659722b6452892b62e9ba4fc8a40.jpg',NULL,2,0,'2025-09-24 13:12:08','2025-09-24 13:12:08'),(26,11,'IMAGE','/uploads/vendor-services/15/11/b18922b5ae4a44898565b1f666b16fed.png',NULL,5,1,'2025-09-24 13:34:18','2025-09-24 13:34:18'),(27,12,'IMAGE','/uploads/vendor-services/15/12/32d594d8c316492ca77e6fdff5442f79.png',NULL,4,0,'2025-09-24 13:34:26','2025-09-24 13:34:26'),(28,13,'IMAGE','/uploads/vendor-services/15/13/6089089c07e146b7bd941f5ea1dd9f7f.jpg',NULL,3,1,'2025-09-24 13:34:37','2025-09-24 13:35:39'),(29,14,'IMAGE','/uploads/vendor-services/15/14/022b2df0dcfc4f359fb6133d4398e88d.png',NULL,4,0,'2025-09-24 13:34:44','2025-09-24 13:34:44'),(31,16,'IMAGE','/uploads/vendor-services/15/16/319f8645cc0348e2a221f8171d184fea.jpg','',0,1,'2025-09-24 13:35:05','2025-09-30 22:37:31'),(32,9509,'IMAGE','/uploads/vendor-services/15/9509/e7ee2fb263ac414a81d1752aed3d19cd_erd_houseservice.png','cây',1,1,'2025-09-30 22:52:40','2025-09-30 22:53:20'),(33,9509,'VIDEO','/uploads/vendor-services/15/9509/5d609121cf60461192dee249fcc21b99_y2mate.com_-_static_tv_screen_transition_effect_720p.mp4','Screen',2,0,'2025-09-30 22:53:11','2025-09-30 22:53:11'),(34,16,'IMAGE','/uploads/vendor-services/15/16/fca76f505e30475a87ee11da3f10e797_gdlivecrooked.png','Anh 1',2,0,'2025-10-04 18:01:31','2025-10-04 18:01:31'),(35,16,'IMAGE','/uploads/vendor-services/15/16/ea84179ab72a406a9ec3a5f454d5859d_g-dragon-ubermensch.jpg','anh 2',3,0,'2025-10-04 18:01:40','2025-10-04 18:01:40'),(36,16,'IMAGE','/uploads/vendor-services/15/16/653be7a8fad54234bc446630ce76ec1e_gd_0to10.png','anh 3',4,0,'2025-10-04 18:01:49','2025-10-04 18:01:49'),(38,16,'IMAGE','/uploads/vendor-services/15/16/d41a9cdc59014a4dbe26920854edb7ba_gdtyds.png','Anh 4',5,0,'2025-10-04 22:49:35','2025-10-04 22:49:35');
/*!40000 ALTER TABLE `vendor_service_media` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-16 10:56:22
