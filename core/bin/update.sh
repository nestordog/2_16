#!/bin/sh

cd "`dirname \"$0\"`"/..

mvn -U -f bin/pom.xml dependency:copy-dependencies -DoutputDirectory=../lib
