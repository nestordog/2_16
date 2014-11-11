SET mypath=%~dp0
cd %mypath:~0,-1%
cd ..

java.exe ^
-classpath target\* ^
-DstrategyName=SERVER ^
-Dcom.sun.management.jmxremote.port=1099 ^
-Dcom.sun.management.jmxremote.authenticate=false ^
-Dcom.sun.management.jmxremote.ssl=false ^
-Dspring.profiles.active=server,pooledDataSource,iBMarketData,iBHistoricalData ^
ch.algotrader.starter.MarketDataStarter
