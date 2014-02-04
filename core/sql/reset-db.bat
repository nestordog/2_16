set db=algotrader

mysql.exe -u root -ppassword -e "drop database %db%; create database %db%;"

mysql.exe -u root -ppassword %db% < db-structure.sql

mysql.exe -u root -ppassword %db% < db-data.sql
