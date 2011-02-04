#!/bin/sh

cd $ALGOTRADER_HOME

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

java -cp $CP -DstrategyName=BASE -agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=localhost:8000 -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false com.algoTrader.starter.TickStarter
