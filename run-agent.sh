#!/bin/sh

cd /usr/local/AlgoTrader

CP=classes:lib/log4j-1.2.15.jar

java -cp $CP -Dlog4j.configuration=log4j-agent.xml org.apache.log4j.net.SocketServer 4560 classes/log4j-agent.xml .

