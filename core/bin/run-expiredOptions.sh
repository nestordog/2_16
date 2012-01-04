#!/bin/sh

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
com.algoTrader.starter.HistoricalDataStarter \
20101215 20110317 BID:ASK 11658:11625:11627:11629:11631:11633:11635:11637:11639:11641:11643:11645:11647:11649:11651:11653:11655:11657:11659:11661:11663:11665:11667:11669:11671:11673

# 20110121 20110218 TRADES 2:3:4:5
# 20101115 20110216 BID:ASK 11534:11536:11538

