 mysqldump -u root -ppassword ^
 --where="security_fk in (select id from security where security_family_fk = 22 or id = 10 or id = 29) and date_time > '2012-01-01'" ^
 --skip-lock-tables ^
 --no-create-info^
 algotrader tick > tick.sql

mysqldump -u root -ppassword ^
--where="id > 1000" ^
--no-create-info^
algotrader security future stock_option > security.sql
