#!/bin/sh

cd $ALGOTRADER_HOME

nohup java \
-cp lib/*:`cat bin/cp.txt` \
-DstrategyName=BASE \
-Dcom.algoTrarder.rmi.registryPort=1099 \
-Dcom.algoTrarder.rmi.serverPort=1098 \
-Djava.rmi.server.hostname=127.0.0.1 \
-Dlog4j.configuration=log4j-prod.xml \
-Denvironment=$ENVIRONMENT \
-javaagent:lib/agent.jar \
-XX:MaxHeapFreeRatio=10 \
-XX:MinHeapFreeRatio=5 \
-XX:-UseParallelGC \
com.algoTrader.starter.MarketDataStarter \
> log/nohup.log 2>&1 &

