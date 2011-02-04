#!/bin/sh

cd /usr/share/AlgoTrader

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

java -cp $CP -Dsimulation=true -DdataSource.dataSet=1year -DdataSource.url=jdbc:mysql://127.0.0.1:3306/algotrader com.algoTrader.starter.SimulationStarter simulateWithCurrentParams

