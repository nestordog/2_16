#!/bin/sh

cd /usr/share/AlgoTrader/backup

/usr/bin/mysqldump -u root -ppassword --single-transaction --flush-logs --delete-master-logs --master-data=2 algotrader | gzip > backup_$(date +%y%m%d).sql.gz
