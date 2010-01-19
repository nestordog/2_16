# SQL Manager 2005 for MySQL 3.7.0.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : AlgoTrader


SET FOREIGN_KEY_CHECKS=0;

USE `AlgoTrader`;

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
  `ACTIVE` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;

#
# Data for the `rule` table  (LIMIT 0,500)
#

INSERT INTO `rule` (`id`, `NAME`, `PRIORITY`, `DEFINITION`, `SUBSCRIBER`, `LISTENERS`, `PATTERN`, `ACTIVE`) VALUES
  (1,'setExitValue',0,'select \r\noption.position.id as positionId,\r\nStockOptionUtil.getExitValue(option, indexTick.last, volaTick.last / 100) as exitValue\r\nfrom pattern [every (indexTick=Tick(security.volatility!=null) -> volaTick=Tick(security.id=indexTick.security.volatility.id) where timer:within(2 min))],\r\nmethod:EntityUtil.getSecuritiesInPortfolio() as option \r\nwhere option.underlaying.id = indexTick.security.id \r\nand StockOptionUtil.getExitValue(option, indexTick.last, volaTick.last / 100) < option.position.exitValue\r\noutput last every 30 minutes\r\n','SetExitValueSubscriber',NULL,False,False),
  (2,'closePosition',0,'select  \r\noptionTick.security.position\r\nfrom Tick as optionTick\r\nwhere optionTick.volBid > 10 and optionTick.volAsk > 10\r\nand (optionTick.bid + optionTick.ask)/2  > optionTick.security.position.exitValue','ClosePositionSubscriber',NULL,False,False),
  (3,'printTick',0,'select *\r\nfrom Tick','PrintTickSubscriber',NULL,False,True),
  (7,'setMargin',0,'every timer:at(0, 1, *, *, *)\r\n\r\n//select DateUtil.toDate(current_timestamp()), \r\n//optionTick.security.position as position\r\n//from pattern [every (optionTick=Tick(security.underlaying!=null) -> indexTick=Tick(security.id=optionTick.security.underlaying.id) where timer:within(2 min))]\r\n//where optionTick.security.position.quantity != 0\r\n//output last at(0, 1, *, *, *)',NULL,'SetMarginListener',True,False),
  (8,'expireOptions',2,'every timer:at(0, 14, *, *, *)',NULL,'ExpireStockOptionsListener',True,False),
  (9,'getLastTick',0,'select tick.security.id as securityId, tick.* as tick \r\nfrom Tick.std:groupby(security.id).win:length(1) as tick',NULL,NULL,False,True),
  (10,'simulateDummySecurities',1,'insert into Tick\r\nselect DateUtil.toDate(current_timestamp()) as dateTime,\r\nStockOptionUtil.getFairValue(option, indexTick.last, volaTick.last / 100) as last,\r\nDateUtil.toDate(current_timestamp()) as lastDateTime,\r\n0 as vol, 0 as volBid, 0 as volAsk, cast(0.0, BigDecimal) as bid, cast(0.0, BigDecimal) as ask, 0 as openIntrest, cast(0.0, BigDecimal) as settlement,\r\noption as security\r\nfrom pattern [every (indexTick=Tick(security.isin = var_isin) -> volaTick=Tick(security.id=indexTick.security.volatility.id))],\r\nmethod:LookupUtil.getDummySecurities() as option \r\nwhere option.underlaying.id = indexTick.security.id ',NULL,NULL,False,True),
  (11,'putOnWatchlist\r\n',0,'select tick.security.isin,\r\nStockOptionUtil.roundTo50(tick.currentValue) as current\r\nfrom Tick(tick.security.isin = var_isin) as tick\r\nwhere StockOptionUtil.roundTo50(tick.currentValue) not in (LookupUtil.getStrikesOnWatchlist())','PutOnWatchlistSubscriber\r\n',NULL,False,False),
  (12,'timeTheMarket',0,'select indexTick.security.id,\r\nindexTick.currentValue\r\nfrom pattern [(stockOptionTick=Tick(security.id = {0}) -> indexTick=Tick(security.id=stockOptionTick.security.underlaying.id))]\r\n\r\n// TODO implement markettiming. At the moment the first event showing up is selected','TimeTheMarketSubscriber',NULL,False,False),
  (13,'openPosition',0,'select security.id,\r\ncurrentValue\r\nfrom Tick(security.id={0}).std:firstevent()','OpenPositionSubscriber',NULL,False,False);

COMMIT;

