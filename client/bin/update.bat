SET mypath=%~dp0
cd %mypath:~0,-1%
cd ..

rmdir target /s /q

mvn -U -f bin\pom.xml dependency:copy-dependencies -DoutputDirectory=..\lib
