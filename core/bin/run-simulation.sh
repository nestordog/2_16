#!/bin/sh

# Usage:
# run-simulation.sh dataSetName simulateWithCurrentParams

cd $ALGOTRADER_HOME

nohup java \
-cp `cat bin/cp.txt` \
-Dsimulation=true \
-DdataSource.dataSet=$1 \
com.algoTrader.starter.SimulationStarter $2
