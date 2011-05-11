#!/bin/sh

cd $ALGOTRADER_HOME

mvn -o -q -f bin/pom.xml \
dependency:build-classpath \
-Dmdep.outputFile=cp.txt

nohup java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-Dcom.algoTrarder.rmi.registryPort=1099 \
-Dcom.algoTrarder.rmi.serverPort=1098 \
-Djava.rmi.server.hostname=127.0.0.1 \
-javaagent:lib/agent.jar \
-agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=localhost:8000 \
-Xmx150m \
-XX:MaxHeapFreeRatio=10 \
-XX:MinHeapFreeRatio=5 \
-XX:-UseParallelGC \
com.algoTrader.starter.TickStarter \
> log/nohup.log 2>&1 &

