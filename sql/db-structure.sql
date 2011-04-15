-- MySQL dump 10.13  Distrib 5.1.48, for Win64 (unknown)
--
-- Host: localhost    Database: algotraderlive
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
-- Table structure for table `dividend`
--

DROP TABLE IF EXISTS `dividend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dividend` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `DATE_TIME` datetime NOT NULL,
  `AMOUNT` decimal(10,0) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `DIVIDEND_SECURITY_FKC` (`SECURITY_FK`),
  CONSTRAINT `DIVIDEND_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forex`
--

DROP TABLE IF EXISTS `forex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forex` (
  `ID` int(11) NOT NULL,
  `BASE_CURRENCY` enum('CHF','EUR','USD') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FOREXIFKC` (`ID`),
  CONSTRAINT `FOREXIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `future`
--

DROP TABLE IF EXISTS `future`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `future` (
  `ID` int(11) NOT NULL,
  `EXPIRATION` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FUTUREIFKC` (`ID`),
  CONSTRAINT `FUTUREIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `future_family`
--

DROP TABLE IF EXISTS `future_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `future_family` (
  `ID` int(11) NOT NULL,
  `INTREST` double(15,10) NOT NULL,
  `DIVIDEND` double(15,10) NOT NULL,
  `MARGIN_PARAMETER` double(15,10) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FUTURE_FAMILYIFKC` (`ID`),
  CONSTRAINT `FUTURE_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=164 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intrest_rate`
--

DROP TABLE IF EXISTS `intrest_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intrest_rate` (
  `ID` int(11) NOT NULL,
  `DURATION` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `INTREST_RATEIFKC` (`ID`),
  CONSTRAINT `INTREST_RATEIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  `SECURITY_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `SECURITY_FK_STRATEGY_FK` (`SECURITY_FK`,`STRATEGY_FK`),
  KEY `QUANTITY` (`QUANTITY`),
  KEY `POSITION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `POSITION_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `POSITION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`),
  CONSTRAINT `POSITION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
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
-- Table structure for table `sabr_params`
--

DROP TABLE IF EXISTS `sabr_params`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sabr_params` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `DATE_TIME` datetime NOT NULL,
  `ATM_VOLA_DELTA` double NOT NULL,
  `RHO_CALL` double NOT NULL,
  `RHO_PUT` double NOT NULL,
  `VOL_VOL_CALL` double NOT NULL,
  `VOL_VOL_PUT` double NOT NULL,
  `STOCK_OPTION_FAMILY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `SABR_PARAMS_STOCK_OPTION_FAMIC` (`STOCK_OPTION_FAMILY_FK`),
  CONSTRAINT `SABR_PARAMS_STOCK_OPTION_FAMIC` FOREIGN KEY (`STOCK_OPTION_FAMILY_FK`) REFERENCES `stock_option_family` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `saldo`
--

DROP TABLE IF EXISTS `saldo`;
/*!50001 DROP VIEW IF EXISTS `saldo`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `saldo` (
  `date_time` datetime,
  `type` enum('BUY','SELL','EXPIRATION','CREDIT','DEBIT','INTREST','FEES','REBALANCE'),
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
  `SECURITY_FAMILY_FK` int(11) NOT NULL,
  `INTREST_RATE_FAMILY_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `SYMBOL` (`SYMBOL`),
  UNIQUE KEY `ISIN` (`ISIN`),
  KEY `SECURITY_SECURITY_FAMILY_FKC` (`SECURITY_FAMILY_FK`),
  KEY `SECURITY_UNDERLAYING_FKC` (`UNDERLAYING_FK`),
  KEY `SECURITY_VOLATILITY_FKC` (`VOLATILITY_FK`),
  CONSTRAINT `SECURITY_SECURITY_FAMILY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`),
  CONSTRAINT `SECURITY_UNDERLAYING_FKC` FOREIGN KEY (`UNDERLAYING_FK`) REFERENCES `security` (`id`),
  CONSTRAINT `SECURITY_VOLATILITY_FKC` FOREIGN KEY (`VOLATILITY_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114439 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security_family`
--

DROP TABLE IF EXISTS `security_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_family` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `MARKET` enum('SOFFEX','DTB','IDEALPRO') NOT NULL,
  `CURRENCY` enum('CHF','EUR','USD') NOT NULL,
  `CONTRACT_SIZE` int(11) NOT NULL,
  `TICK_SIZE` double NOT NULL,
  `COMMISSION` decimal(10,5) DEFAULT NULL,
  `MARKET_OPEN` time NOT NULL,
  `MARKET_CLOSE` time NOT NULL,
  `TRADEABLE` bit(1) DEFAULT NULL,
  `SIMULATABLE` bit(1) NOT NULL,
  `SPREAD_SLOPE` double DEFAULT NULL,
  `SPREAD_CONSTANT` double DEFAULT NULL,
  `MAX_SPREAD_SLOPE` double DEFAULT NULL,
  `MAX_SPREAD_CONSTANT` double DEFAULT NULL,
  `PERIODICITY` enum('MINUTE','HOUR','DAY') DEFAULT NULL,
  `UNDERLAYING_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `SECURITY_FAMILY_UNDERLAYING_FC` (`UNDERLAYING_FK`),
  CONSTRAINT `SECURITY_FAMILY_UNDERLAYING_FC` FOREIGN KEY (`UNDERLAYING_FK`) REFERENCES `security` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
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
  PRIMARY KEY (`ID`),
  KEY `STOCK_OPTION_FAMILYIFKC` (`ID`),
  CONSTRAINT `STOCK_OPTION_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
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
  `FAMILY` varchar(20) NOT NULL,
  `AUTO_ACTIVATE` bit(1) NOT NULL,
  `ALLOCATION` double(15,3) NOT NULL,
  `MODULES` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `NAME` (`NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
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
  `VOL` int(11) NOT NULL,
  `VOL_BID` int(11) NOT NULL,
  `VOL_ASK` int(11) NOT NULL,
  `BID` decimal(11,2) NOT NULL,
  `ASK` decimal(11,2) NOT NULL,
  `OPEN_INTREST` int(11) NOT NULL,
  `SETTLEMENT` decimal(11,2) DEFAULT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `DATE_TIME_SECURITY_FK` (`DATE_TIME`,`SECURITY_FK`),
  KEY `DATE_TIME` (`DATE_TIME`),
  KEY `TICK_SECURITY_FKC` (`SECURITY_FK`)
) ENGINE=MyISAM AUTO_INCREMENT=150 DEFAULT CHARSET=latin1 ROW_FORMAT=FIXED;
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
  `TYPE` enum('BUY','SELL','EXPIRATION','CREDIT','DEBIT','INTREST','FEES','REBALANCE') NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=11228 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watch_list_item`
--

DROP TABLE IF EXISTS `watch_list_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `watch_list_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `PERSISTENT` bit(1) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `STRATEGY_SECURITY_UNIQUE` (`SECURITY_FK`,`STRATEGY_FK`),
  KEY `WATCH_LIST_ITEM_SECURITY_FKC` (`SECURITY_FK`),
  KEY `WATCH_LIST_ITEM_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `WATCH_LIST_ITEM_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`),
  CONSTRAINT `WATCH_LIST_ITEM_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
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
/*!50001 VIEW `saldo` AS select `t1`.`DATE_TIME` AS `date_time`,`t1`.`TYPE` AS `type`,`s1`.`SYMBOL` AS `symbol`,`s1`.`ISIN` AS `isin`,`t1`.`POSITION_FK` AS `position_fk`,`o1`.`STRIKE` AS `STRIKE`,`o1`.`EXPIRATION` AS `expiration`,`t1`.`QUANTITY` AS `quantity`,`t1`.`PRICE` AS `price`,`t1`.`COMMISSION` AS `commission`,(select sum((case `t2`.`TYPE` when 'CREDIT' then `t2`.`PRICE` when 'DEBIT' then `t2`.`PRICE` else (((-(`t2`.`QUANTITY`) * `t2`.`PRICE`) * `f2`.`CONTRACT_SIZE`) - `t2`.`COMMISSION`) end)) from ((`transaction` `t2` left join `security` `s2` on((`t2`.`SECURITY_FK` = `s2`.`id`))) left join `security_family` `f2` on((`s2`.`SECURITY_FAMILY_FK` = `f2`.`id`))) where (`t2`.`id` <= `t1`.`id`)) AS `saldo` from ((`transaction` `t1` left join `security` `s1` on((`t1`.`SECURITY_FK` = `s1`.`id`))) left join `stock_option` `o1` on((`o1`.`ID` = `s1`.`id`))) order by `t1`.`id` */;
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

-- Dump completed on 2011-04-15 14:19:47
