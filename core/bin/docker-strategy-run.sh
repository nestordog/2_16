#!/bin/bash

case "$1" in
  "-e")
  
    # wait for MySql to be available
    until mysql -N -s -u $DATABASE_USER -p$DATABASE_PASSWORD -h $DATABASE_HOST $DATABASE_NAME -e "SHOW TABLES;" > /dev/null 2>&1; do sleep 1s; done

    # invoke flyway migrate (on first startup only in embedded mode only)
    if [ ! -f /usr/local/algotrader/flyway/INIT ]; then
      pushd /usr/local/algotrader/flyway
      flyway -url=jdbc:mysql://$DATABASE_HOST:$DATABASE_PORT migrate
      touch INIT
      popd
    fi

    # import db samples (only if database is empty, i.e. has no securities)
    if [[ `mysql -N -s -u $DATABASE_USER -p$DATABASE_PASSWORD -h $DATABASE_HOST $DATABASE_NAME -e "select count(id) from strategy where name = '$STRATEGY_NAME';"` == 0 ]]; then
      mysql -u $DATABASE_USER -p$DATABASE_PASSWORD -h $DATABASE_HOST $DATABASE_NAME < db/mysql/mysql-data.sql
	  echo "imported mysql data"
    fi
	
	# Start strategy in embedded mode
    exec java \
      -cp conf:lib/*:../algotrader/lib/* \
      -Dmisc.embedded=true \
      -DdataSource.url=jdbc:mysql://$DATABASE_HOST:$DATABASE_PORT/$DATABASE_NAME \
      -DdataSource.user=$DATABASE_USER \
      -DdataSource.password=$DATABASE_PASSWORD \
      -Dib.host=$IB_GATEWAY_HOST \
      -DstrategyName=$STRATEGY_NAME \
      -Dspring.profiles.active=$SPRING_PROFILES \
      $VM_ARGUMENTS \
      ch.algotrader.starter.EmbeddedStrategyStarter
    ;;
	
  "-d")
  
    # wait for the AlgoTrader server to be available
    until netcat -z algotrader 1199 > /dev/null 2>&1; do sleep 1s; done
    until netcat -z algotrader 61616 > /dev/null 2>&1; do sleep 1s; done
	
	# Start strategy in distributed mode
    exec java \
      -cp conf:lib/*:../algotrader/lib/* \
      -DremoteServer=$ALGOTRADER_HOST \
      -DactiveMQ.host=$ALGOTRADER_HOST \
	  -Dorg.apache.activemq.SERIALIZABLE_PACKAGES="*" \
      -DstrategyName=$STRATEGY_NAME \
      $VM_ARGUMENTS \
      ch.algotrader.starter.StrategyStarter
    ;;
	
  *)
    echo "unknown mode $1"
    exit 1
esac
