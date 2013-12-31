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
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) NOT NULL,
  `ACTIVE` bit(1) NOT NULL,
  `BROKER` enum('IB','JPM','DC','RBS','RT') DEFAULT NULL,
  `ORDER_SERVICE_TYPE` enum('IB_NATIVE','IB_FIX','JPM_FIX','DC_FIX','RT_FIX') NOT NULL,
  `SESSION_QUALIFIER` varchar(10) DEFAULT NULL,
  `EXT_ACCOUNT` varchar(20) DEFAULT NULL,
  `EXT_ACCOUNT_GROUP` varchar(20) DEFAULT NULL,
  `EXT_ALLOCATION_PROFILE` varchar(20) DEFAULT NULL,
  `EXT_CLEARING_ACCOUNT` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME` (`NAME`),
  KEY `EXT_ACCOUNT` (`EXT_ACCOUNT`),
  KEY `EXT_ACCOUNT_GROUP` (`EXT_ACCOUNT_GROUP`),
  KEY `EXT_CLEARING_ACCOUNT` (`EXT_CLEARING_ACCOUNT`),
  KEY `EXT_ALLOCATION_PROFILE` (`EXT_ALLOCATION_PROFILE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `allocation`
--

DROP TABLE IF EXISTS `allocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `allocation` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `VALUE` double NOT NULL,
  `ORDER_PREFERENCE_FK` int(11) DEFAULT NULL,
  `ACCOUNT_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ORDER_PREFERENCE_ACCOUNT_UNIQUE` (`ORDER_PREFERENCE_FK`,`ACCOUNT_FK`),
  KEY `ALLOCATION_ACCOUNT_FKC` (`ACCOUNT_FK`),
  KEY `ALLOCATION_ORDER_PREFERENCE_FC` (`ORDER_PREFERENCE_FK`),
  CONSTRAINT `ALLOCATION_ACCOUNT_FKC` FOREIGN KEY (`ACCOUNT_FK`) REFERENCES `account` (`ID`),
  CONSTRAINT `ALLOCATION_ORDER_PREFERENCE_FC` FOREIGN KEY (`ORDER_PREFERENCE_FK`) REFERENCES `order_preference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `OPEN` decimal(13,6) NOT NULL,
  `HIGH` decimal(13,6) NOT NULL,
  `LOW` decimal(13,6) NOT NULL,
  `CLOSE` decimal(13,6) NOT NULL,
  `VOL` int(11) NOT NULL,
  `BAR_SIZE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DATE_TIME_SECURITY_BAR_SIZE_UNIQUE` (`DATE_TIME`,`SECURITY_FK`,`BAR_SIZE`),
  KEY `MARKET_DATA_EVENT_SECURITY_FKC17c13` (`SECURITY_FK`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bond`
--

DROP TABLE IF EXISTS `bond`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bond` (
  `ID` int(11) NOT NULL,
  `MATURITY` datetime NOT NULL,
  `COUPON` double NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `COUPON` (`COUPON`),
  KEY `BONDIFKC` (`ID`),
  CONSTRAINT `BONDIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bond_family`
--

DROP TABLE IF EXISTS `bond_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bond_family` (
  `ID` int(11) NOT NULL,
  `MATURITY_DISTANCE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL,
  `LENGTH` int(11) NOT NULL,
  `QUOTATION_STYLE` enum('PRICE','YIELD') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `BOND_FAMILYIFKC` (`ID`),
  CONSTRAINT `BOND_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `broker_parameters`
--

DROP TABLE IF EXISTS `broker_parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `broker_parameters` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `BROKER` enum('IB','JPM','DC','RBS') NOT NULL,
  `BASE_SYMBOL` varchar(20) DEFAULT NULL,
  `MARKET` varchar(20) DEFAULT NULL,
  `EXECUTION_COMMISSION` decimal(5,2) DEFAULT NULL,
  `CLEARING_COMMISSION` decimal(5,2) DEFAULT NULL,
  `SECURITY_FAMILY_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `BROKER_PARAMETERS_SECURITY_FAC` (`SECURITY_FAMILY_FK`),
  CONSTRAINT `BROKER_PARAMETERS_SECURITY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cash_balance`
--

DROP TABLE IF EXISTS `cash_balance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cash_balance` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CURRENCY` enum('USD','EUR','CHF','GBP','CAD','JPY','AUD','NZD','HKD','SEK','NOK') NOT NULL,
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
-- Table structure for table `commodity`
--

DROP TABLE IF EXISTS `commodity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commodity` (
  `ID` int(11) NOT NULL,
  `TYPE` enum('ENERGY','INDUSTRIAL_METALS','PRECIOUS_METALS','AGRICULTURE','LIVESTOCK') NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `TYPE` (`TYPE`),
  KEY `COMMODITYIFKC` (`ID`),
  CONSTRAINT `COMMODITYIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
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
  `COMBINATION_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `SECURITY_COMBINATION_UNIQUE` (`SECURITY_FK`,`COMBINATION_FK`),
  KEY `COMPONENT_SECURITY_FKC` (`SECURITY_FK`),
  KEY `COMPONENT_COMBINATION_FK` (`COMBINATION_FK`),
  CONSTRAINT `COMPONENT_COMBINATION_FK` FOREIGN KEY (`COMBINATION_FK`) REFERENCES `combination` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,
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
  KEY `DEFAULT_ORDER_PREFERENCE_ORDEC` (`ORDER_PREFERENCE_FK`),
  KEY `DEFAULT_ORDER_PREFERENCE_SECUC` (`SECURITY_FAMILY_FK`),
  KEY `DEFAULT_ORDER_PREFERENCE_STRAC` (`STRATEGY_FK`),
  CONSTRAINT `DEFAULT_ORDER_PREFERENCE_ORDER_PREFERENCE_FKC` FOREIGN KEY (`ORDER_PREFERENCE_FK`) REFERENCES `order_preference` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `DEFAULT_ORDER_PREFERENCE_SECURITY_FAMILY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `DEFAULT_ORDER_PREFERENCE_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `easy_to_borrow`
--

DROP TABLE IF EXISTS `easy_to_borrow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `easy_to_borrow` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DATE` datetime NOT NULL,
  `BROKER` varchar(255) NOT NULL COMMENT 'The {@link Broker} which these parameters apply to.',
  `QUANTITY` bigint(20) NOT NULL,
  `STOCK_FK` int(11) NOT NULL COMMENT 'A Single Stock',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DATE_BROKER_STOCK_UNIQUE` (`DATE`,`BROKER`,`STOCK_FK`),
  KEY `EASY_TO_BORROW_STOCK_FKC` (`STOCK_FK`),
  CONSTRAINT `EASY_TO_BORROW_STOCK_FKC` FOREIGN KEY (`STOCK_FK`) REFERENCES `stock` (`ID`)
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
  `BASE_CURRENCY` enum('USD','EUR','CHF','GBP','CAD','JPY','AUD','NZD','HKD','SEK','NOK') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FOREXIFKC` (`ID`),
  CONSTRAINT `FOREXIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fund`
--

DROP TABLE IF EXISTS `fund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fund` (
  `ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FUNDIFKC` (`ID`),
  CONSTRAINT `FUNDIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`)
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
  `FIRST_NOTICE` datetime DEFAULT NULL,
  `LAST_TRADING` datetime DEFAULT NULL,
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
  `INTREST` double NOT NULL,
  `DIVIDEND` double NOT NULL,
  `MARGIN_PARAMETER` double NOT NULL,
  `EXPIRATION_TYPE` enum('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','NEXT_3_RD_MONDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') NOT NULL,
  `EXPIRATION_DISTANCE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL,
  `LENGTH` int(11) NOT NULL,
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
  `DURATION` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL,
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
  `EXPIRATION_DISTANCE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') DEFAULT NULL,
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
  `DURATION` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL,
  `MONEYNESS` double(2,2) DEFAULT NULL,
  `DELTA` double(2,2) DEFAULT NULL,
  `TYPE` enum('CALL','PUT') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IMPLIED_VOLATILITYIFKC` (`ID`),
  CONSTRAINT `IMPLIED_VOLATILITYIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `index`
--

DROP TABLE IF EXISTS `index`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `index` (
  `ID` int(11) NOT NULL,
  `TYPE` enum('EQUITY','VOLATILITY','COMMODITY') NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `NATURAL_INDEXIFKC` (`ID`),
  CONSTRAINT `NATURAL_INDEXIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
  `DURATION` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL,
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
  `MONEY_VALUE` decimal(15,6) DEFAULT NULL,
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
-- Table structure for table `option`
--

DROP TABLE IF EXISTS `option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `option` (
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
-- Table structure for table `option_family`
--

DROP TABLE IF EXISTS `option_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `option_family` (
  `ID` int(11) NOT NULL,
  `INTREST` double NOT NULL,
  `DIVIDEND` double NOT NULL,
  `MARGIN_PARAMETER` double DEFAULT NULL,
  `EXPIRATION_TYPE` enum('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') DEFAULT NULL,
  `EXPIRATION_DISTANCE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') DEFAULT NULL,
  `STRIKE_DISTANCE` double DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `STOCK_OPTION_FAMILYIFKC` (`ID`),
  CONSTRAINT `STOCK_OPTION_FAMILYIFKC` FOREIGN KEY (`ID`) REFERENCES `security_family` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
  `ORDER_TYPE` enum('MARKET','LIMIT','STOP','STOP_LIMIT','SLICING','TICKWISE_INCREMENTAL','VARIABLE_INCREMENTAL','DISTRIBUTIONAL') NOT NULL,
  `DEFAULT_ACCOUNT_FK` int(11) DEFAULT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME_UNIQUE` (`NAME`),
  KEY `ORDER_PREFERENCE_DEFAULT_ACCOC` (`DEFAULT_ACCOUNT_FK`),
  CONSTRAINT `ORDER_PREFERENCE_DEFAULT_ACCOC` FOREIGN KEY (`DEFAULT_ACCOUNT_FK`) REFERENCES `account` (`ID`)
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
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `QUANTITY` bigint(20) NOT NULL,
  `COST` double NOT NULL,
  `REALIZED_P_L` double DEFAULT NULL,
  `EXIT_VALUE` decimal(15,6) DEFAULT NULL,
  `MAINTENANCE_MARGIN` decimal(15,2) DEFAULT NULL,
  `PERSISTENT` bit(1) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
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
  `MONEY_VALUE` decimal(15,6) DEFAULT NULL,
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
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `SYMBOL` varchar(30) DEFAULT NULL,
  `ISIN` varchar(20) DEFAULT NULL,
  `BBGID` varchar(12) DEFAULT NULL,
  `RIC` varchar(20) DEFAULT NULL,
  `CONID` varchar(30) DEFAULT NULL,
  `UNDERLYING_FK` int(11) DEFAULT NULL,
  `SECURITY_FAMILY_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CONID_UNIQUE` (`CONID`),
  UNIQUE KEY `ISIN_UNIQUE` (`ISIN`),
  UNIQUE KEY `RIC_UNIQUE` (`RIC`),
  UNIQUE KEY `SYMBOL_SECURITY_FAMILY_UNIQUE` (`SYMBOL`,`SECURITY_FAMILY_FK`),
  KEY `SECURITY_SECURITY_FAMILY_FKC` (`SECURITY_FAMILY_FK`),
  KEY `SECURITY_UNDERLYING_FKC` (`UNDERLYING_FK`),
  CONSTRAINT `SECURITY_SECURITY_FAMILY_FKC` FOREIGN KEY (`SECURITY_FAMILY_FK`) REFERENCES `security_family` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `SECURITY_UNDERLYING_FKC` FOREIGN KEY (`UNDERLYING_FK`) REFERENCES `security` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security_family`
--

DROP TABLE IF EXISTS `security_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_family` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) NOT NULL,
  `BASE_SYMBOL` varchar(20) DEFAULT NULL,
  `ISIN_ROOT` varchar(20) DEFAULT NULL,
  `RIC_ROOT` varchar(20) DEFAULT NULL,
  `MARKET` varchar(20) NOT NULL,
  `CURRENCY` enum('USD','EUR','CHF','GBP','CAD','JPY','AUD','NZD','HKD','SEK','NOK') NOT NULL,
  `TRADING_CLASS` varchar(20) DEFAULT NULL,
  `CONTRACT_SIZE` int(11) NOT NULL,
  `SCALE` int(11) NOT NULL,
  `TICK_SIZE_PATTERN` varchar(100) NOT NULL,
  `EXECUTION_COMMISSION` decimal(5,2) DEFAULT NULL,
  `CLEARING_COMMISSION` decimal(5,2) DEFAULT NULL,
  `MARKET_OPEN_DAY` enum('SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') DEFAULT NULL,
  `MARKET_OPEN` time NOT NULL,
  `MARKET_CLOSE` time NOT NULL,
  `TRADEABLE` bit(1) NOT NULL,
  `SYNTHETIC` bit(1) NOT NULL,
  `SPREAD_SLOPE` double DEFAULT NULL,
  `SPREAD_CONSTANT` double DEFAULT NULL,
  `MAX_SPREAD_SLOPE` double DEFAULT NULL,
  `MAX_SPREAD_CONSTANT` double DEFAULT NULL,
  `PERIODICITY` enum('MIN','HOUR','DAY') DEFAULT NULL,
  `MAX_GAP` int(11) DEFAULT NULL,
  `UNDERLYING_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `SECURITY_FAMILY_UNDERLAYING_FC` (`UNDERLYING_FK`),
  CONSTRAINT `SECURITY_FAMILY_UNDERLAYING_FC` FOREIGN KEY (`UNDERLYING_FK`) REFERENCES `security` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
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
  `GICS` char(8) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `STOCKIFKC` (`ID`),
  CONSTRAINT `STOCKIFKC` FOREIGN KEY (`ID`) REFERENCES `security` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `strategy`
--

DROP TABLE IF EXISTS `strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `strategy` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(30) NOT NULL,
  `AUTO_ACTIVATE` bit(1) NOT NULL,
  `ALLOCATION` double NOT NULL,
  `INIT_MODULES` varchar(255) DEFAULT NULL,
  `RUN_MODULES` varchar(255) DEFAULT NULL,
  `VERSION` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
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
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `PERSISTENT` bit(1) NOT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  `STRATEGY_FK` int(11) NOT NULL,
  `VERSION` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `STRATEGY_SECURITY_UNIQUE` (`SECURITY_FK`,`STRATEGY_FK`),
  KEY `PERSISTENT` (`PERSISTENT`),
  KEY `SUBSCRIPTION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `SUBSCRIPTION_STRATEGY_FKC` (`STRATEGY_FK`),
  CONSTRAINT `SUBSCRIPTION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `SUBSCRIPTION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tick`
--

DROP TABLE IF EXISTS `tick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tick` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DATE_TIME` datetime NOT NULL,
  `LAST` decimal(13,6) DEFAULT NULL,
  `LAST_DATE_TIME` datetime DEFAULT NULL,
  `VOL` int(11) NOT NULL,
  `VOL_BID` int(11) NOT NULL,
  `VOL_ASK` int(11) NOT NULL,
  `BID` decimal(13,6) DEFAULT NULL,
  `ASK` decimal(13,6) DEFAULT NULL,
  `SECURITY_FK` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
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
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `EXT_ID` varchar(30) DEFAULT NULL,
  `DATE_TIME` datetime NOT NULL,
  `SETTLEMENT_DATE` datetime DEFAULT NULL,
  `QUANTITY` bigint(20) NOT NULL,
  `PRICE` decimal(15,6) NOT NULL,
  `EXECUTION_COMMISSION` decimal(15,2) DEFAULT NULL,
  `CLEARING_COMMISSION` decimal(15,2) DEFAULT NULL,
  `CURRENCY` enum('USD','EUR','CHF','GBP','CAD','JPY','AUD','NZD','HKD','SEK','NOK') NOT NULL,
  `TYPE` enum('BUY','SELL','EXPIRATION','CREDIT','DEBIT','INTREST_PAID','INTREST_RECEIVED','FEES','REFUND','REBALANCE','TRANSFER') NOT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `SECURITY_FK` int(11) DEFAULT NULL,
  `STRATEGY_FK` int(11) DEFAULT NULL,
  `POSITION_FK` int(11) DEFAULT NULL,
  `ACCOUNT_FK` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DATE_TIME_PRICE_TYPE_DESCRIPTION_UNIQUE` (`DATE_TIME`,`PRICE`,`TYPE`,`DESCRIPTION`),
  UNIQUE KEY `EXT_ID_UNIQUE` (`EXT_ID`),
  KEY `CURRENCY` (`CURRENCY`),
  KEY `DATE_TIME` (`DATE_TIME`),
  KEY `TRANSACTION_POSITION_FKC` (`POSITION_FK`),
  KEY `TRANSACTION_SECURITY_FKC` (`SECURITY_FK`),
  KEY `TRANSACTION_STRATEGY_FKC` (`STRATEGY_FK`),
  KEY `TRANSACTION_ACCOUNT_FKC` (`ACCOUNT_FK`),
  CONSTRAINT `TRANSACTION_ACCOUNT_FKC` FOREIGN KEY (`ACCOUNT_FK`) REFERENCES `account` (`ID`),
  CONSTRAINT `TRANSACTION_POSITION_FKC` FOREIGN KEY (`POSITION_FK`) REFERENCES `position` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `TRANSACTION_SECURITY_FKC` FOREIGN KEY (`SECURITY_FK`) REFERENCES `security` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `TRANSACTION_STRATEGY_FKC` FOREIGN KEY (`STRATEGY_FK`) REFERENCES `strategy` (`ID`) ON UPDATE CASCADE
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

-- Dump completed on 2013-12-31 11:27:30
