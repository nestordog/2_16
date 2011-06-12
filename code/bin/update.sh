#!/bin/sh

cd $ALGOTRADER_HOME

mvn -U -f bin/pom.xml \
dependency:build-classpath \
-Dmdep.outputFile=cp.txt
