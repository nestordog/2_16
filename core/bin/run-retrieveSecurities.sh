#!/bin/sh

# Usage:
# run-retrieveSecurities.sh 2:3:4:5

cd $ALGOTRADER_HOME

java \
-cp `cat bin/cp.txt` \
-DstrategyName=BASE \
-Dspring.profiles.active=securityRetrieval \
ch.algotrader.starter.SecurityRetrievalStarter $*
