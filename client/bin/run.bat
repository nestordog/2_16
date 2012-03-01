echo off

cd %ALGOTRADER_CLIENT_HOME%

for /f %%x in (bin\cp.txt) do (set cp=%%x)

start javaw.exe ^
-classpath %cp%;^
%ALGOTRADER_CLIENT_HOME%\lib\indicator-plugin.jar;^
%JAVA_HOME%\lib\jconsole.jar;^
%JAVA_HOME%\lib\tools.jar ^
sun.tools.jconsole.JConsole ^
-interval=60 ^
-debug ^
localhost:1099 ^
localhost:1097 ^
localhost:1095
