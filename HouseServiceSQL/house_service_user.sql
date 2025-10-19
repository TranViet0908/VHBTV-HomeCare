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
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_CUSTOMER','ROLE_VENDOR') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ROLE_CUSTOMER',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'vendor01','$2a$10$hash','ROLE_VENDOR','vendor01@example.com','0900000001',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(2,'vendor02','$2a$10$hash','ROLE_VENDOR','vendor02@example.com','0900000002',NULL,1,'2025-09-20 05:24:29','2025-09-22 02:54:18'),(3,'vendor03','$2a$10$hash','ROLE_VENDOR','vendor03@example.com','0900000003',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(4,'vendor04','$2a$10$hash','ROLE_VENDOR','vendor04@example.com','0900000004',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(5,'vendor05','$2a$10$hash','ROLE_VENDOR','vendor05@example.com','0900000005',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(6,'vendor06','$2a$10$hash','ROLE_VENDOR','vendor06@example.com','0900000006',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(7,'vendor07','$2a$10$hash','ROLE_VENDOR','vendor07@example.com','0900000007',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(8,'vendor08','$2a$10$hash','ROLE_VENDOR','vendor08@example.com','0900000008',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(9,'vendor09','$2a$10$hash','ROLE_VENDOR','vendor09@example.com','0900000009',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(10,'vendor10','$2a$10$hash','ROLE_VENDOR','vendor10@example.com','0900000010',NULL,1,'2025-09-20 05:24:29','2025-09-20 05:24:29'),(13,'admin','$2a$10$j672HpXtoT/G4zLg9f.58uDokC.k8gxdc0LcBtXHRTLP/GOluddvi','ROLE_ADMIN','admin@gmail.com','0123456789',NULL,1,'2025-09-20 07:56:34','2025-09-21 08:15:07'),(14,'TranViet0908','$2a$10$7BcXb7eemLMJqq9NUkTnxOTb/IvammDnBF4D9aT1xQzt/Dlopxniu','ROLE_CUSTOMER','viettun0908@gmail.com','0397915683','/uploads/avatars/f306f34afab54eca95c2ca0294a76e88.png',1,'2025-09-21 10:05:19','2025-09-22 03:21:13'),(15,'viettun0908','$2a$10$NKCD5l/bwy5kZE468cPDXe7SwJuy4tYS6hAjpaZ/uDhE2tGTujMVy','ROLE_VENDOR','tranviet2004v@gmail.com','0999999999','/uploads/avatars/be79aded539b47c1bd29a423ac647814.png',1,'2025-09-21 10:07:39','2025-10-01 18:02:57'),(17,'TestUsername','$2a$10$.LaCEzHSFkXs9KrPUdBz/.9UZJOPFqFKyoiL/H2LQXXG26h.E7mnu','ROLE_CUSTOMER','1@gmail.com','0147852369','/uploads/avatars/73f3440b842b4f42b07626211975b995.png',1,'2025-09-22 03:06:30','2025-09-22 03:21:26'),(18,'a','$2a$10$DyhgxiuA0E/XfBQdsTz4gOuorFlTBzjumlpd0cYTMH4kx..BMtw5y','ROLE_CUSTOMER','2@gmail.com','0123654789','/uploads/avatars/2b134ea813bc4cf8a80865c146e892be.png',1,'2025-09-22 03:23:40','2025-09-22 03:23:40'),(19,'cust_anhkhoa','$2a$10$r8nNf2qk2eQ7zF4z5u9rAeG0V9x9o2l5vF6G2j3T1u9b0Lw1xYtmi','ROLE_CUSTOMER','anhkhoa@example.com','0900000001',NULL,1,'2025-09-22 15:49:15','2025-09-22 15:49:15'),(20,'cust_minhchau','$2a$10$r8nNf2qk2eQ7zF4z5u9rAeG0V9x9o2l5vF6G2j3T1u9b0Lw1xYtmi','ROLE_CUSTOMER','minhchau@example.com','0900000002',NULL,1,'2025-09-22 15:49:15','2025-09-22 15:49:15'),(999,'guest','$2a$10$7BcXb7eemLMJqq9NUkTnxOTb/IvammDnBF4D9aT1xQzt/Dlopxniu','ROLE_CUSTOMER','guest@example.com',NULL,NULL,1,'2025-09-29 03:32:29','2025-09-29 03:32:29'),(9001,'seed_vendor1','{noop}123456','ROLE_VENDOR','vendor1@example.com','0900001000',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9002,'seed_vendor2','{noop}123456','ROLE_VENDOR','vendor2@example.com','0900001001',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9003,'seed_vendor3','{noop}123456','ROLE_VENDOR','vendor3@example.com','0900001002',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9004,'seed_vendor4','{noop}123456','ROLE_VENDOR','vendor4@example.com','0900001003',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9005,'seed_vendor5','{noop}123456','ROLE_VENDOR','vendor5@example.com','0900001004',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9101,'seed_cust1','{noop}123456','ROLE_CUSTOMER','cust1@example.com','0900001005',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9102,'seed_cust2','{noop}123456','ROLE_CUSTOMER','cust2@example.com','0900001006',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9103,'seed_cust3','{noop}123456','ROLE_CUSTOMER','cust3@example.com','0900001007',NULL,1,'2025-09-27 17:51:21','2025-09-27 17:51:21'),(9104,'Namtun123','$2a$10$UaYK6gGr5jLCcWzt1qdSUuywB6pncYHOXpn0148EYmURwNfxNIjd6','ROLE_CUSTOMER','123@gmail.com','0909090909','/uploads/avatars/4737bdd4413d4e52b8f7d8196c12f536.jpg',1,'2025-10-18 10:52:21','2025-10-18 10:52:21'),(9105,'Cena','$2a$10$TU77IVQtqMCOZZh2NDDhD.NiHVb944W0ErSGuYK1SOcsYIn2.7vOu','ROLE_CUSTOMER','cena@gmail.com','0111111111','/uploads/avatars/8185e7cb2dd1456abea7b42227faa3f7.png',1,'2025-10-18 11:19:22','2025-10-18 11:19:22'),(9106,'CMPunk','$2a$10$kXoZiT680/O247Y0cI7YtOlR1Rgl3LW/m2OL.6mw7LDVUKIxoDo72','ROLE_CUSTOMER','cmpunk@gmail.com','0222222222','/uploads/avatars/3d42ed358b8441af809246568d142d74.jpg',1,'2025-10-18 11:21:03','2025-10-18 11:21:03'),(9107,'aaa','$2a$10$rLCaiY./uW/.ww./H5tF1ufG0He3nwcislBMK9EJ/QfCJt.RE8g06','ROLE_CUSTOMER','valid1@example.com','0999999998','/uploads/avatars/19199fe423784242a8b35bfbca5f49d9.svg',1,'2025-10-19 10:13:09','2025-10-19 10:13:09');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-19 22:42:07
