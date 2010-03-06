# SQL Manager 2005 for MySQL 3.7.0.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : AlgoTrader


SET FOREIGN_KEY_CHECKS=0;

#
# Data for the `rule` table  (LIMIT 0,500)
#

INSERT INTO `rule` (`id`, `NAME`, `PRIORITY`, `DEFINITION`, `SUBSCRIBER`, `LISTENERS`, `PATTERN`, `ACTIVE`, `PREPARED`, `TARGET_FK`) VALUES
  (1,'SET_EXIT_VALUE',4,'select \r\noptionTick.security.position.id as positionId,\r\nStockOptionUtil.getExitValue(optionTick.security, indexTick.currentValue, optionTick.currentValue) as exitValue\r\nfrom pattern [every (indexTick=Tick(security.isin = var_isin) -> optionTick=Tick(security.underlaying.id=indexTick.security.id))]\r\nwhere optionTick.security.position != null \r\nand optionTick.security.position.quantity != 0\r\nand (cast(optionTick.security.position.exitValue,int) = 0 \r\n          or StockOptionUtil.getExitValue(optionTick.security, indexTick.currentValue, optionTick.currentValue) < optionTick.security.position.exitValue)\r\noutput last every 30 minutes\r\n','SetExitValueSubscriber',NULL,False,True,False,NULL),
  (2,'CLOSE_POSITION',6,'select\r\ntick.security.position.id\r\nfrom Tick as tick\r\nwhere cast(tick.security.position.exitValue,int) != 0\r\nand tick.currentValue  > tick.security.position.exitValue','ClosePositionSubscriber',NULL,False,True,False,NULL),
  (3,'SET_MARGIN',0,'every timer:at(0, 1, *, *, *)',NULL,'SetMarginListener',True,True,False,NULL),
  (4,'EXPIRE_OPTIONS',7,'select option.position.id as positionId\r\nfrom Tick as tick,\r\nmethod:LookupUtil.getStockOptionsOnWatchlist() as option \r\nwhere option.expiration.time < current_timestamp()\r\nand option.position != null\r\nand option.position.quantity != 0\r\n','ExpireStockOptionsSubscriber',NULL,False,True,False,NULL),
  (5,'GET_LAST_TICK',3,'select tick.security.id as securityId, tick.* as tick \r\nfrom Tick.std:groupby(security.id).win:length(1) as tick',NULL,NULL,False,True,False,NULL),
  (6,'SIMULATE_DUMMY_SECURITIES',5,'insert into Tick\r\nselect DateUtil.toDate(current_timestamp()) as dateTime,\r\nStockOptionUtil.getFairValue(option, indexTick.last, volaTick.last / 100) as last,\r\nDateUtil.toDate(current_timestamp()) as lastDateTime,\r\n0 as vol, 0 as volBid, 0 as volAsk, cast(0.0, BigDecimal) as bid, cast(0.0, BigDecimal) as ask, 0 as openIntrest, cast(0.0, BigDecimal) as settlement,\r\noption as security\r\nfrom pattern [every (indexTick=Tick(security.isin = var_isin) -> volaTick=Tick(security.id=indexTick.security.volatility.id))],\r\nmethod:LookupUtil.getDummySecurities() as option \r\nwhere option.underlaying.id = indexTick.security.id\r\nand var_simulation=true ',NULL,NULL,False,True,False,NULL),
  (7,'TIME_THE_MARKET',1,'select stockOptionTick.security.id,\r\nindexTick.security.id,\r\nindexTick.currentValue\r\nfrom pattern [(indexTick=Tick(security.isin = var_isin) -> stockOptionTick=Tick(security.id = ?,security.underlaying.id=indexTick.security.id))]\r\n','TimeTheMarketSubscriber',NULL,False,False,True,NULL),
  (8,'OPEN_POSITION',1,'select stockOptionTick.security.id,\r\nstockOptionTick.settlement,\r\nstockOptionTick.currentValue,\r\nindexTick.currentValue\r\nfrom pattern [(indexTick=Tick(security.isin = var_isin) -> stockOptionTick=Tick(security.id = ?,security.underlaying.id=indexTick.security.id))]','OpenPositionSubscriber',NULL,False,False,True,NULL),
  (10,'PRINT_TICK',8,'select *\r\nfrom Tick','PrintTickSubscriber',NULL,False,True,False,NULL),
  (11,'START_TIME_THE_MARKET',2,'select indexTick.security.id,\r\nindexTick.currentValue\r\nfrom Tick as indexTick\r\nwhere not LookupUtil.hasStockOptionsOnWatchlist()\r\nand indexTick.security.isin = var_isin','StartTimeTheMarketSubscriber',NULL,False,True,False,NULL),
  (12,'RETRIEVE_TICKS',0,'every timer:at (*, 9:21, *, *, 1:5,*/10)',NULL,'RetrieveTickListener',True,True,False,NULL);

COMMIT;

