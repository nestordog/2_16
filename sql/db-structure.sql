-- MySQL dump 10.13  Distrib 5.1.41, for Win32 (ia32)
--
-- Host: localhost    Database: algotrader
-- ------------------------------------------------------
-- Server version    5.1.41-community

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `CURRENCY` enum('CHF','EUR') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `position`
--

DROP TABLE IF EXISTS `position`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `QUANTITY` bigint(20) NOT NULL,
  `EXIT_VALUE` decimal(9,2) DEFAULT NULL,
  `MARGIN` decimal(17,2) DEFAULT NULL,
  `ACCOUNT_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `POSITION_ACCOUNT_FKC` (`ACCOUNT_FK`),
  CONSTRAINT `POSITION_ACCOUNT_FKC` FOREIGN KEY (`ACCOUNT_FK`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4335 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule`
--

DROP TABLE IF EXISTS `rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `PRIORITY` tinyint(4) NOT NULL,
  `DEFINITION` text NOT NULL,
  `SUBSCRIBER` varchar(255) DEFAULT NULL,
  `LISTENERS` varchar(255) DEFAULT NULL,
  `PATTERN` bit(1) NOT NULL,
  `ACTIVATABLE` bit(1) NOT NULL,
  `PREPARED` bit(1) NOT NULL,
  `TARGET_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security`
--

DROP TABLE IF EXISTS `security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ISIN` varchar(20) DEFAULT NULL,
  `SYMBOL` varchar(30) DEFAULT NULL,
  `MARKET` enum('21','M9') NOT NULL,
  `CURRENCY` enum('CHF','EUR') NOT NULL,
  `UNDERLAYING_FK` int(11) DEFAULT NULL,
  `VOLATILITY_FK` int(11) DEFAULT NULL,
  `POSITION_FK` int(11) DEFAULT NULL,
  `ON_WATCHLIST` bit(1) NOT NULL,
  `DUMMY` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ISIN` (`ISIN`),
  UNIQUE KEY `SYMBOL` (`SYMBOL`),
  KEY `SECURITY_UNDERLAYING_FKC` (`UNDERLAYING_FK`),
  KEY `SECURITY_VOLATILITY_FKC` (`VOLATILITY_FK`),
  CONSTRAINT `SECURITY_UNDERLAYING_FKC` FOREIGN KEY (`UNDERLAYING_FK`) REFERENCES `security` (`id`),
  CONSTRAINT `SECURITY_VOLATILITY_FKC` FOREIGN KEY (`VOLATILITY_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9365 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_option`
--

DROP TABLE IF EXISTS `stock_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_option` (
  `ID` int(11) NOT NULL,
  `STRIKE` decimal(9,2) NOT NULL,
  `EXPIRATION` datetime NOT NULL,
  `TYPE` enum('CALL','PUT') NOT NULL,
  `CONTRACT_SIZE` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `OPTIONIFKC` (`ID`),
  CONSTRAINT `OPTIONIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NUMBER` varchar(20) DEFAULT NULL,
  `DATE_TIME` datetime NOT NULL,
  `QUANTITY` bigint(20) NOT NULL,
  `PRICE` decimal(9,2) NOT NULL,
  `COMMISSION` decimal(15,2) DEFAULT NULL,
  `TYPE` enum('BUY','SELL','DIVIDEND','DEBIT','CREDIT','FEES','INTREST','EXPIRATION') NOT NULL,
  `SECURITY_FK` int(11) DEFAULT NULL,
  `ACCOUNT_FK` int(11) NOT NULL,
  `POSITION_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `TRANSACTION_ACCOUNT_FKC` (`ACCOUNT_FK`),
  KEY `TRANSACTION_POSITION_FKC` (`POSITION_FK`),
  KEY `TRANSACTION_SECURITY_FKC` (`SECURITY_FK`),
  CONSTRAINT `TRANSACTION_ACCOUNT_FKC` FOREIGN KEY (`ACCOUNT_FK`) REFERENCES `account` (`id`),
  CONSTRAINT `TRANSACTION_POSITION_FKC` FOREIGN KEY (`POSITION_FK`) REFERENCES `position` (`id`),
  CONSTRAINT `TRANSACTION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9459 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-03-25 17:24:45
