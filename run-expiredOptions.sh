#!/bin/sh

cd /usr/local/AlgoTrader

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

## java -cp $CP -DstrategyName=BASE -DdataSource.dataSet=expiredOptions -Dib.historicalDataServiceEnabled=true -Dib.accountServiceEnabled=false -Dib.stockOptionRetrieverServiceEnabled=false -Dib.tickServiceEnabled=false -Dib.transactionServiceEnabled=false -Dlog4j.configuration=log4j-console.xml com.algoTrader.starter.HistoricalDataStarter 20101018 20110119 BID:ASK 11087:11089:11091:11093:11095:11097:11099:11101:11103:11105
## java -cp $CP -DstrategyName=BASE -DdataSource.dataSet=expiredOptions -Dib.historicalDataServiceEnabled=true -Dib.accountServiceEnabled=false -Dib.stockOptionRetrieverServiceEnabled=false -Dib.tickServiceEnabled=false -Dib.transactionServiceEnabled=false -Dlog4j.configuration=log4j-console.xml com.algoTrader.starter.HistoricalDataStarter 20101220 20110119 TRADES 4:5

## java -cp $CP -DstrategyName=BASE -DdataSource.dataSet=expiredOptions -Dib.historicalDataServiceEnabled=true -Dib.accountServiceEnabled=false -Dib.stockOptionRetrieverServiceEnabled=false -Dib.tickServiceEnabled=false -Dib.transactionServiceEnabled=false -Dlog4j.configuration=log4j-console.xml com.algoTrader.starter.HistoricalDataStarter 20101018 20110120 BID:ASK 11439:11441:11443:11445:11447:11449:11451:11453
java -cp $CP -DstrategyName=BASE -DdataSource.dataSet=expiredOptions -Dib.historicalDataServiceEnabled=true -Dib.accountServiceEnabled=false -Dib.stockOptionRetrieverServiceEnabled=false -Dib.tickServiceEnabled=false -Dib.transactionServiceEnabled=false -Dlog4j.configuration=log4j-console.xml com.algoTrader.starter.HistoricalDataStarter 20101220 20110120 TRADES 2:3

