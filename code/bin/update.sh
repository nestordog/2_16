#!/bin/sh

cd $ALGOTRADER_HOME

mvn -f bin/pom.xml \
dependency:build-classpath \
-Dmdep.outputFile=cp.txt
