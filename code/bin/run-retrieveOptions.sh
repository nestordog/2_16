#!/bin/sh

cd $ALGOTRADER_HOME

mvn -o -q -f bin/pom.xml \
dependency:build-classpath \
-Dmdep.outputFile=cp.txt

nohup java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-DmarketChannel=SQ \
-DdataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader \
-Dlog4j.configuration=log4j-console.xml \
-agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=localhost:8000 \
com.algoTrader.starter.StockOptionRetrievalStarter \
2 4
