INSERT INTO `strategy` (`id`, `NAME`, `AUTO_ACTIVATE`, `ALLOCATION`, `INIT_MODULES`, `VERSION`) VALUES (${strategyId},'${serviceName.toUpperCase()}',0,1,'${artifactId}',0);

#foreach( $subscriptionId in $subscriptionIds.split(",") )
INSERT INTO `subscription` (`PERSISTENT`, `SECURITY_FK`, `STRATEGY_FK`, `FEED_TYPE`, `VERSION`) VALUES (0,${subscriptionId},${strategyId},'IB',0);
#end
