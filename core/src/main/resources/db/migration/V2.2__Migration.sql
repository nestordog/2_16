-- account
ALTER TABLE `account` MODIFY `BROKER` ENUM('IB','JPM','DC','RBS','RT','LMAX','FXCM','CNX') DEFAULT NULL;
ALTER TABLE `account` MODIFY `ORDER_SERVICE_TYPE` ENUM('IB_NATIVE','IB_FIX','JPM_FIX','DC_FIX','RT_FIX','LMAX_FIX','FXCM_FIX','CNX_FIX') NOT NULL;

-- bar
ALTER TABLE `bar` MODIFY `FEED_TYPE` ENUM('IB','BB','DC','LMAX','FXCM','CNX') DEFAULT NULL;

-- bond
ALTER TABLE `bond` MODIFY `MATURITY` date NOT NULL;

-- broker_parameters
ALTER TABLE `broker_parameters` MODIFY `BROKER` ENUM('IB','JPM','DC','RBS','RT','LMAX','FXCM','CNX') NOT NULL;
ALTER TABLE `broker_parameters` CHANGE `BASE_SYMBOL` `SYMBOL_ROOT` varchar(20) DEFAULT NULL;
ALTER TABLE `broker_parameters` CHANGE `MARKET` `EXCHANGE_CODE` varchar(20) DEFAULT NULL;

-- cash_balance
ALTER TABLE `cash_balance` MODIFY `CURRENCY` ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR') NOT NULL;

-- combination
ALTER TABLE `combination` ADD `UUID` char(36) DEFAULT NULL AFTER `ID`;
UPDATE `combination` SET `UUID` = UUID();
ALTER TABLE `combination` MODIFY `UUID` char(36) NOT NULL;

--easy_to_borrow
ALTER TABLE `easy_to_borrow` MODIFY `DATE` date NOT NULL;
ALTER TABLE `easy_to_borrow` MODIFY `BROKER` ENUM('IB','JPM','DC','RBS','RT','LMAX','FXCM','CNX') NOT NULL;

-- exchange
CREATE TABLE `exchange` (
    `ID` int(11) NOT NULL auto_increment,
    `NAME` varchar(50) NOT NULL,
    `CODE` varchar(10) NOT NULL,
    `TIME_ZONE` varchar(50) default NULL,
    UNIQUE INDEX `NAME` ( `NAME` ),
    PRIMARY KEY  ( `ID` )
) ENGINE=InnoDB CHARACTER SET=latin1 ROW_FORMAT=Compact;

-- forex
ALTER TABLE `forex` MODIFY `BASE_CURRENCY` ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR') NOT NULL;

-- future
ALTER TABLE `future` MODIFY `EXPIRATION` date NOT NULL;
ALTER TABLE `future` MODIFY `FIRST_NOTICE` date DEFAULT NULL;
ALTER TABLE `future` MODIFY `LAST_TRADING` date DEFAULT NULL;

-- generic future 
ALTER TABLE `generic_future` ADD `ASSET_CLASS` ENUM('EQUITY','VOLATILITY','COMMODITY','FIXED_INCOME','FX') DEFAULT 'EQUITY'; 
ALTER TABLE `generic_future` MODIFY `ASSET_CLASS` ENUM('EQUITY','VOLATILITY','COMMODITY','FIXED_INCOME','FX') NOT NULL;

-- generic_tick
CREATE TABLE `generic_tick` (
    `ID` int(11) NOT NULL,
    `DATE_TIME` datetime NOT NULL,
    `FEED_TYPE` enum('IB','BB','DC','LMAX','FXCM','CNX') NOT NULL,
    `SECURITY_FK` int(11) NOT NULL,
    `TICK_TYPE` enum('OPEN','HIGH','LOW','CLOSE','OPEN_INTEREST','IMBALANCE') NOT NULL,
    `MONEY_VALUE` decimal(13,6) default NULL,
    `DOUBLE_VALUE` double default NULL,
    `INT_VALUE` int(11) default NULL,
    KEY `GENERIC_TICK_SECURITY_FKC` ( `SECURITY_FK` ),
    PRIMARY KEY  ( `ID` )
) ENGINE=MyISAM CHARACTER SET=latin1 ROW_FORMAT=Fixed;

-- holiday
CREATE TABLE `holiday` (
    `ID` int(11) NOT NULL auto_increment,
    `DATE` date NOT NULL,
    `LATE_OPEN` time default NULL,
    `EARLY_CLOSE` time default NULL,
    `EXCHANGE_FK` int(11) NOT NULL,
    KEY `HOLIDAY_MARKET_FKC` ( `EXCHANGE_FK` ),
    PRIMARY KEY  ( `ID` )
) ENGINE=InnoDB CHARACTER SET=latin1 ROW_FORMAT=Compact;

