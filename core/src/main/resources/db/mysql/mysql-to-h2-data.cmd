:: batch script to convert mysql sample data into an h2 import file

SET mypath=%~dp0
cd %mypath:~0,-1% 

:: copy mysql data
echo SET REFERENTIAL_INTEGRITY FALSE; > ..\sample\h2\h2-data.sql
type ..\sample\mysql\mysql-data.sql >> ..\sample\h2\h2-data.sql

:: clean data
sed -i "/^\/\*.*\*\/;$/ d" ..\sample\h2\h2-data.sql
sed -i "/^--.*$/ d" ..\sample\h2\h2-data.sql
sed -i "s/INSERT INTO `\(.*\)` [(]/INSERT INTO \"\1\" (/" ..\sample\h2\h2-data.sql
sed -i "s/'\\0'/'0'/g" ..\sample\h2\h2-data.sql
sed -i "s/'\x01'/'1'/g" ..\sample\h2\h2-data.sql

del sed*
