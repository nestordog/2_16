set db=algotrader

mysql.exe -u root -ppassword -e "drop database %db%; create database %db%;"

mysql.exe -u root -ppassword %db% < %ALGOTRADER_HOME%\core\sql\db-structure.sql

mysql.exe -u root -ppassword %db% < %ALGOTRADER_HOME%\core\sql\db-data.sql
