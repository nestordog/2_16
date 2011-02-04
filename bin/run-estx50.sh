#!/bin/sh

cd $ALGOTRADER_HOME

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

nohup java \
-cp $CP \
-DstrategyName=ESTX50 \
-Dcom.algoTrarder.rmi.registryPort=1095 \
-Dcom.algoTrarder.rmi.serverPort=1094 \
-Djava.rmi.server.hostname=127.0.0.1 \
-javaagent:lib/agent.jar \
-javaagent:lib/org.springframework.instrument-3.0.5.RELEASE.jar \
-agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=localhost:8002 \
com.algoTrader.service.theta.ThetaStarter \
> log/nohup.log 2>&1 &
