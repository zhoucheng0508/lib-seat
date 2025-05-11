/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.11-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: lib-seat-db-b-mysql.ns-51r2zsdm.svc    Database: lib_seat
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `admins` (
  `id` varchar(32) NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mi8vkhus4xbdbqcac2jm4spvd` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `reservations` (
  `id` varchar(32) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date` date NOT NULL,
  `end_time` time(6) NOT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `seat_id` varchar(255) NOT NULL,
  `start_time` time(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `study_room_id` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `adjusted_at` date DEFAULT NULL,
  `adjusted_by` varchar(255) DEFAULT NULL,
  `deleted_at` date DEFAULT NULL,
  `deleted_by` varchar(255) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_reservations_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`),
  CONSTRAINT `FK_reservations_study_room` FOREIGN KEY (`study_room_id`) REFERENCES `study_rooms` (`id`),
  CONSTRAINT `FK_reservations_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seat_reservations`
--

DROP TABLE IF EXISTS `seat_reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `seat_reservations` (
  `id` varchar(32) NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `end_time` varchar(255) NOT NULL,
  `reservation_date` varchar(255) NOT NULL,
  `start_time` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `seat_id` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_seat_reservations_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seats`
--

DROP TABLE IF EXISTS `seats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `seats` (
  `id` varchar(32) NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `seat_number` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `study_room_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_seats_study_room` FOREIGN KEY (`study_room_id`) REFERENCES `study_rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_room_status`
--

DROP TABLE IF EXISTS `study_room_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `study_room_status` (
  `id` varchar(32) NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `end_time` varchar(255) NOT NULL,
  `start_time` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `study_room_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_study_room_status_study_room` FOREIGN KEY (`study_room_id`) REFERENCES `study_rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_room_time_slots`
--

DROP TABLE IF EXISTS `study_room_time_slots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `study_room_time_slots` (
  `id` varchar(32) NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `end_time` varchar(255) NOT NULL,
  `slot_date` varchar(255) NOT NULL,
  `start_time` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `study_room_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_study_room_time_slots_study_room` FOREIGN KEY (`study_room_id`) REFERENCES `study_rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_rooms`
--

DROP TABLE IF EXISTS `study_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `study_rooms` (
  `id` varchar(36) NOT NULL,
  `capacity` int NOT NULL,
  `close_time` varchar(255) DEFAULT NULL,
  `created_at` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `max_advance_days` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `open_time` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `time_slots`
--

DROP TABLE IF EXISTS `time_slots`;


DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `users` (
  `id` varchar(32) NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `blacklist_start_time` datetime(6) DEFAULT NULL,
  `is_blacklisted` bit(1) DEFAULT NULL,
  `no_show_count` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` varchar(32) NOT NULL,
  `content` text NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `processed_at` bigint DEFAULT NULL,
  `processor_id` varchar(32) DEFAULT NULL,
  `response` text,
  `status` varchar(20) NOT NULL DEFAULT '待处理',
  `type` varchar(50) NOT NULL,
  `updated_at` bigint DEFAULT NULL,
  `user_id` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK_feedback_processor` FOREIGN KEY (`processor_id`) REFERENCES `admins` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-10  1:55:25
