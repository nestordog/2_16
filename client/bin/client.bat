@echo off
set mypath=%~dp0
cd %mypath:~0,-1%
cd ..

start javaw.exe ^
-cp lib\* ^
sun.tools.jconsole.JConsole ^
-interval=60 ^
localhost:1099 ^
