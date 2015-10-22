SET mypath=%~dp0
cd %mypath:~0,-1%
cd ..

del target\*.jar

mvn -U -f bin\pom.xml dependency:copy-dependencies -DoutputDirectory=..\lib
