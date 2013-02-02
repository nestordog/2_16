cd %ALGOTRADER_CLIENT_HOME%

start javaw.exe ^
-classpath target\*;^
%ALGOTRADER_CLIENT_HOME%\lib\indicator-plugin.jar;^
%JAVA_HOME%\lib\jconsole.jar;^
%JAVA_HOME%\lib\tools.jar ^
-Djavax.net.ssl.keyStore=%ALGOTRADER_CLIENT_HOME%/bin/keystore ^
-Djavax.net.ssl.keyStorePassword=Zermatt11 ^
-Djavax.net.ssl.trustStore=%ALGOTRADER_CLIENT_HOME%/bin/truststore ^
-Djavax.net.ssl.trustStorePassword=Zermatt11 ^
sun.tools.jconsole.JConsole ^
-interval=60 ^
-debug ^
base.quant.linardcapital.com:1099 ^
swingset.quant.linardcapital.com:1097 ^
box1.quant.linardcapital.com:1095 ^
box2.quant.linardcapital.com:1093
