#!/bin/sh

cd $ALGOTRADER_HOME

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

nohup java -cp $CP -DstrategyName=SMI -agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=localhost:8001 -Dcom.sun.management.jmxremote.port=1098 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:lib/org.springframework.instrument-3.0.5.RELEASE.jar com.algoTrader.service.theta.ThetaStarter > log/nohup.log 2>&1 &
