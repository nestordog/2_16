SET mypath=%~dp0
cd %mypath:~0,-1%
cd ..

start javaw.exe ^
-classpath target\*;^
lib\indicator-plugin.jar;^
%JAVA_HOME%\lib\jconsole.jar;^
%JAVA_HOME%\lib\tools.jar ^
sun.tools.jconsole.JConsole ^
-interval=60 ^
localhost:1099 ^
