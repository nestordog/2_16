#!/bin/sh

# Usage:
# run-expiredOptions.sh 20110121 20110218 TRADES 2:3:4:5
# run-expiredOptions.sh 20101115 20110216 BID:ASK 11534:11536:11538

cd $ALGOTRADER_HOME

java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-DdataSource.dataSet=expiredOptions \
-Dib.historicalDataServiceEnabled=true \
-Dib.accountServiceEnabled=false \
-Dib.stockOptionRetrieverServiceEnabled=false \
-Dib.tickServiceEnabled=false \
-Dib.transactionServiceEnabled=false \
ch.algotrader.starter.HistoricalDataStarter $*
