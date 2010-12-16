-- MySQL dump 10.13  Distrib 5.1.48, for Win64 (unknown)
--
-- Host: localhost    Database: algotrader
-- ------------------------------------------------------
-- Server version    5.1.48-community-log

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
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `TBL` varchar(255) NOT NULL,
  `REF_ID` int(11) NOT NULL,
  `TIME` datetime NOT NULL,
  `COL` varchar(255) DEFAULT NULL,
  `VALUE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40924 DEFAULT CHARSET=latin1;
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
  `EXIT_VALUE` double(15,10) DEFAULT NULL,
  `MAINTENANCE_MARGIN` decimal(17,2) DEFAULT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `QUANTITY` (`QUANTITY`),
  KEY `POSITION_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `POSITION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=226 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `position_after_update` AFTER UPDATE ON `position`
  FOR EACH ROW
BEGIN
     IF NOT NEW.QUANTITY = OLD.QUANTITY OR (NEW.QUANTITY IS NULL XOR OLD.QUANTITY IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'QUANTITY', NEW.QUANTITY);
     END IF;
     IF NOT NEW.EXIT_VALUE = OLD.EXIT_VALUE OR (NEW.EXIT_VALUE IS NULL XOR OLD.EXIT_VALUE IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'EXIT_VALUE', NEW.EXIT_VALUE);
     END IF;
     IF NOT NEW.MAINTENANCE_MARGIN = OLD.MAINTENANCE_MARGIN OR (NEW.MAINTENANCE_MARGIN IS NULL XOR OLD.MAINTENANCE_MARGIN IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'MAINTENANCE_MARGIN', NEW.MAINTENANCE_MARGIN);
     END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rule`
--

DROP TABLE IF EXISTS `rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(30) NOT NULL,
  `PRIORITY` tinyint(4) NOT NULL DEFAULT '0',
  `DEFINITION` text NOT NULL,
  `SUBSCRIBER` varchar(100) DEFAULT NULL,
  `LISTENERS` varchar(100) DEFAULT NULL,
  `AUTO_ACTIVATE` bit(1) NOT NULL DEFAULT b'1',
  `INIT` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `NAME` (`NAME`(20))
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rules2strategies`
--

