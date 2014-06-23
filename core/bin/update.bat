SET mypath=%~dp0
cd %mypath:~0,-1%
cd ..

del target\*.jar

mvn -U -f pom.xml dependency:copy-dependencies -DoutputDirectory=target
