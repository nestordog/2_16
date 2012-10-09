#!/bin/sh

cd $ALGOTRADER_HOME

java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-Dspring.profiles.active=securityRetrieval \
com.algoTrader.starter.SecurityRetrievalStarter \
22 26 44

