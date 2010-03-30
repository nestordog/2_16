#!/bin/sh

cd /usr/local/AlgoTrader

/usr/local/etc/rc.d/algotrader.sh stop

svn update
/usr/local/bin/maven mda
/usr/local/bin/ant compile


