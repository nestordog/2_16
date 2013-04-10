start javaw.exe ^
-classpath ..\target\*;^
..\lib\indicator-plugin.jar;^
%JAVA_HOME%\lib\jconsole.jar;^
%JAVA_HOME%\lib\tools.jar ^
-Djavax.net.ssl.keyStore=keystore ^
-Djavax.net.ssl.keyStorePassword=Zermatt11 ^
-Djavax.net.ssl.trustStore=truststore ^
-Djavax.net.ssl.trustStorePassword=Zermatt11 ^
sun.tools.jconsole.JConsole ^
-interval=60 ^
-debug ^
base.vola.linardcapital.com:1099 ^
macro.vola.linardcapital.com:1097 ^
tailhedge.vola.linardcapital.com:1095 ^
static.vola.linardcapital.com:1093 ^
fixed.vola.linardcapital.com:1091 ^
rollyield.vola.linardcapital.com:1087 ^
endofday.vola.linardcapital.com:1089
