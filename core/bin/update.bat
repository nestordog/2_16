SET mypath=%~dp0
cd %mypath:~0,-1%

mvn -U -f pom.xml dependency:copy-dependencies -DoutputDirectory=..\target
