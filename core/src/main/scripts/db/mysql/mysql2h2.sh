#!/bin/bash

DB_INSTANCE='algotrader'
DB_USER='root'
DB_PASSWORD='password'
DB_TABLES='property account strategy exchange trading_hours holiday security_family broker_parameters security security_reference component easy_to_borrow order order_preference order_property order_status portfolio_value position subscription transaction cash_balance tick bar generic_tick measurement'
OUT_FILE='../../../resources/db/h2/h2.sql'

mysqldump \
    --skip-triggers \
    --skip-set-charset \
    --no-data \
    --skip-set-charset \
    --skip-comments \
    --ignore-table=algotrader.schema_version \
    -u $DB_USER \
    -p$DB_PASSWORD \
    -r $OUT_FILE \
    $DB_INSTANCE \
    $DB_TABLES

sed -i "s/^\/\*.*\*\/;$//" $OUT_FILE
sed -i "s/enum(.*)/varchar(100)/" $OUT_FILE
sed -i "s/ENGINE.*;/;/" $OUT_FILE
sed -i "s/ENGINE.*;/;/" $OUT_FILE
sed -i -r "s/DROP TABLE IF EXISTS \`(.*)\`;/DROP TABLE IF EXISTS \"\1\";/" $OUT_FILE
sed -i -r "s/CREATE TABLE \`(.*)\`/CREATE TABLE \"\1\"/" $OUT_FILE
