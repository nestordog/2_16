cd %ALGOTRADER_CLIENT_HOME%

mvn -U -f bin/pom.xml ^
dependency:build-classpath ^
-Dmdep.outputFile=cp.txt
