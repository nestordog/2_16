#!/bin/sh

cd $ALGOTRADER_HOME

nohup java \
-cp `cat bin/cp.txt` \
-Dsimulation=true \
-DdataSource.dataSet=1year \
com.algoTrader.starter.SimulationStarter \
simulateWithCurrentParams

