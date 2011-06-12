#!/bin/sh

cd $ALGOTRADER_HOME

nohup java \
-cp `cat bin/cp.txt` \
 com.algoTrader.starter.AllCombinationOptimizer $*
