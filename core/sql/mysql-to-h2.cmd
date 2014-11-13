:: batch script to convert mysql db-structure / db-data into an h2 import file
:: steps
:: 1) run script
:: 2) open h2 console using the following JDBC URL: jdbc:h2:~/test;MODE=MySQL
:: 3) past content of file h2-import.sql into console and click 'Run'
:: 4) get the h2 export/backup file (named h2.sql) from the user home directly 

copy db-structure.sql h2-structure.sql

sed -i "/^\/\*.*\*\/;$/ d" h2-structure.sql
sed -i "/^--.*$/ d" h2-structure.sql
sed -i "s/enum(.*)/varchar(100)/" h2-structure.sql
sed -i "s/ENGINE=InnoDB DEFAULT CHARSET=latin1;/;/" h2-structure.sql
sed -i "s/ENGINE=MyISAM DEFAULT CHARSET=latin1;/;/" h2-structure.sql
sed -i "s/DROP TABLE IF EXISTS `\(.*\)`;/DROP TABLE IF EXISTS \"\1\";/" h2-structure.sql
sed -i "s/CREATE TABLE `\(.*\)`/CREATE TABLE \"\1\"/" h2-structure.sql
sed -i "s/KEY `.*` [(]/KEY (/" h2-structure.sql
sed -i "s/REFERENCES `\(.*\)` [(]/REFERENCES \"\1\" (/" h2-structure.sql

copy h2-structure.sql h2-structure-constraint.sql

sed -i "/^.*CONSTRAINT.*$/ d" h2-structure.sql


copy db-data.sql h2-data.sql

sed -i "/^\/\*.*\*\/;$/ d" h2-data.sql
sed -i "/^--.*$/ d" h2-data.sql
sed -i "s/INSERT INTO `\(.*\)` [(]/INSERT INTO \"\1\" (/" h2-data.sql
sed -i "s/'\\0'/'0'/g" h2-data.sql
sed -i "s/'\x01'/'1'/g" h2-data.sql

echo DROP ALL OBJECTS; > h2-import.sql

type h2-structure.sql >> h2-import.sql
type h2-structure-constraint.sql >> h2-import.sql

echo SET REFERENTIAL_INTEGRITY FALSE; >> h2-import.sql

type h2-data.sql >> h2-import.sql

echo SCRIPT TO '~/h2.sql'; >> h2-import.sql

rm sed*
rm h2-structure.sql
rm h2-structure-constraint.sql
rm h2-data.sql
