#!/bin/sh

mvn -U -f bin/pom.xml \
dependency:build-classpath \
-Dmdep.outputFile=cp.txt
