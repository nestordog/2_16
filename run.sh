#!/bin/sh

cd /usr/local/AlgoTrader

CP=webroot/WEB-INF/classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

java -cp $CP com.algoTrader.util.ServiceInvoker $*

