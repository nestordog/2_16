#!/bin/sh

cd $ALGOTRADER_HOME

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

java -cp $CP com.algoTrader.starter.AllCombinationOptimizer $*
