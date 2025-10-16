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
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(220) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'job',
  `parent_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_slug` (`slug`),
  KEY `idx_service_parent` (`parent_id`),
  CONSTRAINT `fk_service_parent` FOREIGN KEY (`parent_id`) REFERENCES `service` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9420 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service`
--

LOCK TABLES `service` WRITE;
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
INSERT INTO `service` VALUES (1,'Vệ sinh nhà','ve-sinh-nha','Dịch vụ vệ sinh tổng hợp','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(2,'Giặt sofa','giat-sofa','Giặt sofa, ghế','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(3,'Sửa điện','sua-dien','Sửa chữa điện dân dụng','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(4,'Sửa ống nước','sua-ong-nuoc','Xử lý rò rỉ, tắc nghẽn','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(5,'Sơn nhà','son-nha','Sơn sửa nội ngoại thất','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(6,'Chống thấm','chong-tham','Chống thấm nhà ở','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(7,'Vệ sinh điều hòa','ve-sinh-dieu-hoa','Vệ sinh bảo dưỡng điều hòa','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(8,'Lắp đặt đèn','lap-den','Lắp đặt thay thế đèn','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(9,'Bảo trì tòa nhà','bao-tri-toa-nha','Gói bảo trì định kỳ','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(10,'Vệ sinh cây cảnh','ve-sinh-cay-canh','Vệ sinh cây cảnh trong nhà','2h',31,'2025-09-24 21:21:54.894476','2025-09-24 23:29:45.941415'),(11,'Giặt thảm','giat-tham','Giặt thảm gia đình','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(12,'Vệ sinh kính','ve-sinh-kinh','Lau kính trong ngoài','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(13,'Dọn nhà theo giờ','don-nha-theo-gio','Dịch vụ dọn theo giờ','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(15,'Phun khử khuẩn','phun-khu-khuan','Khử khuẩn không gian','job',1,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(16,'Diệt côn trùng','diet-con-trung','Kiểm soát côn trùng','job',NULL,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(17,'Lắp quạt trần','lap-quat-tran','Lắp đặt quạt trần','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(18,'Lắp ổ cắm công tắc','lap-o-cam-cong-tac','Thêm ổ cắm, công tắc','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(19,'Kiểm tra chập điện','kiem-tra-chap-dien','Tìm nguyên nhân chập','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(20,'Nạp gas điều hòa','nap-gas-dieu-hoa','Nạp bổ sung gas','job',7,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(21,'Sửa điều hòa','sua-dieu-hoa','Sửa chữa điều hòa','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(22,'Thông tắc bồn cầu','thong-tac-bon-cau','Thông tắc nhanh','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(23,'Thông tắc chậu rửa','thong-tac-chau-rua','Xử lý tắc nghẽn','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(24,'Lắp thiết bị vệ sinh','lap-thiet-bi-ve-sinh','Lavabo, bồn cầu, sen','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(25,'Lắp đặt bình nóng lạnh','lap-dat-binh-nong-lanh','Treo, đấu điện nước','job',3,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(26,'Lắp máy bơm nước','lap-may-bom-nuoc','Đấu nối và vận hành','job',4,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(27,'Sơn chống thấm tường','son-chong-tham-tuong','Sơn chống thấm tường','job',6,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(28,'Sơn cửa gỗ','son-cua-go','Sơn PU cơ bản','job',5,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(29,'Bả matit tường','ba-matit-tuong','Xử lý bề mặt trước sơn','job',5,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(30,'Vệ sinh bể nước','ve-sinh-be-nuoc','Vệ sinh bể ngầm/bồn','job',9,'2025-09-24 21:21:54.894476','2025-09-24 21:21:54.894476'),(31,'Chăm sóc cây cảnh','cham-soc-cay-canh','Chăm sóc cắt tỉa, tưới nước, bón phân cho cây','2h',NULL,'2025-09-24 23:27:57.251764','2025-09-24 23:27:57.251764'),(32,'Cắt tỉa cây thông','cat-tia-cay-thong','Cắt tỉa cây thông Noel','2h',31,'2025-09-24 23:28:33.876682','2025-09-24 23:28:33.876682'),(9401,'Vệ sinh nhà','cleaning',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9402,'Vệ sinh điều hòa','ac-clean',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9403,'Sửa ống nước','plumbing',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9404,'Sửa điện','electric',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9405,'Giặt sofa','sofa',NULL,'job',NULL,'2025-09-28 00:51:21.000000','2025-09-28 00:51:21.000000'),(9406,'Vệ sinh rèm cửa','ve-sinh-rem-cua','Giặt vệ sinh rèm, màn cửa','job',1,'2025-10-04 09:13:48.990449','2025-10-04 09:13:48.990449'),(9407,'Tổng vệ sinh sau xây dựng','tong-ve-sinh-sau-xay-dung','Vệ sinh sau sửa chữa/xây mới','job',1,'2025-10-04 09:13:48.996309','2025-10-04 09:13:48.996309'),(9408,'Vệ sinh tủ bếp','ve-sinh-tu-bep','Làm sạch và khử mùi tủ bếp','job',1,'2025-10-04 09:13:48.999538','2025-10-04 09:13:48.999538'),(9409,'Sửa quạt điện','sua-quat-dien','Sửa quạt trần/treo, thay tụ, tra dầu','job',3,'2025-10-04 09:13:49.002388','2025-10-04 09:13:49.002388'),(9410,'Sơn trần nhà','son-tran-nha','Sơn trần nội thất 1–2 lớp','job',5,'2025-10-04 09:13:49.004534','2025-10-04 09:13:49.004534'),(9411,'Sơn chống gỉ','son-chong-gi','Sơn chống gỉ bề mặt kim loại','job',5,'2025-10-04 09:13:49.006240','2025-10-04 09:13:49.006240'),(9412,'Chống thấm sân thượng','chong-tham-san-thuong','Xử lý chống thấm sân thượng','job',6,'2025-10-04 09:13:49.008334','2025-10-04 09:13:49.008334'),(9413,'Chống thấm nhà vệ sinh','chong-tham-nha-ve-sinh','Lớp phủ chống thấm khu vệ sinh','job',6,'2025-10-04 09:13:49.011890','2025-10-04 09:13:49.011890'),(9414,'Chống thấm ban công','chong-tham-ban-cong','Xử lý chống thấm ban công','job',6,'2025-10-04 09:13:49.014871','2025-10-04 09:13:49.014871'),(9415,'Bảo trì hệ thống điện','bao-tri-he-thong-dien','Kiểm tra định kỳ hệ thống điện','job',9,'2025-10-04 09:13:49.016680','2025-10-04 09:13:49.016680'),(9416,'Bảo trì hệ thống nước','bao-tri-he-thong-nuoc','Súc rửa, kiểm tra hệ thống nước','job',9,'2025-10-04 09:13:49.018989','2025-10-04 09:13:49.018989'),(9417,'Bảo trì máy bơm','bao-tri-may-bom','Bảo dưỡng, thay vật tư máy bơm','job',9,'2025-10-04 09:13:49.020892','2025-10-04 09:13:49.020892'),(9418,'Tưới cây định kỳ','tuoi-cay-dinh-ky','Tưới cây theo lịch','job',31,'2025-10-04 09:13:49.023422','2025-10-04 09:13:49.023422'),(9419,'Bón phân cây cảnh','bon-phan-cay-canh','Bón phân, dưỡng chất cho cây','job',31,'2025-10-04 09:13:49.025607','2025-10-04 09:13:49.025607');
/*!40000 ALTER TABLE `service` ENABLE KEYS */;
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
