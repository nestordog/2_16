#!/bin/sh

cd "`dirname \"$0\"`"/..

nohup java \
-cp classes:`cat bin/cp.txt` \
-DstrategyName=BASE \
-Dcom.sun.management.jmxremote.port=1099 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dspring.profiles.active=server,pooledDataSource,iBMarketData,iBHistoricalData \
ch.algotrader.starter.MarketDataStarter \
> log/nohup.log 2>&1 &
