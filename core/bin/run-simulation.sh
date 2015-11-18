#!/bin/sh

# Usage:
# run-simulation.sh dataSetName simulateWithCurrentParams

cd "`dirname \"$0\"`"/..

nohup java \
-cp `cat bin/cp.txt` \
-Dsimulation=true \
-DdataSource.dataSet=$1 \
ch.algotrader.starter.SimulationStarter $2