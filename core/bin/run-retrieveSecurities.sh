#!/bin/sh

# Usage:
# run-retrieveSecurities.sh 2:3:4:5

cd "`dirname \"$0\"`"/..

java \
-cp `cat bin/cp.txt` \
-DstrategyName=SERVER \
-Dspring.profiles.active=securityRetrieval \
ch.algotrader.starter.SecurityRetrievalStarter $*
