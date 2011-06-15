# SQL Manager 2005 for MySQL 3.7.0.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : algotrader


SET FOREIGN_KEY_CHECKS=0;

#
# Data for the `security_family` table  (LIMIT 0,500)
#

INSERT INTO `security_family` (`id`, `NAME`, `MARKET`, `CURRENCY`, `CONTRACT_SIZE`, `TICK_SIZE`, `COMMISSION`, `MARKET_OPEN`, `MARKET_CLOSE`, `TRADEABLE`, `SIMULATABLE`, `SPREAD_SLOPE`, `SPREAD_CONSTANT`, `MAX_SPREAD_SLOPE`, `MAX_SPREAD_CONSTANT`, `PERIODICITY`, `UNDERLAYING_FK`) VALUES
  (1,'SMI','SOFFEX','CHF',1,0.1,0,'09:00:00','17:20:00',False,False,NULL,NULL,NULL,NULL,'MINUTE',NULL),
  (2,'VSMI','DTB','CHF',1,0.01,NULL,'09:00:00','17:20:00',False,False,NULL,NULL,NULL,NULL,'MINUTE',4),
  (3,'OSMI','SOFFEX','CHF',10,0.1,2.8,'09:00:00','17:20:00',True,True,0.033,11.7,0.037,25,'MINUTE',4),
  (4,'ESTX50','DTB','EUR',1,0.01,NULL,'09:00:00','17:30:00',False,False,NULL,NULL,NULL,NULL,'MINUTE',NULL),
  (5,'V2TX','DTB','EUR',1,0.01,NULL,'09:00:00','17:30:00',False,False,NULL,NULL,NULL,NULL,'MINUTE',2),
  (6,'OESX','DTB','EUR',10,0.1,2.8,'09:00:00','17:30:00',True,True,0.0133,6.7265,0.015,20,'MINUTE',2),
  (7,'DEPOSIT RATE CHF','IDEALPRO','CHF',1,0.01,NULL,'09:00:00','17:30:00',False,False,NULL,NULL,NULL,NULL,'DAY',NULL),
  (8,'CHF FOREX','IDEALPRO','CHF',1,0.0001,2E-5,'09:00:00','18:00:00',True,False,NULL,NULL,NULL,NULL,'HOUR',NULL),
  (9,'USD FOREX','IDEALPRO','USD',1,0.0001,2E-5,'09:00:00','18:00:00',True,False,NULL,NULL,NULL,NULL,'HOUR',NULL),
  (10,'FSMI','SOFFEX','CHF',10,1,4,'07:50:00','22:00:00',True,True,0.0003,0,0.037,25,'MINUTE',4),
  (11,'SPY','SMART','USD',1,0.01,0,'15:30:00','22:00:00',True,False,NULL,NULL,NULL,NULL,'MINUTE',NULL),
  (12,'GFES','GLOBEX','USD',1,0.01,0,'08:00:00','18:00:00',False,False,NULL,NULL,NULL,NULL,'HOUR',NULL),
  (13,'GFLH','CME','USD',1,0.01,0,'08:00:00','18:00:00',False,False,NULL,NULL,NULL,NULL,'HOUR',NULL),
  (14,'GFNG','NYMEX','USD',1,0.01,0,'08:00:00','18:00:00',False,False,NULL,NULL,NULL,NULL,'HOUR',NULL),
  (20,'VIX','CBOE','USD',1,0.01,NULL,'15:30:00','22:00:00',False,False,NULL,NULL,NULL,NULL,'MINUTE',NULL),
  (21,'UX','CFE','USD',1000,0.05,1.85,'13:20:00','21:15:00',True,False,NULL,NULL,NULL,NULL,'MINUTE',NULL);

COMMIT;

#
# Data for the `security` table  (LIMIT 0,500)
#

