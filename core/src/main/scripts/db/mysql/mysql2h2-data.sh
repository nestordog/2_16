#!/bin/bash
# batch script to convert mysql data files into h2 import file
# usage mysql-to-h2-data <input-file> <output-file>

# copy mysql data
echo 'SET REFERENTIAL_INTEGRITY FALSE;' > $2
cat $1 >> $2

sed -i "/^\/\*.*\*\/;$/ d" $2
sed -i "/^--.*$/ d" $2
sed -i -r "s/INSERT INTO \`(.*)\` [(]/INSERT INTO \"\1\" (/" $2
sed -i "s/'\\\0'/'0'/g" $2
sed -i "s/'\x01'/'1'/g" $2
