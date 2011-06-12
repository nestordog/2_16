#!/bin/sh

cd $ALGOTRADER_HOME

java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-DmarketChannel=SQ \
-DdataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader \
com.algoTrader.starter.StockOptionRetrievalStarter \
2 4