ALTER TABLE `holiday` ADD CONSTRAINT `HOLIDAY_MARKET_FKC`
 FOREIGN KEY ( `EXCHANGE_FK` ) REFERENCES `exchange` ( `ID` ) ON UPDATE CASCADE;

-- index
ALTER TABLE `index` CHANGE `TYPE` `ASSET_CLASS` ENUM('EQUITY','VOLATILITY','COMMODITY','FIXED_INCOME','FX') NOT NULL;

-- measurement
ALTER TABLE `measurement` CHANGE `DATE` `DATE_TIME` datetime NOT NULL;

-- option
ALTER TABLE `option` MODIFY `EXPIRATION` date NOT NULL;

-- option_family
ALTER TABLE `option_family` MODIFY `MARGIN_PARAMETER` double NOT NULL;
ALTER TABLE `option_family` MODIFY `EXPIRATION_TYPE` ENUM('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') NOT NULL;
ALTER TABLE `option_family` MODIFY `EXPIRATION_DISTANCE` ENUM('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') NOT NULL;
ALTER TABLE `option_family` MODIFY `STRIKE_DISTANCE` double NOT NULL;
ALTER TABLE `option_family` ADD `WEEKLY` bit(1) DEFAULT '\0';
ALTER TABLE `option_family` MODIFY `WEEKLY` bit(1) NOT NULL;

-- order
CREATE TABLE `order` (
    `ID` int(11) NOT NULL auto_increment,
    `class` varchar(255) NOT NULL,
    `INT_ID` varchar(30) NOT NULL,
    `EXT_ID` varchar(30) default NULL,
    `DATE_TIME` datetime NOT NULL,
    `SIDE` enum('BUY','SELL','SELL_SHORT') NOT NULL,
    `QUANTITY` bigint(20) NOT NULL,
    `TIF` enum('DAY','GTC','GTD','IOC','FOK','ATO','ATC') NOT NULL,
    `TIF_DATE_TIME` datetime default NULL,
    `LIMIT` decimal(15,6) default NULL,
    `STOP` decimal(15,6) default NULL,
    `ACCOUNT_FK` int(11) NOT NULL,
    `SECURITY_FK` int(11) NOT NULL,
    `STRATEGY_FK` int(11) NOT NULL,
    KEY `ORDER_ACCOUNT_FKC` ( `ACCOUNT_FK` ),
    KEY `ORDER_SECURITY_FKC` ( `SECURITY_FK` ),
    KEY `ORDER_STRATEGY_FKC` ( `STRATEGY_FK` ),
    PRIMARY KEY  ( `ID` )
) ENGINE=InnoDB CHARACTER SET=latin1 ROW_FORMAT=Compact;

ALTER TABLE `order` ADD CONSTRAINT `ORDER_ACCOUNT_FKC`
 FOREIGN KEY ( `ACCOUNT_FK` ) REFERENCES `account` ( `ID` );

ALTER TABLE `order` ADD CONSTRAINT `ORDER_SECURITY_FKC`
 FOREIGN KEY ( `SECURITY_FK` ) REFERENCES `security` ( `ID` );

ALTER TABLE `order` ADD CONSTRAINT `ORDER_STRATEGY_FKC`
 FOREIGN KEY ( `STRATEGY_FK` ) REFERENCES `strategy` ( `ID` );

-- order_property
CREATE TABLE `order_property` (
    `ID` int(11) NOT NULL auto_increment,
    `NAME` varchar(30) NOT NULL,
    `VALUE` varchar(30) NOT NULL,
    `TYPE` enum('FIX','IB','INTERNAL') NOT NULL,
    `ORDER_FK` int(11) default NULL,
    KEY `ORDER_PROPERTY_ORDER_FKC` ( `ORDER_FK` ),
    PRIMARY KEY  ( `ID` )
) ENGINE=InnoDB CHARACTER SET=latin1 ROW_FORMAT=Compact;

ALTER TABLE `order_property` ADD CONSTRAINT `ORDER_PROPERTY_ORDER_FKC`
 FOREIGN KEY ( `ORDER_FK` ) REFERENCES `order` ( `ID` );

-- order_status
CREATE TABLE `order_status` (
    `ID` int(11) NOT NULL auto_increment,
    `DATE_TIME` datetime NOT NULL,
    `EXT_DATE_TIME` datetime NOT NULL,
    `STATUS` enum('OPEN','SUBMITTED','PARTIALLY_EXECUTED','EXECUTED','CANCELED','REJECTED') NOT NULL,
    `FILLED_QUANTITY` bigint(20) NOT NULL,
    `REMAINING_QUANTITY` bigint(20) NOT NULL,
    `AVG_PRICE` decimal(15,6) default NULL,
    `LAST_PRICE` decimal(15,6) default NULL,
    `INT_ID` varchar(30) default NULL,
    `EXT_ID` varchar(30) default NULL,
    `SEQUENCE_NUMBER` bigint(20) default NULL,
    `REASON` varchar(255) default NULL,
    `ORDER_FK` int(11) NOT NULL,
    KEY `ORDER_STATUS_ORDER_FKC` ( `ORDER_FK` ),
    PRIMARY KEY  ( `ID` )
) ENGINE=InnoDB CHARACTER SET=latin1 ROW_FORMAT=Compact;

