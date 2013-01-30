-- MySQL dump 10.13  Distrib 5.5.21, for Win64 (x86)
--
-- Host: localhost    Database: algotrader
-- ------------------------------------------------------
-- Server version    5.5.21-log

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
-- Table structure for table `bar`
--

DROP TABLE IF EXISTS `bar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bar` (
  `ID` int(11) NOT NULL,
  `DATE_TIME` datetime NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `OPEN` decimal(12,5) NOT NULL,
  `HIGH` decimal(12,5) NOT NULL,
  `LOW` decimal(12,5) NOT NULL,
  `CLOSE` decimal(12,5) NOT NULL,
  `VOL` int(11) NOT NULL,
  `OPEN_INTEREST` int(11) NOT NULL,
  `SETTLEMENT` decimal(12,5) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `MARKET_DATA_EVENT_SECURITY_FKC17c13` (`SECURITY_FK`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cash_balance`
--

DROP TABLE IF EXISTS `cash_balance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cash_balance` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CURRENCY` enum('CHF','EUR','USD','GBP') NOT NULL,
  `AMOUNT` decimal(15,2) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CURRENCY_STRATEGY_UNIQUE` (`CURRENCY`,`STRATEGY_FK`),
  KEY `CASH_BALANCE_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `CASH_BALANCE_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `combination`
--

DROP TABLE IF EXISTS `combination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `combination` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TYPE` enum('VERTICAL_SPREAD','COVERED_CALL','RATIO_SPREAD','STRADDLE','STRANGLE','BUTTERFLY','CALENDAR_SPREAD','IRON_CONDOR') NOT NULL,
  `PERSISTENT` bit(1) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `COMBINATIONIFKC` (`ID`),
  CONSTRAINT `COMBINATIONIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `component`
--

DROP TABLE IF EXISTS `component`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `component` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `QUANTITY` bigint(20) NOT NULL,
  `PERSISTENT` bit(1) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `PARENT_SECURITY_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `SECURITY_PARENT_SECURITY_UNIQUE` (`SECURITY_FK`,`PARENT_SECURITY_FK`),
  KEY `COMPONENT_SECURITY_FKC` (`SECURITY_FK`),
  KEY `COMPONENT_PARENT_SECURITY_FK` (`PARENT_SECURITY_FK`),
  CONSTRAINT `COMPONENT_PARENT_SECURITY_FKC` FOREIGN KEY (`PARENT_SECURITY_FK`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `COMPONENT_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `default_order_preference`
--

DROP TABLE IF EXISTS `default_order_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `default_order_preference` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ORDER_PREFERENCE_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  `SECURITY_FAMILY_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `DEFAULT_ORDER_PREFERENCE_SECUC` (`SECURITY_FAMILY_FK`),
  KEY `DEFAULT_ORDER_PREFERENCE_ORDEC` (`ORDER_PREFERENCE_FK`),
  KEY `DEFAULT_ORDER_PREFERENCE_STRAC` (`STRATEGY_FK`),
  CONSTRAINT `DEFAULT_ORDER_PREFERENCE_SECURITY_FAMILY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`),
  CONSTRAINT `DEFAULT_ORDER_PREFERENCE_ORDER_PREFERENCE_FKC` FOREIGN KEY (`ORDER_PREFERENCE_FK`) REFERENCES `order_preference` (`ID`),
  CONSTRAINT `DEFAULT_ORDER_PREFERENCE_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forex`
--

DROP TABLE IF EXISTS `forex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forex` (
  `ID` int(11) NOT NULL,
  `BASE_CURRENCY` enum('CHF','EUR','USD','GBP') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FOREXIFKC` (`ID`),
  CONSTRAINT `FOREXIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forex_future`
--

DROP TABLE IF EXISTS `forex_future`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forex_future` (
  `ID` int(11) NOT NULL,
  `BASE_CURRENCY` enum('CHF','EUR','USD') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FOREX_FUTUREIFKC` (`ID`),
  CONSTRAINT `FOREX_FUTUREIFKC` FOREIGN KEY (`ID`) REFERENCES `future` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forex_future_family`
--

DROP TABLE IF EXISTS `forex_future_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forex_future_family` (
  `ID` int(11) NOT NULL,
  `BASE_CURRENCY` enum('CHF','EUR','USD') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FOREX_FUTURE_FAMILYIFKC` (`ID`),
  CONSTRAINT `FOREX_FUTURE_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `future_family` (`ID`)
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
  KEY `EXPIRATION` (`EXPIRATION`),
  KEY `FUTUREIFKC` (`ID`),
  CONSTRAINT `FUTUREIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `future_family`
--

DROP TABLE IF EXISTS `future_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `future_family` (
  `ID` int(11) NOT NULL,
  `LENGTH` int(11) NOT NULL,
  `INTREST` double NOT NULL,
  `DIVIDEND` double NOT NULL,
  `MARGIN_PARAMETER` double NOT NULL,
  `EXPIRATION_TYPE` enum('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','NEXT_3_RD_MONDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') NOT NULL,
  `EXPIRATION_MONTHS` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FUTURE_FAMILYIFKC` (`ID`),
  CONSTRAINT `FUTURE_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `generic_future`
--

DROP TABLE IF EXISTS `generic_future`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `generic_future` (
  `ID` int(11) NOT NULL,
  `DURATION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `DURATION` (`DURATION`),
  KEY `GENERIC_FUTUREIFKC` (`ID`),
  CONSTRAINT `GENERIC_FUTUREIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `generic_future_family`
--

DROP TABLE IF EXISTS `generic_future_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `generic_future_family` (
  `ID` int(11) NOT NULL,
  `EXPIRATION_TYPE` enum('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `GENERIC_FUTURE_FAMILYIFKC` (`ID`),
  CONSTRAINT `GENERIC_FUTURE_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `implied_volatility`
--

DROP TABLE IF EXISTS `implied_volatility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `implied_volatility` (
  `ID` int(11) NOT NULL,
  `DURATION` int(11) NOT NULL,
  `MONEYNESS` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IMPLIED_VOLATILITYIFKC` (`ID`),
  CONSTRAINT `IMPLIED_VOLATILITYIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  CONSTRAINT `INTREST_RATEIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `measurement`
--

DROP TABLE IF EXISTS `measurement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measurement` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `DATE` datetime NOT NULL,
  `INT_VALUE` int(11) DEFAULT NULL,
  `DOUBLE_VALUE` double DEFAULT NULL,
  `MONEY_VALUE` decimal(15,5) DEFAULT NULL,
  `TEXT_VALUE` varchar(50) DEFAULT NULL,
  `BOOLEAN_VALUE` bit(1) DEFAULT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DATE_NAME_STRATEGY_UNIQUE` (`DATE`,`NAME`,`STRATEGY_FK`),
  KEY `MEASUREMENT_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `MEASUREMENT_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `natural_index`
--

DROP TABLE IF EXISTS `natural_index`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `natural_index` (
  `ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `NATURAL_INDEXIFKC` (`ID`),
  CONSTRAINT `NATURAL_INDEXIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_preference`
--

DROP TABLE IF EXISTS `order_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_preference` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL,
  `ORDER_TYPE` enum('MARKET','LIMIT','STOP','STOP_LIMIT','SLICING','TICKWISE_INCREMENTAL','VARIABLE_INCREMENTAL') NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME_UNIQUE` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `portfolio_value`
--

DROP TABLE IF EXISTS `portfolio_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_value` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DATE_TIME` datetime NOT NULL,
  `SECURITIES_CURRENT_VALUE` decimal(15,6) NOT NULL,
  `CASH_BALANCE` decimal(15,6) NOT NULL,
  `MAINTENANCE_MARGIN` decimal(15,6) DEFAULT NULL,
  `NET_LIQ_VALUE` decimal(15,6) NOT NULL,
  `LEVERAGE` double NOT NULL,
  `ALLOCATION` double NOT NULL,
  `CASH_FLOW` decimal(15,6) DEFAULT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `PORTFOLIO_VALUE_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `PORTFOLIO_VALUE_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
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
  `EXIT_VALUE` decimal(15,5) DEFAULT NULL,
  `MAINTENANCE_MARGIN` decimal(15,2) DEFAULT NULL,
  `PERSISTENT` bit(1) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SECURITY_STRATEGY_UNIQUE` (`SECURITY_FK`,`STRATEGY_FK`),
  KEY `POSITION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `POSITION_STRATEGY_FKC` (`STRATEGY_FK`),
  KEY `QUANTITY` (`QUANTITY`),
  CONSTRAINT `POSITION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `POSITION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `PERSISTENT` bit(1) NOT NULL,
  `INT_VALUE` int(11) DEFAULT NULL,
  `DOUBLE_VALUE` double DEFAULT NULL,
  `MONEY_VALUE` decimal(15,5) DEFAULT NULL,
  `TEXT_VALUE` varchar(255) DEFAULT NULL,
  `DATE_VALUE` datetime DEFAULT NULL,
  `BOOLEAN_VALUE` bit(1) DEFAULT NULL,
  `PROPERTY_HOLDER_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security`
--

DROP TABLE IF EXISTS `security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `SYMBOL` varchar(30) DEFAULT NULL,
  `ISIN` varchar(20) DEFAULT NULL,
  `RIC` varchar(20) DEFAULT NULL,
  `CONID` varchar(30) DEFAULT NULL,
  `UNDERLYING_FK` int(11) DEFAULT NULL,
  `SECURITY_FAMILY_FK` int(11) NOT NULL,
  `INTREST_RATE_FAMILY_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `SYMBOL_UNIQUE` (`SYMBOL`),
  UNIQUE KEY `ISIN_UNIQUE` (`ISIN`),
  UNIQUE KEY `RIC_UNIQUE` (`RIC`),
  UNIQUE KEY `CONID_UNIQUE` (`CONID`),
  KEY `SECURITY_SECURITY_FAMILY_FKC` (`SECURITY_FAMILY_FK`),
  KEY `SECURITY_UNDERLYING_FKC` (`UNDERLYING_FK`),
  KEY `SECURITY_INTREST_RATE_FAMILY_FKC` (`INTREST_RATE_FAMILY_FK`),
  CONSTRAINT `SECURITY_INTREST_RATE_FAMILY_FKC` FOREIGN KEY (`INTREST_RATE_FAMILY_FK`) REFERENCES `security_family` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `SECURITY_SECURITY_FAMILY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `SECURITY_UNDERLYING_FKC` FOREIGN KEY (`UNDERLYING_FK`) REFERENCES `security` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security_family`
--

DROP TABLE IF EXISTS `security_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_family` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) NOT NULL,
  `BASE_SYMBOL` varchar(20) DEFAULT NULL,
  `RIC_ROOT` varchar(20) DEFAULT NULL,
  `MARKET` enum('SOFFEX','DTB','IDEALPRO','CBOE','SMART','CFE','GLOBEX','NYMEX','CME','NYSE','NASDAQ') NOT NULL,
  `CURRENCY` enum('CHF','EUR','USD') NOT NULL,
  `CONTRACT_SIZE` int(11) NOT NULL,
  `SCALE` int(11) NOT NULL,
  `TICK_SIZE_PATTERN` varchar(20) NOT NULL,
  `EXECUTION_COMMISSION` decimal(12,5) DEFAULT NULL,
  `CLEARING_COMMISSION` decimal(12,5) DEFAULT NULL,
  `MARKET_OPEN` time NOT NULL,
  `MARKET_CLOSE` time NOT NULL,
  `TRADEABLE` bit(1) NOT NULL,
  `SIMULATABLE` bit(1) NOT NULL,
  `SYNTHETIC` bit(1) NOT NULL,
  `SPREAD_SLOPE` double DEFAULT NULL,
  `SPREAD_CONSTANT` double DEFAULT NULL,
  `MAX_SPREAD_SLOPE` double DEFAULT NULL,
  `MAX_SPREAD_CONSTANT` double DEFAULT NULL,
  `PERIODICITY` enum('MINUTE','HOUR','DAY') DEFAULT NULL,
  `UNDERLYING_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `SECURITY_FAMILY_UNDERLAYING_FC` (`UNDERLYING_FK`),
  CONSTRAINT `SECURITY_FAMILY_UNDERLAYING_FC` FOREIGN KEY (`UNDERLYING_FK`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock`
--

DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock` (
  `ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `STOCKIFKC` (`ID`),
  CONSTRAINT `STOCKIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_option`
--

DROP TABLE IF EXISTS `stock_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_option` (
  `ID` int(11) NOT NULL,
  `STRIKE` decimal(12,5) NOT NULL,
  `EXPIRATION` datetime NOT NULL,
  `TYPE` enum('CALL','PUT') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `EXPIRATION` (`EXPIRATION`),
  KEY `STOCK_OPTIONIFKC` (`ID`),
  KEY `STRIKE` (`STRIKE`),
  KEY `TYPE` (`TYPE`),
  CONSTRAINT `STOCK_OPTIONIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
  `STRIKE_DISTANCE` double NOT NULL,
  `INTREST` double NOT NULL,
  `DIVIDEND` double NOT NULL,
  `MARGIN_PARAMETER` double NOT NULL,
  `EXPIRATION_TYPE` enum('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `STOCK_OPTION_FAMILYIFKC` (`ID`),
  CONSTRAINT `STOCK_OPTION_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
  `AUTO_ACTIVATE` bit(1) NOT NULL,
  `ALLOCATION` double NOT NULL,
  `INIT_MODULES` varchar(255) DEFAULT NULL,
  `RUN_MODULES` varchar(255) DEFAULT NULL,
  `VERSION` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `NAME_UNIQUE` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscription` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `PERSISTENT` bit(1) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `STRATEGY_SECURITY_UNIQUE` (`SECURITY_FK`,`STRATEGY_FK`),
  KEY `PERSISTENT` (`PERSISTENT`),
  KEY `SUBSCRIPTION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `SUBSCRIPTION_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `SUBSCRIPTION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `SUBSCRIPTION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `synthetic_index`
--

DROP TABLE IF EXISTS `synthetic_index`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `synthetic_index` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `clazz` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `SYNTHETIC_INDEXIFKC` (`ID`),
  CONSTRAINT `SYNTHETIC_INDEXIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  `LAST` decimal(12,5) DEFAULT NULL,
  `LAST_DATE_TIME` datetime DEFAULT NULL,
  `VOL` int(11) NOT NULL,
  `VOL_BID` int(11) NOT NULL,
  `VOL_ASK` int(11) NOT NULL,
  `BID` decimal(12,5) NOT NULL,
  `ASK` decimal(12,5) NOT NULL,
  `OPEN_INTREST` int(11) NOT NULL,
  `SETTLEMENT` decimal(12,5) DEFAULT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `DATE_TIME_SECURITY_UNIQUE` (`DATE_TIME`,`SECURITY_FK`),
  KEY `DATE_TIME` (`DATE_TIME`),
  KEY `MARKET_DATA_EVENT_SECURITY_FKC36519d` (`SECURITY_FK`)
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
  `EXT_ID` varchar(30) DEFAULT NULL,
  `DATE_TIME` datetime NOT NULL,
  `QUANTITY` bigint(20) NOT NULL,
  `PRICE` decimal(15,5) NOT NULL,
  `EXECUTION_COMMISSION` decimal(15,2) DEFAULT NULL,
  `CLEARING_COMMISSION` decimal(15,2) DEFAULT NULL,
  `CURRENCY` enum('CHF','EUR','USD','GBP') NOT NULL,
  `TYPE` enum('BUY','SELL','EXPIRATION','CREDIT','DEBIT','INTREST_PAID','INTREST_RECEIVED','FEES','REFUND','REBALANCE','TRANSFER') NOT NULL,
  `MARKET_CHANNEL` enum('IB_NATIVE','IB_FIX','JPM_FIX','RBS_MANUAL') DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `SECURITY_FK` int(11) DEFAULT NULL,
  `STRATEGY_FK` int(11) DEFAULT NULL,
  `POSITION_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `DATE_TIME_PRICE_TYPE_DESCRIPTION_UNIQUE` (`DATE_TIME`,`PRICE`,`TYPE`,`DESCRIPTION`),
  UNIQUE KEY `EXT_ID_UNIQUE` (`EXT_ID`),
  KEY `CURRENCY` (`CURRENCY`),
  KEY `DATE_TIME` (`DATE_TIME`),
  KEY `TRANSACTION_POSITION_FKC` (`POSITION_FK`),
  KEY `TRANSACTION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `TRANSACTION_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `TRANSACTION_POSITION_FKC` FOREIGN KEY (`POSITION_FK`) REFERENCES `position` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `TRANSACTION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `TRANSACTION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-01-30 19:03:23
