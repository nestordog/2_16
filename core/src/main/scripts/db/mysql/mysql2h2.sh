#!/bin/bash
# batch script to convert mysql db structure & data into an h2 import file
# needs environment variable ALGOTRADER_HOME

if [ -z "$1" ]; then
    DB_INSTANCE='algotrader';
else
    DB_INSTANCE="$1";
fi

OUT_FILE='h2.sql'

mysqldump \
    --skip-triggers \
    --skip-set-charset \
    --no-data \
    --skip-set-charset \
    --skip-comments \
    --ignore-table=algotrader.schema_version \
    -u root \
    -p \
    -r $OUT_FILE \
    $DB_INSTANCE

sed -i "s/^\/\*.*\*\/;$//" $OUT_FILE
sed -i "s/enum(.*)/varchar(100)/" $OUT_FILE
sed -i "s/ENGINE.*;/;/" $OUT_FILE
sed -i "s/ENGINE.*;/;/" $OUT_FILE
sed -i -r "s/DROP TABLE IF EXISTS \`(.*)\`;/DROP TABLE IF EXISTS \"\1\";/" $OUT_FILE
sed -i -r "s/CREATE TABLE \`(.*)\`/CREATE TABLE \"\1\"/" $OUT_FILE
