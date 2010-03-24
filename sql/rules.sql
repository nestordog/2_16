# SQL Manager 2005 for MySQL 3.7.0.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : AlgoTrader


SET FOREIGN_KEY_CHECKS=0;

#
# Structure for the `rule` table :
#

DROP TABLE IF EXISTS `rule`;

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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;

#
# Data for the `rule` table  (LIMIT 0,500)
#

INSERT INTO `rule` (`id`, `NAME`, `PRIORITY`, `DEFINITION`, `SUBSCRIBER`, `LISTENERS`, `PATTERN`, `ACTIVATABLE`, `PREPARED`, `TARGET_FK`) VALUES
  (1,'CREATE_EXIT_VALUE',4,'insert into ExitValue\r\nselect optionTick.security as security,\r\nStockOptionUtil.getExitValue(optionTick.security, indexTick.currentValueDouble, optionTick.currentValueDouble) as value\r\nfrom pattern [every (indexTick=Tick(security.isin = var_isin) -> optionTick=Tick(security.underlaying.id=indexTick.security.id))]\r\nwhere optionTick.security.position != null \r\nand optionTick.security.position.quantity != 0',NULL,NULL,False,True,False,NULL),
  (2,'CLOSE_POSITION',6,'select\r\ntick.security.position.id\r\nfrom Tick as tick\r\nwhere tick.security.position.quantity != 0\r\nand tick.security.position.exitValue != null\r\nand tick.currentValue  > tick.security.position.exitValue','ClosePositionSubscriber',NULL,False,True,False,NULL),
  (3,'SET_MARGIN',0,'every timer:at(0, 1, *, *, *)',NULL,'SetMarginListener',True,True,False,NULL),
  (4,'EXPIRE_POSITIONS',7,'select option.position.id as positionId\r\nfrom Tick as tick,\r\nmethod:LookupUtil.getStockOptionsOnWatchlist() as option \r\nwhere option.expiration.time < current_timestamp()\r\nand option.position != null\r\nand option.position.quantity != 0\r\n','ExpirePositionsSubscriber',NULL,False,True,False,NULL),
  (5,'GET_LAST_TICK',3,'select tick.security.id as securityId, tick.* as tick \r\nfrom Tick.std:groupby(security.id).win:length(1) as tick',NULL,NULL,False,True,False,NULL),
  (6,'SIMULATE_DUMMY_SECURITIES',5,'insert into Tick\r\nselect DateUtil.toDate(current_timestamp()) as dateTime,\r\nRoundUtil.getBigDecimal(StockOptionUtil.getFairValue(option, indexTick.currentValueDouble, volaTick.currentValueDouble / 100)) as last,\r\nDateUtil.toDate(current_timestamp()) as lastDateTime,\r\n0 as vol, 0 as volBid, 0 as volAsk, cast(0.0, BigDecimal) as bid, cast(0.0, BigDecimal) as ask, 0 as openIntrest, cast(0.0, BigDecimal) as settlement,\r\noption as security\r\nfrom pattern [every (indexTick=Tick(security.isin = var_isin) -> volaTick=Tick(security.id=indexTick.security.volatility.id))],\r\nmethod:LookupUtil.getDummySecurities() as option \r\nwhere option.underlaying.id = indexTick.security.id\r\nand var_simulation=true\r\n',NULL,NULL,False,True,False,NULL),
  (7,'OPEN_POSITION',1,'select stockOptionTick.security.id,\r\nstockOptionTick.settlement,\r\nstockOptionTick.currentValue,\r\nindexTick.currentValue\r\nfrom pattern [(indexTick=Tick(security.isin = var_isin) -> stockOptionTick=Tick(security.id = ?,security.underlaying.id=indexTick.security.id))]','OpenPositionSubscriber',NULL,False,False,True,NULL),
  (8,'PRINT_TICK',8,'select *\r\nfrom Tick','PrintTickSubscriber',NULL,False,True,False,NULL),
  (9,'RETRIEVE_TICKS',0,'every timer:at (*, 9:21, *, *, 1:5)',NULL,'RetrieveTickListener',True,True,False,NULL),
  (10,'CREATE_K_FAST',0,'insert into KFast\r\nselect security,\r\n(last(currentValueDouble) - min(currentValueDouble))/(max(currentValueDouble) - min(currentValueDouble)) as value\r\nfrom Tick.win:length(40 * var_events_per_hour)\r\nwhere security.isin = var_isin\r\nhaving max(currentValueDouble) != min(currentValueDouble)',NULL,NULL,False,True,False,NULL),
  (11,'CREATE_K_SLOW',0,'insert into KSlow\r\nselect security,\r\navg(value) as value\r\nfrom KFast.win:length(24 * var_events_per_hour)\r\nhaving avg(value) != null',NULL,NULL,False,True,False,NULL),
  (12,'CREATE_D_SLOW',0,'insert into DSlow\r\nselect security,\r\navg(value) as value\r\nfrom KSlow.win:length(24 * var_events_per_hour)\r\nhaving avg(value) != null',NULL,NULL,False,True,False,NULL),
  (13,'GET_BUY_SIGNAL',0,'select indexTick.security.id,\r\nindexTick.currentValue\r\nfrom pattern [every (indexTick=Tick(security.isin=var_isin) -> k=KSlow(security.id=indexTick.security.id) -> d=DSlow(security.id=indexTick.security.id))]\r\nwhere k.value > d.value\r\nand prior(1, k.value) < prior(1, d.value)\r\nand d.value < 0.3','BuySignalSubscriber',NULL,False,True,False,NULL),
  (14,'PRINT_STOCHASTIC',0,'select kFast.value,\r\nkSlow.value,\r\ndSlow.value\r\nfrom pattern [every (kFast=KFast -> kSlow=KSlow -> dSlow=DSlow)]','PrintStochasticSubscriber',NULL,False,True,False,NULL),
  (15,'IMMEDIATE_BUY_SIGNAL',0,'select security.id,\r\ncurrentValue\r\nfrom Tick(security.isin=var_isin).std:firstevent()','BuySignalSubscriber',NULL,False,False,False,NULL),
  (16,'SET_EXIT_VALUE',0,'select \r\nsecurity.position.id as positionId,\r\nRoundUtil.getBigDecimal(value) as exitValue\r\nfrom ExitValue\r\nwhere security.position.exitValue is null\r\nor value < security.position.exitValue\r\noutput last every 30 minutes','SetExitValueSubscriber',NULL,False,True,False,NULL),
  (17,'ROLL_POSITION',0,'select \r\noptionTick.security.position.id,\r\nindexTick.security.id,\r\nindexTick.currentValue\r\nfrom pattern [every (indexTick=Tick(security.isin = var_isin) -> optionTick=Tick(security.underlaying.id=indexTick.security.id))]\r\nwhere StockOptionUtil.isExpirationTimeToLong(optionTick.security, optionTick.currentValueDouble, optionTick.settlementDouble, indexTick.currentValueDouble)\r\nand optionTick.security.position != null\r\nand optionTick.security.position.quantity != 0','RollPositionSubscriber',NULL,False,False,False,NULL);

COMMIT;

