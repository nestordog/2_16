#!/bin/sh

cd $ALGOTRADER_HOME

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

nohup java \
-cp $CP \
-DstrategyName=BASE \
-Dcom.algoTrarder.rmi.registryPort=1099 \
-Dcom.algoTrarder.rmi.serverPort=1098 \
-Djava.rmi.server.hostname=127.0.0.1 \
-javaagent:lib/agent.jar \
-agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=localhost:8000 \
com.algoTrader.starter.TickStarter \
> log/nohup.log 2>&1 &
