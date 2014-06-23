#!/bin/sh

cd "`dirname \"$0\"`"/..

mvn -U -f bin/pom.xml dependency:build-classpath -Dmdep.outputFile=cp.txt
