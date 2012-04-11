#!/bin/sh

cd $ALGOTRADER_HOME

java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-Dib.accountServiceEnabled=false \
-Dib.historicalDataServiceEnabled=false \
-Dib.securityRetrieverServiceEnabled=true \
com.algoTrader.starter.SecurityRetrievalStarter \
22 26

