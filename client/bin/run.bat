cd %ALGOTRADER_CLIENT_HOME%

for /f %%x in (bin\cp.txt) do (set cp=%%x)

start javaw.exe ^
-classpath %cp%;^
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
quant.linardcapital.com:1099 ^
quant.linardcapital.com:1097 ^
vola.linardcapital.com:1099 ^
vola.linardcapital.com:1097
