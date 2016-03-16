#!/bin/bash

if [ -z "$1" ]; then
    DB_INSTANCE='algotrader';
else
    DB_INSTANCE="$1";
fi

OUT_FILE='h2-data.sql'

mysqldump \
    --no-create-info \
    --complete-insert \
    --skip-add-locks \
    --skip-disable-keys \
    --skip-extended-insert \
    --skip-triggers \
    --skip-set-charset \
    --skip-comments \
    --ignore-table=algotrader.schema_version \
    -u root \
    -p \
    -r $OUT_FILE \
    $DB_INSTANCE

sed -i "/^\/\*.*\*\/;$/ d" $OUT_FILE
sed -i "/^--.*$/ d" $OUT_FILE
sed -i -r "s/INSERT INTO \`(.*)\` [(]/INSERT INTO \"\1\" (/" $OUT_FILE
sed -i "s/'\\\0'/'0'/g" $OUT_FILE
sed -i "s/'\x01'/'1'/g" $OUT_FILE
