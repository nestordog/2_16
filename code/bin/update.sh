#!/bin/sh

cd $ALGOTRADER_HOME

mvn -o -q -f bin/pom.xml \
dependency:build-classpath \
-Dmdep.outputFile=cp.txt