DROP TABLE IF EXISTS `rules2strategies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rules2strategies` (
  `RULES_FK` int(11) NOT NULL,
  `STRATEGIES_FK` int(11) NOT NULL,
  PRIMARY KEY (`RULES_FK`,`STRATEGIES_FK`),
  KEY `STRATEGY_RULES_FKC` (`RULES_FK`),
  KEY `RULE_STRATEGIES_FKC` (`STRATEGIES_FK`),
  CONSTRAINT `RULE_STRATEGIES_FKC` FOREIGN KEY (`STRATEGIES_FK`) REFERENCES `strategy` (`id`),
  CONSTRAINT `STRATEGY_RULES_FKC` FOREIGN KEY (`RULES_FK`) REFERENCES `rule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `saldo`
--

DROP TABLE IF EXISTS `saldo`;
/*!50001 DROP VIEW IF EXISTS `saldo`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `saldo` (
  `id` int(11),
  `date_time` datetime,
  `type` enum('BUY','SELL','DIVIDEND','DEBIT','CREDIT','FEES','INTREST','EXPIRATION'),
  `symbol` varchar(30),
  `isin` varchar(20),
  `position_fk` int(11),
  `STRIKE` decimal(9,2),
  `expiration` datetime,
  `quantity` bigint(20),
  `price` decimal(9,2),
  `commission` decimal(15,2),
  `saldo` decimal(61,2)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `security`
--

DROP TABLE IF EXISTS `security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ISIN` varchar(20) DEFAULT NULL,
  `SYMBOL` varchar(30) NOT NULL,
  `UNDERLAYING_FK` int(11) DEFAULT NULL,
  `VOLATILITY_FK` int(11) DEFAULT NULL,
  `POSITION_FK` int(11) DEFAULT NULL,
  `SECURITY_FAMILY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `SYMBOL` (`SYMBOL`),
  UNIQUE KEY `ISIN` (`ISIN`),
  UNIQUE KEY `POSITION_FK` (`POSITION_FK`),
  KEY `SECURITY_UNDERLAYING_FKC` (`UNDERLAYING_FK`),
  KEY `SECURITY_VOLATILITY_FKC` (`VOLATILITY_FK`),
  KEY `SECURITY_SECURITY_FAMILY_FKC` (`SECURITY_FAMILY_FK`),
  CONSTRAINT `SECURITY_SECURITY_FAMILY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`),
  CONSTRAINT `SECURITY_UNDERLAYING_FKC` FOREIGN KEY (`UNDERLAYING_FK`) REFERENCES `security` (`id`),
  CONSTRAINT `SECURITY_VOLATILITY_FKC` FOREIGN KEY (`VOLATILITY_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=252 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `security_after_update` AFTER UPDATE ON `security`
  FOR EACH ROW
BEGIN
     IF NOT NEW.POSITION_FK = OLD.POSITION_FK OR (NEW.POSITION_FK IS NULL XOR OLD.POSITION_FK IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('security', NEW.id, NOW(), 'POSITION_FK', NEW.POSITION_FK);
     END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `security_family`
--

DROP TABLE IF EXISTS `security_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_family` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `MARKET` enum('EUREX','DE') NOT NULL,
  `CURRENCY` enum('CHF','EUR') NOT NULL,
  `CONTRACT_SIZE` int(11) NOT NULL,
  `TICK_SIZE` double NOT NULL,
  `COMMISSION` decimal(10,2) DEFAULT NULL,
  `MARKET_OPEN` time NOT NULL,
  `MARKET_CLOSE` time NOT NULL,
  `SPREAD_SLOPE` double DEFAULT NULL,
  `SPREAD_CONSTANT` double DEFAULT NULL,
  `MAX_SPREAD_SLOPE` double DEFAULT NULL,
  `MAX_SPREAD_CONSTANT` double DEFAULT NULL,
  `UNDERLAYING_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `SECURITY_FAMILY_UNDERLAYING_FC` (`UNDERLAYING_FK`),
  CONSTRAINT `SECURITY_FAMILY_UNDERLAYING_FC` FOREIGN KEY (`UNDERLAYING_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
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
  PRIMARY KEY (`ID`),
  KEY `OPTIONIFKC` (`ID`),
  KEY `STRIKE` (`STRIKE`),
  KEY `EXPIRATION` (`EXPIRATION`),
  KEY `TYPE` (`TYPE`),
  CONSTRAINT `OPTIONIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_option_family`
--

DROP TABLE IF EXISTS `stock_option_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_option_family` (
  `ID` int(11) NOT NULL,
  `STRIKE_DISTANCE` double(15,10) NOT NULL,
  `INTREST` double(15,10) NOT NULL,
  `DIVIDEND` double(15,10) NOT NULL,
  `MARGIN_PARAMETER` double(15,10) NOT NULL,
  `BETA` double(15,10) NOT NULL,
  `CORRELATION` double(15,10) NOT NULL,
  `VOL_VOL` double(15,10) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `STOCK_OPTION_FAMILYIFKC` (`ID`),
  CONSTRAINT `STOCK_OPTION_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `strategy`
--

DROP TABLE IF EXISTS `strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `strategy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(30) NOT NULL,
  `GROUP` varchar(20) NOT NULL,
  `AUTO_ACTIVATE` bit(1) NOT NULL,
  `ALLOCATION` double(15,3) NOT NULL,
  `UNDERLAYING_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `NAME` (`NAME`),
  KEY `STRATEGY_UNDERLAYING_FKC` (`UNDERLAYING_FK`),
  CONSTRAINT `STRATEGY_UNDERLAYING_FKC` FOREIGN KEY (`UNDERLAYING_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tick`
--

DROP TABLE IF EXISTS `tick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tick` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `DATE_TIME` datetime NOT NULL,
  `LAST` decimal(11,2) DEFAULT NULL,
  `LAST_DATE_TIME` datetime DEFAULT NULL,
  `VOL` int(11) DEFAULT NULL,
  `VOL_BID` int(11) DEFAULT NULL,
  `VOL_ASK` int(11) DEFAULT NULL,
  `BID` decimal(11,2) DEFAULT NULL,
  `ASK` decimal(11,2) DEFAULT NULL,
  `OPEN_INTREST` int(11) DEFAULT NULL,
  `SETTLEMENT` decimal(11,2) DEFAULT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `DATE_TIME_SECURITY_FK_UNIQUE` (`DATE_TIME`,`SECURITY_FK`),
  KEY `DATE_TIME` (`DATE_TIME`),
  KEY `SECURITY_FKC` (`SECURITY_FK`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NUMBER` varchar(30) DEFAULT NULL,
  `DATE_TIME` datetime NOT NULL,
  `QUANTITY` bigint(20) NOT NULL,
  `PRICE` decimal(9,2) NOT NULL,
  `COMMISSION` decimal(15,2) DEFAULT NULL,
  `CURRENCY` enum('CHF','EUR') NOT NULL,
  `TYPE` enum('BUY','SELL','DIVIDEND','DEBIT','CREDIT','FEES','INTREST','EXPIRATION') NOT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `SECURITY_FK` int(11) DEFAULT NULL,
  `STRATEGY_FK` int(11) DEFAULT NULL,
  `POSITION_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `TRANSACTION_POSITION_FKC` (`POSITION_FK`),
  KEY `TRANSACTION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `TRANSACTION_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `TRANSACTION_POSITION_FKC` FOREIGN KEY (`POSITION_FK`) REFERENCES `position` (`id`),
  CONSTRAINT `TRANSACTION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`),
  CONSTRAINT `TRANSACTION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=630 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watchers2watchlist`
--

DROP TABLE IF EXISTS `watchers2watchlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `watchers2watchlist` (
  `WATCHLIST_FK` int(11) NOT NULL,
  `WATCHERS_FK` int(11) NOT NULL,
  PRIMARY KEY (`WATCHLIST_FK`,`WATCHERS_FK`),
  KEY `STRATEGY_WATCHLIST_FKC` (`WATCHLIST_FK`),
  KEY `SECURITY_WATCHERS_FKC` (`WATCHERS_FK`),
  CONSTRAINT `SECURITY_WATCHERS_FKC` FOREIGN KEY (`WATCHERS_FK`) REFERENCES `strategy` (`id`),
  CONSTRAINT `STRATEGY_WATCHLIST_FKC` FOREIGN KEY (`WATCHLIST_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Final view structure for view `saldo`
--

/*!50001 DROP TABLE IF EXISTS `saldo`*/;
/*!50001 DROP VIEW IF EXISTS `saldo`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `saldo` AS select `t1`.`id` AS `id`,`t1`.`DATE_TIME` AS `date_time`,`t1`.`TYPE` AS `type`,`s1`.`SYMBOL` AS `symbol`,`s1`.`ISIN` AS `isin`,`s1`.`POSITION_FK` AS `position_fk`,`o1`.`STRIKE` AS `STRIKE`,`o1`.`EXPIRATION` AS `expiration`,`t1`.`QUANTITY` AS `quantity`,`t1`.`PRICE` AS `price`,`t1`.`COMMISSION` AS `commission`,(select sum((case `t2`.`TYPE` when 'CREDIT' then `t2`.`PRICE` when 'DEBIT' then -(`t2`.`PRICE`) when 'FEES' then -(`t2`.`PRICE`) else (((-(`t2`.`QUANTITY`) * `f2`.`CONTRACT_SIZE`) * `t2`.`PRICE`) - `t2`.`COMMISSION`) end)) AS `FIELD_1` from (((`transaction` `t2` left join `stock_option` `o2` on((`t2`.`SECURITY_FK` = `o2`.`ID`))) left join `security` `s2` on((`t2`.`SECURITY_FK` = `s2`.`id`))) left join `security_family` `f2` on((`s2`.`SECURITY_FAMILY_FK` = `f2`.`id`))) where (`t2`.`DATE_TIME` <= `t1`.`DATE_TIME`)) AS `saldo` from ((`transaction` `t1` left join `security` `s1` on((`t1`.`SECURITY_FK` = `s1`.`id`))) left join `stock_option` `o1` on((`s1`.`id` = `o1`.`ID`))) order by `t1`.`id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-12-16 17:44:39