ALTER TABLE `order_status` ADD CONSTRAINT `ORDER_STATUS_ORDER_FKC`
 FOREIGN KEY ( `ORDER_FK` ) REFERENCES `order` ( `ID` ) ON DELETE CASCADE ON UPDATE CASCADE;

-- property
ALTER TABLE `property` CHANGE `DATE_VALUE` `DATE_TIME_VALUE` datetime DEFAULT NULL;

-- security
ALTER TABLE `security` ADD `LMAXID` varchar(30) DEFAULT NULL AFTER `CONID`;

-- security family
ALTER TABLE `security_family` CHANGE `BASE_SYMBOL` `SYMBOL_ROOT` varchar(20) DEFAULT NULL;
ALTER TABLE `security_family` DROP `MARKET`;
ALTER TABLE `security_family` MODIFY `CURRENCY` ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR') NOT NULL;
ALTER TABLE `security_family` MODIFY `CONTRACT_SIZE` double NOT NULL;
ALTER TABLE `security_family` DROP `MARKET_OPEN_DAY`;
ALTER TABLE `security_family` DROP `MARKET_OPEN`;
ALTER TABLE `security_family` DROP `MARKET_CLOSE`;
ALTER TABLE `security_family` DROP `SPREAD_SLOPE`;
ALTER TABLE `security_family` DROP `SPREAD_CONSTANT`;
ALTER TABLE `security_family` DROP `MAX_SPREAD_SLOPE`;
ALTER TABLE `security_family` DROP `MAX_SPREAD_CONSTANT`;
ALTER TABLE `security_family` ADD `EXCHANGE_FK` int(11) DEFAULT NULL AFTER `UNDERLYING_FK`;

ALTER TABLE `security_family` ADD KEY `SECURITY_FAMILY_MARKET_FKC` ( `EXCHANGE_FK` );

ALTER TABLE `security_family` ADD CONSTRAINT `SECURITY_FAMILY_MARKET_FKC`
 FOREIGN KEY ( `EXCHANGE_FK` ) REFERENCES `exchange` ( `ID` );

-- strategy
UPDATE `strategy` 
SET `NAME` = 'SERVER', `INIT_MODULES` = 'market-data,combination,current-values,market-data-simulation,trades,portfolio,performance,algo-slicing,ib'
WHERE ID = 1;

-- subscription
ALTER TABLE `subscription` MODIFY `FEED_TYPE` ENUM('IB','BB','DC','LMAX','FXCM','CNX') NOT NULL;

-- tick
ALTER TABLE `tick` MODIFY `FEED_TYPE` ENUM('IB','BB','DC','LMAX','FXCM','CNX') DEFAULT NULL;

-- trading_hours
CREATE TABLE `trading_hours` (
    `ID` int(11) NOT NULL auto_increment,
    `OPEN` time NOT NULL,
    `CLOSE` time NOT NULL,
    `SUNDAY` bit(1) NOT NULL,
    `MONDAY` bit(1) NOT NULL,
    `TUESDAY` bit(1) NOT NULL,
    `WEDNESDAY` bit(1) NOT NULL,
    `THURSDAY` bit(1) NOT NULL,
    `FRIDAY` bit(1) NOT NULL,
    `SATURDAY` bit(1) NOT NULL,
    `EXCHANGE_FK` int(11) NOT NULL,
    PRIMARY KEY  ( `ID` ),
    KEY `TRADING_HOURS_EXCHANGE_FKC` ( `EXCHANGE_FK` )
) ENGINE = InnoDB CHARACTER SET = latin1 ROW_FORMAT = Compact;

ALTER TABLE `trading_hours` ADD CONSTRAINT `TRADING_HOURS_EXCHANGE_FKC`
 FOREIGN KEY ( `EXCHANGE_FK` ) REFERENCES `exchange` ( `ID` );

-- transaction
ALTER TABLE `transaction` ADD `INT_ORDER_ID` varchar(30) DEFAULT NULL AFTER `EXT_ID`;
ALTER TABLE `transaction` ADD `EXT_ORDER_ID` varchar(30) DEFAULT NULL AFTER `INT_ORDER_ID`;
ALTER TABLE `transaction` MODIFY `SETTLEMENT_DATE` date DEFAULT NULL;
ALTER TABLE `transaction` MODIFY `CURRENCY` ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR') NOT NULL;