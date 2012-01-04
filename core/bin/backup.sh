#!/bin/sh

cd /usr/share/AlgoTrader/backup

/usr/bin/mysqldump -u root -ppassword algotrader > backup.sql
