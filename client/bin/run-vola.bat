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
base.vola.linardcapital.com:1099 ^
macro.vola.linardcapital.com:1097 ^
static.vola.linardcapital.com:1095 ^
fixed.vola.linardcapital.com:1093 ^
atr.vola.linardcapital.com:1091 ^
tailhedge.vola.linardcapital.com:1089