INSERT INTO `security` (`id`, `ISIN`, `SYMBOL`, `UNDERLAYING_FK`, `VOLATILITY_FK`, `SECURITY_FAMILY_FK`, `INTREST_RATE_FAMILY_FK`) VALUES
  (2,'DE0009652396','ESTX50',NULL,3,4,NULL),
  (3,'DE000A0C3QF1','V2TX',NULL,NULL,5,NULL),
  (4,'CH0008616382','SMI',NULL,5,1,7),
  (5,'CH0019900841','V3X',NULL,NULL,7,NULL),
  (6,'CH0007475228','CHFDOMDEP1W',NULL,NULL,6,NULL),
  (7,'CH0007473702','CHFDOMDEP1M',NULL,NULL,6,NULL),
  (8,'EU0009654078','EUR.CHF',NULL,NULL,8,NULL),
  (9,'XC0009652816','USD.CHF',NULL,NULL,8,NULL),
  (10,'EU0009652759','EUR.USD',NULL,NULL,9,NULL),
  (11,'GB0031973075','GBP.USD',NULL,NULL,9,NULL),
  (12,'SPY','SPY',NULL,NULL,11,NULL),
  (13,'GFES1M','GFES1M',NULL,NULL,12,NULL),
  (14,'GFLH1M','GFLH1M',NULL,NULL,13,NULL),
  (15,'GFNG1M','GFNG1M',NULL,NULL,14,NULL),
  (20,'VIX','VIX',NULL,NULL,20,NULL),
  (21,'UX1','UX1',NULL,NULL,21,NULL),
  (22,'UX2','UX2',NULL,NULL,21,NULL),
  (23,'UX3','UX3',NULL,NULL,21,NULL),
  (24,'UX4','UX4',NULL,NULL,21,NULL),
  (25,'UX5','UX5',NULL,NULL,21,NULL),
  (26,'UX6','UX6',NULL,NULL,21,NULL),
  (27,'UX7','UX7',NULL,NULL,21,NULL),
  (28,'UX8','UX8',NULL,NULL,21,NULL);

COMMIT;

#
# Data for the `forex` table  (LIMIT 0,500)
#

INSERT INTO `forex` (`ID`, `BASE_CURRENCY`) VALUES
  (8,'EUR'),
  (9,'USD'),
  (10,'EUR'),
  (11,'GBP');

COMMIT;

#
# Data for the `future_family` table  (LIMIT 0,500)
#

INSERT INTO `future_family` (`ID`, `INTREST`, `DIVIDEND`, `MARGIN_PARAMETER`) VALUES
  (10,0.0015,0.03,490.9);

COMMIT;

#
# Data for the `generic_future` table  (LIMIT 0,500)
#

INSERT INTO `generic_future` (`ID`, `DURATION`) VALUES
  (13,1),
  (14,1),
  (15,1),
  (21,1),
  (22,2),
  (23,3),
  (24,4),
  (25,5),
  (26,6),
  (27,7),
  (28,8);

COMMIT;

#
# Data for the `intrest_rate` table  (LIMIT 0,500)
#

INSERT INTO `intrest_rate` (`ID`, `DURATION`) VALUES
  (6,604800000),
  (7,2592000000);

COMMIT;

#
# Data for the `strategy` table  (LIMIT 0,500)
#

INSERT INTO `strategy` (`id`, `NAME`, `FAMILY`, `AUTO_ACTIVATE`, `ALLOCATION`, `MODULES`) VALUES
  (0,'BASE','BASE',True,0,'base');
UPDATE `strategy` SET `id`=0 WHERE `id`=LAST_INSERT_ID();
INSERT INTO `strategy` (`id`, `NAME`, `FAMILY`, `AUTO_ACTIVATE`, `ALLOCATION`, `MODULES`) VALUES
  (1,'SMI','THETA',True,1,'theta-init,theta-main'),
  (2,'ESTX50','THETA',False,0,'theta-init,theta-main'),
  (3,'MULTIIND','MULTIIND',False,0,'multiind-movavcross,multiind-main'),
  (4,'EASTWEST','EASTWEST',False,0,'volcarry-main');

COMMIT;

#
# Data for the `stock_option_family` table  (LIMIT 0,500)
#

INSERT INTO `stock_option_family` (`ID`, `STRIKE_DISTANCE`, `INTREST`, `DIVIDEND`, `MARGIN_PARAMETER`) VALUES
  (3,50,0.0015,0.03,0.075),
  (6,50,0.006,0.03,0.075);

COMMIT;

#
# Data for the `transaction` table  (LIMIT 0,500)
#

INSERT INTO `transaction` (`id`, `NUMBER`, `DATE_TIME`, `QUANTITY`, `PRICE`, `COMMISSION`, `CURRENCY`, `TYPE`, `DESCRIPTION`, `SECURITY_FK`, `STRATEGY_FK`, `POSITION_FK`) VALUES
  (1,NULL,'1990-01-01',1,1000000,0,'EUR','CREDIT',NULL,NULL,NULL,NULL);

COMMIT;

#
# Data for the `watch_list_item` table  (LIMIT 0,500)
#

INSERT INTO `watch_list_item` (`id`, `PERSISTENT`, `SECURITY_FK`, `STRATEGY_FK`) VALUES
  (1,True,2,2),
  (2,True,3,2),
  (3,True,4,1),
  (4,True,8,2),
  (5,True,5,1),
  (6,True,12,3),
  (7,True,13,3),
  (8,True,14,3),
  (9,True,15,3),
  (10,True,20,4),
  (11,True,21,4),
  (12,True,22,4),
  (13,True,23,4),
  (14,True,24,4),
  (15,True,25,4),
  (16,True,26,4),
  (17,True,27,4),
  (18,True,28,4);

COMMIT;

