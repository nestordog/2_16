#!/bin/sh

cd "`dirname \"$0\"`"/..

nohup java \
-cp classes:`cat bin/cp.txt` \
-DstrategyName=BASE \
-Dch.algotrader.rmi.registryPort=1099 \
-Dch.algotrader.rmi.serverPort=1098 \
-Djava.rmi.server.hostname=$EXT_HOSTNAME \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=true \
-Dcom.sun.management.jmxremote.ssl.need.client.auth=true \
-Dcom.sun.management.jmxremote.registry.ssl=true \
-Djavax.net.ssl.keyStore=$ALGOTRADER_HOME/bin/keystore \
-Djavax.net.ssl.keyStorePassword=... \
-Djavax.net.ssl.trustStore=$ALGOTRADER_HOME/bin/truststore \
-Djavax.net.ssl.trustStorePassword=... \
-Dlog4j.configuration=log4j-prod.xml \
-Denvironment=$ENVIRONMENT \
-javaagent:bin/agent.jar \
-XX:MaxHeapFreeRatio=10 \
-XX:MinHeapFreeRatio=5 \
-XX:-UseParallelGC \
-Dspring.profiles.active=server,pooledDataSource,iBMarketData,iBHistoricalData \
ch.algotrader.starter.MarketDataStarter \
> log/nohup.log 2>&1 &
