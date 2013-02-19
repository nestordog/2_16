rmdir ..\target /s /q

mvn -U -f pom.xml dependency:copy-dependencies -DoutputDirectory=..\target
