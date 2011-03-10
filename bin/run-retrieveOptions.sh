#!/bin/sh

cd $ALGOTRADER_HOME

CP=classes
for name in lib/*.jar ; do
  CP=$CP:$name
done

java -cp $CP \
-DstrategyName=BASE \
-DmarketChannel=SQ \
-DdataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader \
-Dlog4j.configuration=log4j-console.xml \
com.algoTrader.starter.StockOptionRetrievalStarter \
2 4
