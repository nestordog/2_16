#!/bin/sh

cd /usr/local/AlgoTrader/sql

mysqldump -u @localhost algotrader > backup.sql

smbclient -U aflury -c "cd aflury/Algotrader/backup;put backup.sql" //bubba/home franklin91
