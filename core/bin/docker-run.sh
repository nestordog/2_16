#!/bin/bash

until mysql -N -s -u root -ppassword -h mysql algotrader -e "SHOW TABLES;"
do
  sleep 1s
done

if [ ! -f flyway/INIT ]; then
  pushd flyway
  flyway -url=jdbc:mysql://$DATABASE_HOST:$DATABASE_PORT migrate
  touch INIT
  popd
fi

if [[ `mysql -N -s -u root -ppassword -h mysql algotrader -e "SELECT COUNT(id) FROM security;"` == 0 ]]; then
  mysql -u root -ppassword -h mysql algotrader < samples/db/mysql/mysql-data.sql
fi

exec java -cp conf:lib/* -DdataSource.url=jdbc:mysql://$DATABASE_HOST:$DATABASE_PORT/$DATABASE_NAME -Dib.host=$IB_GATEWAY_HOST -Dspring.profiles.active=$SPRING_PROFILES $VM_ARGUMENTS $STARTER_CLASS