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
base.test.linardcapital.com:1099 ^
macro.test.linardcapital.com:1097 ^
tailhedge.test.linardcapital.com:1095 ^
static.test.linardcapital.com:1093 ^
fixed.test.linardcapital.com:1091 ^
rollyield.test.linardcapital.com:1087 ^
endofday.test.linardcapital.com:1083
