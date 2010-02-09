#!/bin/sh

cd /usr/local/AlgoTrader

svn update
/usr/local/bin/maven mda
/usr/local/bin/ant compile

/usr/local/etc/rc.d/algotrader.sh restart

