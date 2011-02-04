#!/bin/sh

cd /usr/share/AlgoTrader

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

java -cp $CP -Dsimulation=true -DdataSource.dataSet=1year com.algoTrader.starter.SimulationStarter simulateWithCurrentParams

