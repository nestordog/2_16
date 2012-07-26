cd %ALGOTRADER_CLIENT_HOME%

rmdir target /s /q

mvn -U -f bin/pom.xml dependency:copy-dependencies -DoutputDirectory=..\target
