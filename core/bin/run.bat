SET mypath=%~dp0
cd %mypath:~0,-1%
cd ..

java.exe ^
-cp "conf;lib/*" ^
-DstrategyName=SERVER ^
-Dcom.sun.management.jmxremote.port=1099 ^
-Dcom.sun.management.jmxremote.authenticate=false ^
-Dcom.sun.management.jmxremote.ssl=false ^
-Dspring.profiles.active=live,pooledDataSource,iBMarketData,iBNative,iBHistoricalDataa,embeddedBroker,html5 ^
ch.algotrader.starter.ServerStarter
