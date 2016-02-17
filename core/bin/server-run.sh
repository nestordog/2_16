#!/bin/bash

BASEDIR=`readlink --canonicalize-existing "$(dirname "$0")/.."`

RUNDIR=$BASEDIR/run
if [ ! -d $RUNDIR ]
    then
        mkdir $RUNDIR
    fi

PID=$RUNDIR/algotrade.pid
NOHUP=$RUNDIR/algotrade.nohup

COMMAND="$JAVA_HOME/bin/java -cp conf:lib/* \
 -DstrategyName=SERVER \
 -Dcom.sun.management.jmxremote.port=1099 \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.ssl=false \
 -Dspring.profiles.active=live,pooledDataSource,iBMarketData,iBNative,iBHistoricalDataa,embeddedBroker,html5 \
 ch.algotrader.starter.ServerStarter"

status() {
    if [ -f $PID ]
    then
        echo "Algotrader is running; PID: $(cat $PID)"
    else
        echo "Algotrader is not running"
    fi
}

start() {
    if [ -f $PID ]
    then
        echo "Algotrader is already running; PID: $(cat $PID)"
    else
        echo "Starting Algotrader"
        if [ -f $NOHUP ]
        then
            /bin/rm -f $NOHUP
        fi
        touch $PID
        if nohup $COMMAND >>$NOHUP 2>&1 &
        then
            echo $! >$PID
            echo "Algotrader is running"
        else
            echo "Algotrader failed to start"
            /bin/rm $PID
        fi
    fi
}

stop() {
    echo "Stopping Algotrader"
    if [ -f $PID ]
    then
        if kill `cat $PID`
        then
             echo "Algotrader stopped"
        fi
        /bin/rm -f $PID
    else
        echo "Algotrader is not running"
    fi
}

case "$1" in
    'start')
            start
            ;;
    'stop')
            stop
            ;;
    'restart')
            stop ; echo "Sleeping..."; sleep 1 ;
            start
            ;;
    'status')
            status
            ;;
    *)
            echo
            echo "Usage: $0 { start | stop | restart | status }"
            echo
            exit 1
            ;;
esac

exit 0