#!/bin/bash
# batch script to convert mysql db structure & data into an h2 import file
# needs environment variable ALGOTRADER_HOME

BASEDIR=$(dirname $0)
cd $BASEDIR

# reset database 
mvn -o -f "$(cygpath -m -l $ALGOTRADER_HOME/core/pom.xml)" flyway:clean flyway:migrate

# drop structure
mysqldump.exe \
--skip-triggers \
--skip-set-charset \
--no-data \
--ignore-table=algotrader.schema_version \
-u root \
-ppassword -r \
h2-structure.sql \
algotrader

# clean structure
sed -i "/^\/\*.*\*\/;$/ d" h2-structure.sql
sed -i "/^--.*$/ d" h2-structure.sql
sed -i "s/enum(.*)/varchar(100)/" h2-structure.sql
sed -i "s/ENGINE.*;/;/" h2-structure.sql
sed -i "s/ENGINE.*;/;/" h2-structure.sql
sed -i -r "s/DROP TABLE IF EXISTS \`(.*)\`;/DROP TABLE IF EXISTS \"\1\";/" h2-structure.sql
sed -i -r "s/CREATE TABLE \`(.*)\`/CREATE TABLE \"\1\"/" h2-structure.sql
sed -i "s/KEY \`.*\` [(]/KEY (/" h2-structure.sql
sed -i -r "s/REFERENCES \`(.*)\` [(]/REFERENCES \"\1\" (/" h2-structure.sql

# copy structure with constraints
cp h2-structure.sql h2-structure-constraint.sql

# remove constraints
sed -i "/^.*CONSTRAINT.*$/ d" h2-structure.sql

# drop data
mysqldump.exe \
--complete-insert \
--no-create-info \
--skip-add-locks \
--skip-disable-keys \
--skip-extended-insert \
--skip-triggers \
--ignore-table=algotrader.schema_version \
-u root \
-ppassword \
-r h2-data.sql \
algotrader

# clean data
sed -i "/^\/\*.*\*\/;$/ d" h2-data.sql
sed -i "/^--.*$/ d" h2-data.sql
sed -i -r "s/INSERT INTO \`(.*)\` [(]/INSERT INTO \"\1\" (/" h2-data.sql
sed -i "s/'\\\0'/'0'/g" h2-data.sql
sed -i "s/'\x01'/'1'/g" h2-data.sql

# assemble aggregated import file
echo 'DROP ALL OBJECTS;' > h2-import.sql
cat h2-structure.sql >> h2-import.sql
cat h2-structure-constraint.sql >> h2-import.sql
echo 'SET REFERENTIAL_INTEGRITY FALSE;' >> h2-import.sql
cat h2-data.sql >> h2-import.sql
echo "SCRIPT TO '../h2/h2.sql';" >> h2-import.sql

# execute h2 script through maven 
mvn -o -f "$(cygpath -m -l $ALGOTRADER_HOME/core/pom.xml)" exec:java -Dexec.mainClass=org.h2.tools.RunScript -Dexec.args="-url jdbc:h2:~/test;MODE=MySQL -user sa -script h2-import.sql"

# remove temporary files
rm -f h2*.sql
